package io.github.decadedx.smarthealthcare.controller;

import io.github.decadedx.smarthealthcare.common.Result;
import io.github.decadedx.smarthealthcare.service.AppointmentService;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Dx
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    /** 挂号单查询服务。 */
    private final AppointmentService appointmentService;

    /**
     * 注入挂号单查询服务。
     *
     * @param appointmentService 挂号单查询服务
     */
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * 查询指定挂号单的详情。
     *
     * @param appointmentId 挂号单主键
     * @return 挂号单详情响应
     */
    @GetMapping("/{appointmentId}")
    public Result<AppointmentDetailVO> getDetail(@PathVariable @Positive Integer appointmentId) {
        AppointmentDetailVO detail = appointmentService.getDetail(appointmentId);
        return Result.success(detail);
    }
}
