package io.github.decadedx.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.github.decadedx.smarthealthcare.entity.Appointment;
import io.github.decadedx.smarthealthcare.enums.AppointmentStatus;
import io.github.decadedx.smarthealthcare.exception.BusinessException;
import io.github.decadedx.smarthealthcare.mapper.AppointmentDetailSource;
import io.github.decadedx.smarthealthcare.mapper.AppointmentMapper;
import io.github.decadedx.smarthealthcare.service.AppointmentService;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import io.github.decadedx.smarthealthcare.vo.DoctorSummaryVO;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 负责一次联合查询挂号详情，并组装不含反向关联的患者端响应树。
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    /**
     * 查询挂号单详情；所有关键关联（含科室主任）必须存在，病历允许为空。
     *
     * @param appointmentId 挂号单主键
     * @return 有限层级的挂号详情
     * @throws BusinessException 挂号单不存在、状态非法、病历基数异常或关键关联缺失时抛出
     */
    @Override
    public AppointmentDetailVO getDetail(Integer appointmentId) {
        List<AppointmentDetailSource> sources = baseMapper.findAppointmentDetailSource(appointmentId);
        if (sources.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", "挂号单不存在");
        }
        if (sources.size() > 1) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "DATA_INTEGRITY_ERROR", "同一挂号单存在多条病历");
        }

        AppointmentDetailSource source = sources.get(0);
        validateSource(source);
        return toDetailVO(source);
    }

    /**
     * 校验联合查询中不可为空的业务关联，避免以空对象掩盖脏数据。
     *
     * @param source 联合查询结果
     */
    private void validateSource(AppointmentDetailSource source) {
        if (!AppointmentStatus.isSupported(source.getAppointmentStatus())) {
            throw dataIntegrityError("挂号单状态数据不合法");
        }
        requireValue(source.getPatientId(), "患者");
        requireValue(source.getScheduleId(), "排班");
        requireValue(source.getDoctorId(), "医生");
        requireValue(source.getDepartmentId(), "科室");
        requireValue(source.getCampusId(), "院区");
        requireValue(source.getDirectorId(), "科室主任");
    }

    /**
     * 校验关键关联主键不为空。
     *
     * @param value 关联主键
     * @param relationName 关联业务名称
     */
    private void requireValue(Integer value, String relationName) {
        if (value == null) {
            throw dataIntegrityError("挂号单关联的" + relationName + "不存在");
        }
    }

    /**
     * 构造数据完整性错误。
     *
     * @param message 对外错误说明
     * @return 数据完整性业务异常
     */
    private BusinessException dataIntegrityError(String message) {
        return new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "DATA_INTEGRITY_ERROR", message);
    }

    /**
     * 将联合查询平铺结果组装为有限的详情响应树。
     *
     * @param source 联合查询结果
     * @return 患者端挂号详情
     */
    private AppointmentDetailVO toDetailVO(AppointmentDetailSource source) {
        AppointmentDetailVO detail = new AppointmentDetailVO();
        detail.setId(source.getAppointmentId());
        detail.setStatus(source.getAppointmentStatus());
        detail.setPatient(toPatientSummary(source));
        detail.setSchedule(toScheduleDetail(source));
        detail.setMedicalRecord(toMedicalRecordDetail(source));
        return detail;
    }

    /**
     * 组装患者摘要，不包含身份证号。
     *
     * @param source 联合查询结果
     * @return 患者摘要
     */
    private AppointmentDetailVO.PatientSummaryVO toPatientSummary(AppointmentDetailSource source) {
        AppointmentDetailVO.PatientSummaryVO patient = new AppointmentDetailVO.PatientSummaryVO();
        patient.setId(source.getPatientId());
        patient.setRealName(source.getPatientRealName());
        return patient;
    }

    /**
     * 组装排班及其出诊医生详情。
     *
     * @param source 联合查询结果
     * @return 排班详情
     */
    private AppointmentDetailVO.ScheduleDetailVO toScheduleDetail(AppointmentDetailSource source) {
        AppointmentDetailVO.ScheduleDetailVO schedule = new AppointmentDetailVO.ScheduleDetailVO();
        schedule.setId(source.getScheduleId());
        schedule.setWorkDate(source.getScheduleWorkDate());
        schedule.setTimeSlot(source.getScheduleTimeSlot());
        schedule.setCapacity(source.getScheduleCapacity());
        schedule.setDoctor(toDoctorDetail(source));
        return schedule;
    }

    /**
     * 组装出诊医生及所属科室详情。
     *
     * @param source 联合查询结果
     * @return 出诊医生详情
     */
    private AppointmentDetailVO.DoctorDetailVO toDoctorDetail(AppointmentDetailSource source) {
        AppointmentDetailVO.DoctorDetailVO doctor = new AppointmentDetailVO.DoctorDetailVO();
        doctor.setId(source.getDoctorId());
        doctor.setName(source.getDoctorName());
        doctor.setTitle(source.getDoctorTitle());
        doctor.setDepartment(toDepartmentDetail(source));
        return doctor;
    }

    /**
     * 组装科室、院区和主任摘要。
     *
     * @param source 联合查询结果
     * @return 科室详情
     */
    private AppointmentDetailVO.DepartmentDetailVO toDepartmentDetail(AppointmentDetailSource source) {
        AppointmentDetailVO.DepartmentDetailVO department = new AppointmentDetailVO.DepartmentDetailVO();
        department.setId(source.getDepartmentId());
        department.setName(source.getDepartmentName());
        department.setCampus(toCampusSummary(source));
        department.setDirector(toDirectorSummary(source));
        return department;
    }

    /**
     * 组装院区摘要。
     *
     * @param source 联合查询结果
     * @return 院区摘要
     */
    private AppointmentDetailVO.CampusSummaryVO toCampusSummary(AppointmentDetailSource source) {
        AppointmentDetailVO.CampusSummaryVO campus = new AppointmentDetailVO.CampusSummaryVO();
        campus.setId(source.getCampusId());
        campus.setName(source.getCampusName());
        campus.setAddress(source.getCampusAddress());
        return campus;
    }

    /**
     * 组装科室主任摘要；主任是详情链路中的必填关联。
     *
     * @param source 联合查询结果
     * @return 科室主任摘要
     */
    private DoctorSummaryVO toDirectorSummary(AppointmentDetailSource source) {
        DoctorSummaryVO director = new DoctorSummaryVO();
        director.setId(source.getDirectorId());
        director.setName(source.getDirectorName());
        director.setTitle(source.getDirectorTitle());
        return director;
    }

    /**
     * 组装可选电子病历详情。
     *
     * @param source 联合查询结果
     * @return 没有病历时返回 null
     */
    private AppointmentDetailVO.MedicalRecordDetailVO toMedicalRecordDetail(AppointmentDetailSource source) {
        if (source.getMedicalRecordId() == null) {
            return null;
        }
        AppointmentDetailVO.MedicalRecordDetailVO record = new AppointmentDetailVO.MedicalRecordDetailVO();
        record.setId(source.getMedicalRecordId());
        record.setDiagnosis(source.getMedicalRecordDiagnosis());
        record.setPrescription(source.getMedicalRecordPrescription());
        return record;
    }
}
