package io.github.decadedx.smarthealthcare.vo;

import java.util.List;
import lombok.Data;

/**
 * 患者端查询医生排班的响应数据。
 */
@Data
public class DoctorScheduleVO {

    /** 被查询医生的摘要。 */
    private DoctorSummaryVO doctor;

    /** 按出诊日期、时段和排班主键排序的排班列表。 */
    private List<ScheduleSummaryVO> schedules;
}
