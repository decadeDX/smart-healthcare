package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Appointment;
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
}
