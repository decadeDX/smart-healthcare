package io.github.decadedx.smarthealthcare.vo;

import java.util.Date;
import lombok.Data;

/**
 * 挂号单详情联合查询的平铺结果。
 *
 * <p>该对象仅承载 Mapper 查询结果，不携带任何反向关联，供后续服务层组装有限层级的接口 DTO。</p>
 */
@Data
public class AppointmentDetailVO {

    /** 挂号单主键。 */
    private Integer appointmentId;

    /** 挂号单当前状态。 */
    private String appointmentStatus;

    /** 就诊患者主键。 */
    private Integer patientId;

    /** 就诊患者实名。 */
    private String patientRealName;

    /** 排班主键。 */
    private Integer scheduleId;

    /** 排班出诊日期。 */
    private Date scheduleWorkDate;

    /** 排班出诊时段。 */
    private String scheduleTimeSlot;

    /** 排班剩余号源数。 */
    private Integer scheduleCapacity;

    /** 出诊医生主键。 */
    private Integer doctorId;

    /** 出诊医生姓名。 */
    private String doctorName;

    /** 出诊医生职称。 */
    private String doctorTitle;

    /** 医生所属科室主键。 */
    private Integer departmentId;

    /** 医生所属科室名称。 */
    private String departmentName;

    /** 科室所属院区主键。 */
    private Integer campusId;

    /** 科室所属院区名称。 */
    private String campusName;

    /** 科室所属院区详细地址。 */
    private String campusAddress;

    /** 科室主任主键，可为空。 */
    private Integer directorId;

    /** 科室主任姓名，可为空。 */
    private String directorName;

    /** 科室主任职称，可为空。 */
    private String directorTitle;

    /** 电子病历主键，可为空。 */
    private Integer medicalRecordId;

    /** 电子病历诊断结果，可为空。 */
    private String medicalRecordDiagnosis;

    /** 电子病历处方信息，可为空。 */
    private String medicalRecordPrescription;
}
