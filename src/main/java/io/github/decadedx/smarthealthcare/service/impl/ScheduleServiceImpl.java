package io.github.decadedx.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.github.decadedx.smarthealthcare.entity.Doctor;
import io.github.decadedx.smarthealthcare.entity.Schedule;
import io.github.decadedx.smarthealthcare.exception.BusinessException;
import io.github.decadedx.smarthealthcare.mapper.DoctorMapper;
import io.github.decadedx.smarthealthcare.mapper.ScheduleMapper;
import io.github.decadedx.smarthealthcare.service.ScheduleService;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleItemVO;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleVO;
import io.github.decadedx.smarthealthcare.vo.DoctorSummaryVO;
import io.github.decadedx.smarthealthcare.vo.ScheduleCapacityUpdateVO;
import io.github.decadedx.smarthealthcare.vo.ScheduleSummaryVO;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 负责医生排班的缓存查询及容量变更，并保证变更提交后精准失效医生维度缓存。
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    /** 医生排班 JSON 缓存键前缀。 */
    private static final String CACHE_KEY_PREFIX = "hospital:doctor:schedules:";

    /** 医生维度的缓存重建与写入协调锁前缀。 */
    private static final String LOCK_KEY_PREFIX = "hospital:doctor:schedules:lock:";

    /** 按令牌删除锁，防止已过期持有者误删新持有者的锁。 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    /** 医生数据访问对象。 */
    private final DoctorMapper doctorMapper;

    /** Redis 字符串缓存访问对象。 */
    private final StringRedisTemplate redisTemplate;

    /** 与 HTTP 一致的 JSON 序列化器。 */
    private final ObjectMapper objectMapper;

    /** 在 Service 层显式控制数据库事务边界。 */
    private final TransactionTemplate transactionTemplate;

    /** 医生排班缓存的兜底生存时间。 */
    private final Duration cacheTtl;

    /** 医生维度协调锁的短租约。 */
    private final Duration lockTtl;

    /**
     * 注入排班查询和缓存一致性所需的基础设施。
     *
     * @param doctorMapper 医生 Mapper
     * @param redisTemplate Redis 字符串操作对象
     * @param objectMapper JSON 序列化器
     * @param transactionTemplate 数据库事务模板
     * @param cacheTtl 排班缓存兜底 TTL
     * @param lockTtl 排班读写协调锁 TTL
     */
    public ScheduleServiceImpl(DoctorMapper doctorMapper, StringRedisTemplate redisTemplate,
                               ObjectMapper objectMapper, TransactionTemplate transactionTemplate,
                               @Value("${hospital.cache.doctor-schedule-ttl:24h}") Duration cacheTtl,
                               @Value("${hospital.cache.doctor-schedule-lock-ttl:5s}") Duration lockTtl) {
        this.doctorMapper = doctorMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
        this.cacheTtl = cacheTtl;
        this.lockTtl = lockTtl;
    }

    /**
     * 查询医生的全部排班并转换为内部快照；医生不存在时保留空列表语义。
     *
     * @param doctorId 医生主键
     * @return 医生不存在或没有排班时为空列表
     */
    @Override
    public List<DoctorScheduleItemVO> findDoctorScheduleSnapshot(Integer doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return List.of();
        }
        return findSchedules(doctorId).stream().map(schedule -> toSnapshot(doctor, schedule)).toList();
    }

    /**
     * 按 Cache-Aside 读取患者端医生排班；Redis 不可用时允许本次查询直接回源数据库。
     *
     * @param doctorId 医生主键
     * @return 医生摘要和有序排班列表
     */
    @Override
    public DoctorScheduleVO getDoctorSchedules(Integer doctorId) {
        try {
            DoctorScheduleVO cached = getCached(doctorId);
            if (cached != null) {
                return cached;
            }
            return withDoctorLock(doctorId, () -> {
                DoctorScheduleVO rechecked = getCached(doctorId);
                if (rechecked != null) {
                    return rechecked;
                }
                DoctorScheduleVO loaded = loadDoctorSchedules(doctorId);
                putCached(doctorId, loaded);
                return loaded;
            });
        } catch (DataAccessException exception) {
            return loadDoctorSchedules(doctorId);
        }
    }

    /**
     * 更新余量并返回所属医生，用于兼容内部调用方；成功路径同样会完成缓存失效。
     *
     * @param scheduleId 排班主键
     * @param capacity 更新后的非负余量
     * @return 所属医生主键
     */
    @Override
    public Integer updateCapacity(Integer scheduleId, Integer capacity) {
        return updateCapacityAndGetSnapshot(scheduleId, capacity).getDoctorId();
    }

    /**
     * 在数据库提交后删除对应医生缓存；Redis 锁或删除失败时不对外承诺命令成功。
     *
     * @param scheduleId 排班主键
     * @param capacity 更新后的非负余量
     * @return 已提交更新的最小排班回执
     */
    @Override
    public ScheduleCapacityUpdateVO updateCapacityAndGetSnapshot(Integer scheduleId, Integer capacity) {
        if (capacity == null || capacity < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "排班余量必须为非负整数");
        }
        Schedule schedule = getById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "排班不存在");
        }
        try {
            return withDoctorLock(schedule.getDoctorId(), () -> updateAndEvict(scheduleId, capacity));
        } catch (DataAccessException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "CACHE_UNAVAILABLE",
                    "排班更新后的缓存失效未完成，请稍后重试");
        }
    }

    /**
     * 在事务提交后执行精准缓存删除。
     *
     * @param scheduleId 排班主键
     * @param capacity 新余量
     * @return 更新后的排班回执
     */
    private ScheduleCapacityUpdateVO updateAndEvict(Integer scheduleId, Integer capacity) {
        ScheduleCapacityUpdateVO updated = Objects.requireNonNull(transactionTemplate.execute(status -> {
            Schedule current = getById(scheduleId);
            if (current == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "排班不存在");
            }
            current.setCapacity(capacity);
            if (!updateById(current)) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_UPDATE_FAILED", "排班余量更新失败");
            }
            return toCapacityUpdateVO(current);
        }));
        evictCached(updated.getDoctorId());
        return updated;
    }

    /**
     * 从数据库读取医生及其稳定排序后的排班列表。
     *
     * @param doctorId 医生主键
     * @return 患者端排班响应
     */
    private DoctorScheduleVO loadDoctorSchedules(Integer doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", "医生不存在");
        }
        DoctorScheduleVO response = new DoctorScheduleVO();
        response.setDoctor(toDoctorSummary(doctor));
        response.setSchedules(findSchedules(doctorId).stream().map(this::toScheduleSummary).toList());
        return response;
    }

    /**
     * 读取医生维度的 JSON 缓存。
     *
     * @param doctorId 医生主键
     * @return 命中时的响应对象；未命中时为 null
     */
    private DoctorScheduleVO getCached(Integer doctorId) {
        String json = redisTemplate.opsForValue().get(cacheKey(doctorId));
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, DoctorScheduleVO.class);
        } catch (JacksonException exception) {
            redisTemplate.delete(cacheKey(doctorId));
            return null;
        }
    }

    /**
     * 将响应数据以可读 JSON 写入医生维度缓存；写入失败不影响本次读请求。
     *
     * @param doctorId 医生主键
     * @param response 待缓存的排班响应
     */
    private void putCached(Integer doctorId, DoctorScheduleVO response) {
        try {
            redisTemplate.opsForValue().set(cacheKey(doctorId), objectMapper.writeValueAsString(response), cacheTtl);
        } catch (DataAccessException | JacksonException exception) {
            // 本次读已拿到数据库快照，缓存失败仅影响后续命中率。
        }
    }

    /**
     * 删除指定医生的排班缓存；键不存在视为已完成失效。
     *
     * @param doctorId 医生主键
     */
    private void evictCached(Integer doctorId) {
        redisTemplate.delete(cacheKey(doctorId));
    }

    /**
     * 以医生维度短租约协调缓存重建和排班写入，避免并发回源或旧快照回填。
     *
     * @param doctorId 医生主键
     * @param operation 持锁执行的业务动作
     * @param <T> 业务动作返回类型
     * @return 业务动作结果
     */
    private <T> T withDoctorLock(Integer doctorId, Supplier<T> operation) {
        String lockKey = LOCK_KEY_PREFIX + doctorId;
        String token = UUID.randomUUID().toString();
        for (int attempt = 0; attempt < 20; attempt++) {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, token, lockTtl))) {
                try {
                    return operation.get();
                } finally {
                    redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
                }
            }
            try {
                Thread.sleep(25L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "CACHE_COORDINATION_FAILED",
                "排班缓存正在重建，请稍后重试");
    }

    /**
     * 查询并稳定排序某位医生的排班实体。
     *
     * @param doctorId 医生主键
     * @return 有序排班实体列表
     */
    private List<Schedule> findSchedules(Integer doctorId) {
        return lambdaQuery().eq(Schedule::getDoctorId, doctorId)
                .orderByAsc(Schedule::getWorkDate, Schedule::getTimeSlot, Schedule::getId).list();
    }

    /**
     * 构造 Redis 缓存键。
     *
     * @param doctorId 医生主键
     * @return 医生排班缓存键
     */
    private String cacheKey(Integer doctorId) {
        return CACHE_KEY_PREFIX + doctorId;
    }

    /**
     * 转换医生摘要。
     *
     * @param doctor 医生实体
     * @return 对外医生摘要
     */
    private DoctorSummaryVO toDoctorSummary(Doctor doctor) {
        DoctorSummaryVO summary = new DoctorSummaryVO();
        summary.setId(doctor.getId());
        summary.setName(doctor.getName());
        summary.setTitle(doctor.getTitle());
        return summary;
    }

    /**
     * 转换患者端单条排班摘要。
     *
     * @param schedule 排班实体
     * @return 对外排班摘要
     */
    private ScheduleSummaryVO toScheduleSummary(Schedule schedule) {
        ScheduleSummaryVO summary = new ScheduleSummaryVO();
        summary.setId(schedule.getId());
        summary.setWorkDate(schedule.getWorkDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        summary.setTimeSlot(schedule.getTimeSlot());
        summary.setCapacity(schedule.getCapacity());
        return summary;
    }

    /**
     * 转换后台容量调整回执。
     *
     * @param schedule 已更新排班实体
     * @return 后台最小快照
     */
    private ScheduleCapacityUpdateVO toCapacityUpdateVO(Schedule schedule) {
        ScheduleCapacityUpdateVO response = new ScheduleCapacityUpdateVO();
        response.setId(schedule.getId());
        response.setDoctorId(schedule.getDoctorId());
        response.setWorkDate(schedule.getWorkDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        response.setTimeSlot(schedule.getTimeSlot());
        response.setCapacity(schedule.getCapacity());
        return response;
    }

    /**
     * 转换内部排班快照。
     *
     * @param doctor 医生实体
     * @param schedule 排班实体
     * @return 带医生摘要的内部快照
     */
    private DoctorScheduleItemVO toSnapshot(Doctor doctor, Schedule schedule) {
        DoctorScheduleItemVO snapshot = new DoctorScheduleItemVO();
        snapshot.setDoctorId(doctor.getId());
        snapshot.setDoctorName(doctor.getName());
        snapshot.setDoctorTitle(doctor.getTitle());
        snapshot.setScheduleId(schedule.getId());
        snapshot.setWorkDate(schedule.getWorkDate());
        snapshot.setTimeSlot(schedule.getTimeSlot());
        snapshot.setCapacity(schedule.getCapacity());
        return snapshot;
    }
}
