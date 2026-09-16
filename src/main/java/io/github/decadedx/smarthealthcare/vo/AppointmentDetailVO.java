package io.github.decadedx.smarthealthcare.vo;

import java.time.LocalDate;
import lombok.Data;

/**
 * 患者端挂号单详情的有限单向响应树，不含任何 Entity 或反向关联集合。
 */
@Data
public class AppointmentDetailVO {

    /** 挂号单主键。 */
    private Integer id;

    /** 挂号单当前状态。 */
    private String status;

    /** 挂号患者最小摘要。 */
    private PatientSummaryVO patient;

    /** 排班、出诊医生和所属组织信息。 */
    private ScheduleDetailVO schedule;

    /** 可选电子病历；尚未产生病历时为 null。 */
    private MedicalRecordDetailVO medicalRecord;

    /** 挂号患者的最小展示信息。 */
    @Data
    public static class PatientSummaryVO {

        /** 患者主键。 */
        private Integer id;

        /** 患者真实姓名，不包含身份证号。 */
        private String realName;
    }

    /** 挂号关联的排班详情。 */
    @Data
    public static class ScheduleDetailVO {

        /** 排班主键。 */
        private Integer id;

        /** 出诊日期。 */
        private LocalDate workDate;

        /** 出诊时段。 */
        private String timeSlot;

        /** 当前剩余可预约号源数。 */
        private Integer capacity;

        /** 出诊医生详情。 */
        private DoctorDetailVO doctor;
    }

    /** 出诊医生及所属科室详情。 */
    @Data
    public static class DoctorDetailVO {

        /** 医生主键。 */
        private Integer id;

        /** 医生姓名。 */
        private String name;

        /** 医生职称。 */
        private String title;

        /** 所属科室详情。 */
        private DepartmentDetailVO department;
    }

    /** 医生所属科室及院区、主任摘要。 */
    @Data
    public static class DepartmentDetailVO {

        /** 科室主键。 */
        private Integer id;

        /** 科室名称。 */
        private String name;

        /** 所属院区摘要。 */
        private CampusSummaryVO campus;

        /** 科室主任摘要。 */
        private DoctorSummaryVO director;
    }

    /** 院区最小展示信息。 */
    @Data
    public static class CampusSummaryVO {

        /** 院区主键。 */
        private Integer id;

        /** 院区名称。 */
        private String name;

        /** 院区地址。 */
        private String address;
    }

    /** 电子病历详情。 */
    @Data
    public static class MedicalRecordDetailVO {

        /** 病历主键。 */
        private Integer id;

        /** 诊断结果，可为空。 */
        private String diagnosis;

        /** 处方信息，可为空。 */
        private String prescription;
    }
}
