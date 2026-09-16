package io.github.decadedx.smarthealthcare.vo;

import java.time.LocalDate;
import lombok.Data;

/**
 * 医生排班查询中的单条排班摘要。
 */
@Data
public class ScheduleSummaryVO {

    /** 排班主键。 */
    private Integer id;

    /** 出诊日期。 */
    private LocalDate workDate;

    /** 出诊时段。 */
    private String timeSlot;

    /** 当前剩余号源数。 */
    private Integer capacity;
}
