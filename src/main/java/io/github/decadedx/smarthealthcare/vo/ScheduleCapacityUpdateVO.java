package io.github.decadedx.smarthealthcare.vo;

import java.time.LocalDate;
import lombok.Data;

/**
 * 排班余量更新成功后的最小回执。
 */
@Data
public class ScheduleCapacityUpdateVO {

    /** 排班主键。 */
    private Integer id;

    /** 出诊医生主键，用于确认被失效的排班缓存范围。 */
    private Integer doctorId;

    /** 出诊日期。 */
    private LocalDate workDate;

    /** 出诊时段。 */
    private String timeSlot;

    /** 更新后的剩余号源数。 */
    private Integer capacity;
}
