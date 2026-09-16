package io.github.decadedx.smarthealthcare.service;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import com.baomidou.mybatisplus.spring.service.IService;
import org.springframework.stereotype.Service;

/**
* @author 86183
* @description 针对表【appointment】的数据库操作Service
* @createDate 2026-09-15 21:18:28
*/
@Service
public interface AppointmentService extends IService<Appointment> {

    /**
     * 查询指定挂号单的详情数据。
     *
     * @param appointmentId 挂号单主键
     * @return 挂号单详情
     */
    AppointmentDetailVO getDetail(Integer appointmentId);
}
