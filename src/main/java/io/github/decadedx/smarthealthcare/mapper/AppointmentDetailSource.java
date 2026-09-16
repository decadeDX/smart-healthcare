package io.github.decadedx.smarthealthcare.mapper;

import java.time.LocalDate;
import lombok.Data;

/**
 * 挂号详情联合查询的单行结果，仅用于 Mapper 与 Service 之间传递必要字段，不作为 HTTP 响应对象。
 */
@Data
public class AppointmentDetailSource {

    /** 挂号单主键。 */
    private Integer appointmentId;

    /** 挂号单状态。 */
    private String appointmentStatus;

    /** 患者主键。 */
    private Integer patientId;

    /** 患者真实姓名。 */
    private String patientRealName;

    /** 排班主键。 */
    private Integer scheduleId;

    /** 排班出诊日期。 */
    private LocalDate scheduleWorkDate;

    /** 排班出诊时段。 */
    private String scheduleTimeSlot;

    /** 排班当前余量。 */
    private Integer scheduleCapacity;

    /** 出诊医生主键。 */
    private Integer doctorId;

    /** 出诊医生姓名。 */
    private String doctorName;

    /** 出诊医生职称。 */
    private String doctorTitle;

    /** 科室主键。 */
    private Integer departmentId;

    /** 科室名称。 */
    private String departmentName;

    /** 院区主键。 */
    private Integer campusId;

    /** 院区名称。 */
    private String campusName;

    /** 院区地址。 */
    private String campusAddress;

    /** 科室主任主键。 */
    private Integer directorId;

    /** 科室主任姓名。 */
    private String directorName;

    /** 科室主任职称。 */
    private String directorTitle;

    /** 电子病历主键，可为空。 */
    private Integer medicalRecordId;

    /** 电子病历诊断结果，可为空。 */
    private String medicalRecordDiagnosis;

    /** 电子病历处方信息，可为空。 */
    private String medicalRecordPrescription;
}
