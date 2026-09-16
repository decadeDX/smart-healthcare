package io.github.decadedx.smarthealthcare.vo;

import java.util.Date;
import lombok.Data;

/**
 * 医生排班快照中的单条排班记录。
 *
 * <p>同一医生的基本信息会在其每条排班记录中重复，服务层据此组装医生与排班列表，而不加载挂号单等反向关系。</p>
 */
@Data
public class DoctorScheduleItemVO {

    /** 医生主键。 */
    private Integer doctorId;

    /** 医生姓名。 */
    private String doctorName;

    /** 医生职称。 */
    private String doctorTitle;

    /** 排班主键；医生没有排班时为空。 */
    private Integer scheduleId;

    /** 排班出诊日期；医生没有排班时为空。 */
    private Date workDate;

    /** 排班出诊时段；医生没有排班时为空。 */
    private String timeSlot;

    /** 排班当前剩余号源；医生没有排班时为空。 */
    private Integer capacity;
}
