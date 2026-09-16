package io.github.decadedx.smarthealthcare.service;

import io.github.decadedx.smarthealthcare.entity.Schedule;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleVO;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleItemVO;
import io.github.decadedx.smarthealthcare.vo.ScheduleCapacityUpdateVO;
import com.baomidou.mybatisplus.spring.service.IService;
import java.util.List;


/**
* @author 86183
* @description 针对表【schedule】的数据库操作Service
* @createDate 2026-09-15 21:26:53
*/
public interface ScheduleService extends IService<Schedule> {

    /**
     * 查询医生的全部排班，并转换为有序快照。
     *
     * @param doctorId 医生主键
     * @return 医生不存在或没有排班时返回空列表
     */
    List<DoctorScheduleItemVO> findDoctorScheduleSnapshot(Integer doctorId);

    /**
     * 查询患者端所需的医生摘要和排班列表。
     *
     * @param doctorId 医生主键
     * @return 医生排班响应数据
     */
    DoctorScheduleVO getDoctorSchedules(Integer doctorId);

    /**
     * 更新排班余量并返回所属医生主键，用于后续精准失效缓存。
     *
     * @param scheduleId 排班主键
     * @param capacity 更新后的非负余量
     * @return 所属医生主键
     */
    Integer updateCapacity(Integer scheduleId, Integer capacity);

    /**
     * 更新排班余量并返回后台接口回执。
     *
     * @param scheduleId 排班主键
     * @param capacity 更新后的非负余量
     * @return 更新后的排班最小快照
     */
    ScheduleCapacityUpdateVO updateCapacityAndGetSnapshot(Integer scheduleId, Integer capacity);
}
