package io.github.decadedx.smarthealthcare.vo;

import lombok.Data;

/**
 * 医生对外展示的最小摘要。
 */
@Data
public class DoctorSummaryVO {

    /** 医生主键。 */
    private Integer id;

    /** 医生姓名。 */
    private String name;

    /** 医生职称。 */
    private String title;
}
