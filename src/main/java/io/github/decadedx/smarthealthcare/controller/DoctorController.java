package io.github.decadedx.smarthealthcare.controller;

import io.github.decadedx.smarthealthcare.common.Result;
import io.github.decadedx.smarthealthcare.service.ScheduleService;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleVO;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者端医生排班查询接口。
 */
@Validated
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    /** 排班查询服务。 */
    private final ScheduleService scheduleService;

    /**
     * 注入排班查询服务。
     *
     * @param scheduleService 排班查询服务
     */
    public DoctorController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 查询指定医生的有序排班和当前号源。
     *
     * @param doctorId 医生主键
     * @return 医生摘要和排班列表
     */
    @GetMapping("/{doctorId}/schedules")
    public Result<DoctorScheduleVO> getSchedules(@PathVariable @Positive Integer doctorId) {
        return Result.success(scheduleService.getDoctorSchedules(doctorId));
    }
}
