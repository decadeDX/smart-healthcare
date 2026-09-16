package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Schedule;
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
public class ScheduleMapperTest {
    @Autowired
    private ScheduleMapper scheduleMapper;

    @Test
    void shouldFindScheduleById() {
        Schedule schedule = scheduleMapper.selectById(1);

        assertNotNull(schedule);
        assertEquals(1, schedule.getId());
        assertEquals(1, schedule.getDoctorId());
    }
}
