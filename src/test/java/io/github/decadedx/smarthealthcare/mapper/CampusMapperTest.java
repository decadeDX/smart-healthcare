package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Campus;
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
public class CampusMapperTest {
    @Autowired
    private CampusMapper campusMapper;

    @Test
    void shouldFindCampusById() {
        Campus campus = campusMapper.selectById(1);

        assertNotNull(campus);
        assertEquals("本部院区", campus.getName());
    }

    @Test
    void StringRedisTemplateTest() {
        String key = "testKey";
        String value = "testValue";

        // Save the value
    }
}
