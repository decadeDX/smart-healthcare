package io.github.decadedx.smarthealthcare.service.impl;

import io.github.decadedx.smarthealthcare.entity.Schedule;
import io.github.decadedx.smarthealthcare.mapper.ScheduleMapper;
import io.github.decadedx.smarthealthcare.mapper.DoctorMapper;
import io.github.decadedx.smarthealthcare.entity.Doctor;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleItemVO;
import io.github.decadedx.smarthealthcare.service.ScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

/**
* @author 86183
* @description 针对表【schedule】的数据库操作Service实现
* @createDate 2026-09-15 21:26:53
*/
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    /** 医生数据访问对象。 */
    private final DoctorMapper doctorMapper;

    /**
     * 注入医生数据访问对象，供排班快照补充医生摘要。
     *
     * @param doctorMapper 医生 Mapper
     */
    public ScheduleServiceImpl(DoctorMapper doctorMapper) {
        this.doctorMapper = doctorMapper;
    }

    /**
     * 使用 MyBatis-Plus 条件查询并通过 Stream 转换医生排班快照。
     *
     * @param doctorId 医生主键
     * @return 医生不存在或没有排班时返回空列表
     */
    @Override
    public List<DoctorScheduleItemVO> findDoctorScheduleSnapshot(Integer doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return List.of();
        }
        return lambdaQuery()
            .eq(Schedule::getDoctorId, doctorId)
            .orderByAsc(Schedule::getWorkDate, Schedule::getTimeSlot, Schedule::getId)
            .list()
            .stream()
            .map(schedule -> toSnapshot(doctor, schedule))
            .toList();
    }

    /**
     * 更新余量前获取排班所属医生主键，供下一阶段精准删除该医生缓存。
     *
     * @param scheduleId 排班主键
     * @param capacity 更新后的非负余量
     * @return 所属医生主键
     * @throws IllegalArgumentException 余量为负数或排班不存在时抛出
     * @throws IllegalStateException 数据库更新失败时抛出
     */
    @Override
    public Integer updateCapacity(Integer scheduleId, Integer capacity) {
        if (capacity == null || capacity < 0) {
            throw new IllegalArgumentException("排班余量必须为非负整数");
        }
        Schedule schedule = getById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("排班不存在");
        }
        schedule.setCapacity(capacity);
        if (!updateById(schedule)) {
            throw new IllegalStateException("排班余量更新失败");
        }
        return schedule.getDoctorId();
    }

    /**
     * 将单条排班转换为带医生摘要的快照。
     *
     * @param doctor 医生信息
     * @param schedule 排班信息
     * @return 医生排班快照
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




