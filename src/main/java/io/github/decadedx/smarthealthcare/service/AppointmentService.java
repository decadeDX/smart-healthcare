package io.github.decadedx.smarthealthcare.service;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import com.baomidou.mybatisplus.spring.service.IService;

/**
* @author 86183
* @description 针对表【appointment】的数据库操作Service
* @createDate 2026-09-15 21:18:28
*/
public interface AppointmentService extends IService<Appointment> {

    /**
     * 读取挂号单详情所需的有限关联数据。
     *
     * @param appointmentId 挂号单主键
     * @return 平铺详情数据；挂号单不存在时返回 {@code null}
     */
    AppointmentDetailVO findDetailSource(Integer appointmentId);
}
