package io.github.decadedx.smarthealthcare.controller;

import io.github.decadedx.smarthealthcare.common.Result;
import io.github.decadedx.smarthealthcare.dto.ScheduleCapacityUpdateDTO;
import io.github.decadedx.smarthealthcare.service.ScheduleService;
import io.github.decadedx.smarthealthcare.vo.ScheduleCapacityUpdateVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 院办后台排班调整接口。
 */
@Validated
@RestController
@RequestMapping("/api/admin/schedules")
public class ScheduleController {

    /** 排班命令服务。 */
    private final ScheduleService scheduleService;

    /**
     * 注入排班命令服务。
     *
     * @param scheduleService 排班命令服务
     */
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 调整指定排班的余量；余量为 0 时表示紧急停诊。
     *
     * @param scheduleId 排班主键
     * @param request 余量更新请求
     * @return 更新后的排班最小快照
     */

    @PatchMapping("/{scheduleId}/capacity")
    public Result<ScheduleCapacityUpdateVO> updateCapacity(
            @PathVariable @Positive Integer scheduleId,
            @Valid @RequestBody ScheduleCapacityUpdateDTO request) {
        return Result.success(scheduleService.updateCapacityAndGetSnapshot(scheduleId, request.getCapacity()));
    }
}
