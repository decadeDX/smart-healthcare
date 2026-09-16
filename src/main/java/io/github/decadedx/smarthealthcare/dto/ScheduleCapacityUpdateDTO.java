package io.github.decadedx.smarthealthcare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台调整排班余量的请求参数。
 */
@Data
public class ScheduleCapacityUpdateDTO {

    /** 更新后的剩余号源数；0 表示该排班停诊。 */
    @NotNull(message = "capacity 不能为空")
    @Min(value = 0, message = "capacity 必须为非负整数")
    private Integer capacity;
}
