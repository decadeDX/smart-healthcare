package io.github.decadedx.smarthealthcare.service.impl;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import io.github.decadedx.smarthealthcare.entity.Campus;
import io.github.decadedx.smarthealthcare.entity.Department;
import io.github.decadedx.smarthealthcare.entity.Doctor;
import io.github.decadedx.smarthealthcare.entity.MedicalRecord;
import io.github.decadedx.smarthealthcare.entity.Patient;
import io.github.decadedx.smarthealthcare.entity.Schedule;
import io.github.decadedx.smarthealthcare.enums.AppointmentStatus;
import io.github.decadedx.smarthealthcare.exception.BusinessException;
import io.github.decadedx.smarthealthcare.mapper.AppointmentMapper;
import io.github.decadedx.smarthealthcare.mapper.CampusMapper;
import io.github.decadedx.smarthealthcare.mapper.DepartmentMapper;
import io.github.decadedx.smarthealthcare.mapper.DoctorMapper;
import io.github.decadedx.smarthealthcare.mapper.MedicalRecordMapper;
import io.github.decadedx.smarthealthcare.mapper.PatientMapper;
import io.github.decadedx.smarthealthcare.mapper.ScheduleMapper;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import io.github.decadedx.smarthealthcare.service.AppointmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import java.time.ZoneId;

import io.github.decadedx.smarthealthcare.vo.DoctorSummaryVO;
import org.springframework.stereotype.Service;

/**
* @author 86183
* @description 针对表【appointment】的数据库操作Service实现
* @createDate 2026-09-15 21:18:28
*/
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    /** 患者数据访问对象。 */
    private final PatientMapper patientMapper;

    /** 排班数据访问对象。 */
    private final ScheduleMapper scheduleMapper;

    /** 医生数据访问对象。 */
    private final DoctorMapper doctorMapper;

    /** 科室数据访问对象。 */
    private final DepartmentMapper departmentMapper;

    /** 院区数据访问对象。 */
    private final CampusMapper campusMapper;

    /** 病历数据访问对象。 */
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * 注入构造挂号详情所需的关联表 Mapper。
     *
     * @param patientMapper 患者 Mapper
     * @param scheduleMapper 排班 Mapper
     * @param doctorMapper 医生 Mapper
     * @param departmentMapper 科室 Mapper
     * @param campusMapper 院区 Mapper
     * @param medicalRecordMapper 病历 Mapper
     */
    public AppointmentServiceImpl(PatientMapper patientMapper, ScheduleMapper scheduleMapper,
                                  DoctorMapper doctorMapper, DepartmentMapper departmentMapper,
                                  CampusMapper campusMapper, MedicalRecordMapper medicalRecordMapper) {
        this.patientMapper = patientMapper;
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.campusMapper = campusMapper;
        this.medicalRecordMapper = medicalRecordMapper;
    }

    /**
     * 查询挂号单及其必要关联数据，并组装不含反向关系的详情响应。
     *
     * @param appointmentId 挂号单主键
     * @return 挂号单详情
     * @throws BusinessException 挂号单不存在、状态非法或关键关联（含科室主任）缺失时抛出
     */
    @Override
    public AppointmentDetailVO getDetail(Integer appointmentId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(org.springframework.http.HttpStatus.NOT_FOUND,
                    "APPOINTMENT_NOT_FOUND", "挂号单不存在");
        }
        if (!AppointmentStatus.isSupported(appointment.getStatus())) {
            throw new BusinessException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "DATA_INTEGRITY_ERROR", "挂号单状态数据不合法");
        }

        Patient patient = requireEntity(patientMapper.selectById(appointment.getPatientId()), "患者");
        Schedule schedule = requireEntity(scheduleMapper.selectById(appointment.getScheduleId()), "排班");
        Doctor doctor = requireEntity(doctorMapper.selectById(schedule.getDoctorId()), "医生");
        Department department = requireEntity(departmentMapper.selectById(doctor.getDeptId()), "科室");
        Campus campus = requireEntity(campusMapper.selectById(department.getCampusId()), "院区");
        Doctor director = requireEntity(
                department.getDirectorId() == null ? null : doctorMapper.selectById(department.getDirectorId()),
                "科室主任");
        MedicalRecord medicalRecord = medicalRecordMapper.selectList(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getAppointmentId, appointmentId)
                .orderByAsc(MedicalRecord::getId))
            .stream()
            .findFirst()
            .orElse(null);

        return toDetailSource(appointment, patient, schedule, doctor, department, campus, director, medicalRecord);
    }

    /**
     * 校验查询出的关键关联实体存在。
     *
     * @param entity 关联查询结果
     * @param entityName 实体业务名称
     * @param <T> 实体类型
     * @return 已确认非空的实体
     */
    private <T> T requireEntity(T entity, String entityName) {
        if (entity == null) {
            throw new BusinessException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "DATA_INTEGRITY_ERROR", "挂号单关联的" + entityName + "不存在");
        }
        return entity;
    }

    /**
     * 将多表实体转换为不含反向对象的详情平铺数据。
     *
     * @param appointment 挂号单
     * @param patient 患者
     * @param schedule 排班
     * @param doctor 出诊医生
     * @param department 科室
     * @param campus 院区
     * @param director 科室主任，不可为空
     * @param medicalRecord 病历，可为空
     * @return 详情平铺数据
     */
    private AppointmentDetailVO toDetailSource(Appointment appointment, Patient patient, Schedule schedule,
                                                Doctor doctor, Department department, Campus campus,
                                                Doctor director, MedicalRecord medicalRecord) {
        AppointmentDetailVO detail = new AppointmentDetailVO();
        detail.setId(appointment.getId());
        detail.setStatus(appointment.getStatus());

        AppointmentDetailVO.PatientSummaryVO patientSummary = new AppointmentDetailVO.PatientSummaryVO();
        patientSummary.setId(patient.getId());
        patientSummary.setRealName(patient.getRealName());
        detail.setPatient(patientSummary);

        AppointmentDetailVO.CampusSummaryVO campusSummary = new AppointmentDetailVO.CampusSummaryVO();
        campusSummary.setId(campus.getId());
        campusSummary.setName(campus.getName());
        campusSummary.setAddress(campus.getAddress());

        DoctorSummaryVO directorSummary = new DoctorSummaryVO();
        directorSummary.setId(director.getId());
        directorSummary.setName(director.getName());
        directorSummary.setTitle(director.getTitle());

        AppointmentDetailVO.DepartmentDetailVO departmentDetail = new AppointmentDetailVO.DepartmentDetailVO();
        departmentDetail.setId(department.getId());
        departmentDetail.setName(department.getName());
        departmentDetail.setCampus(campusSummary);
        departmentDetail.setDirector(directorSummary);

        AppointmentDetailVO.DoctorDetailVO doctorDetail = new AppointmentDetailVO.DoctorDetailVO();
        doctorDetail.setId(doctor.getId());
        doctorDetail.setName(doctor.getName());
        doctorDetail.setTitle(doctor.getTitle());
        doctorDetail.setDepartment(departmentDetail);

        AppointmentDetailVO.ScheduleDetailVO scheduleDetail = new AppointmentDetailVO.ScheduleDetailVO();
        scheduleDetail.setId(schedule.getId());
        scheduleDetail.setWorkDate(schedule.getWorkDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        scheduleDetail.setTimeSlot(schedule.getTimeSlot());
        scheduleDetail.setCapacity(schedule.getCapacity());
        scheduleDetail.setDoctor(doctorDetail);
        detail.setSchedule(scheduleDetail);

        if (medicalRecord != null) {
            AppointmentDetailVO.MedicalRecordDetailVO recordDetail = new AppointmentDetailVO.MedicalRecordDetailVO();
            recordDetail.setId(medicalRecord.getId());
            recordDetail.setDiagnosis(medicalRecord.getDiagnosis());
            recordDetail.setPrescription(medicalRecord.getPrescription());
            detail.setMedicalRecord(recordDetail);
        }
        return detail;
    }
}
