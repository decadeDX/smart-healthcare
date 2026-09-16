package io.github.decadedx.smarthealthcare.service;

import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 挂号单详情流式查询测试。
 *
 * @author Dx
 * @version 1.0
 */
@SpringBootTest
class AppointmentServiceTest {

    /** 挂号单服务。 */
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 验证服务可组装初始化数据的完整详情平铺结果。
     */
    @Test
    void shouldFindDetailSource() {
        AppointmentDetailVO source = appointmentService.findDetailSource(1);

        assertNotNull(source);
        assertEquals("张三", source.getPatientRealName());
        assertEquals("钟南", source.getDoctorName());
        assertEquals("呼吸内科", source.getDepartmentName());
        assertEquals("本部院区", source.getCampusName());
    }
}
