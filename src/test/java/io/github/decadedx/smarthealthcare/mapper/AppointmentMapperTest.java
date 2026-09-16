package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dx
 * @version 1.0
 */
@SpringBootTest
class AppointmentMapperTest {

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 验证 MyBatis-Plus 可按主键读取初始化挂号单。
     */
    @Test
    void shouldFindAppointmentById() {
        Appointment appointment = appointmentMapper.selectById(1);

        assertNotNull(appointment);
        assertEquals("BOOKED", appointment.getStatus());
    }

    /**
     * 验证联合查询一次返回组装详情所需字段。
     */
    @Test
    void shouldFindAppointmentDetailSource() {
        List<AppointmentDetailSource> sources = appointmentMapper.findAppointmentDetailSource(1);

        assertEquals(1, sources.size());
        AppointmentDetailSource source = sources.get(0);
        assertEquals("张三", source.getPatientRealName());
        assertEquals("钟南", source.getDoctorName());
        assertEquals("呼吸内科", source.getDepartmentName());
        assertEquals("本部院区", source.getCampusName());
        assertEquals("钟南", source.getDirectorName());
    }
}
