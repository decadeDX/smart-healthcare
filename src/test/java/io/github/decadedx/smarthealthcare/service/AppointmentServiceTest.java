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
     * 验证服务可组装初始化数据的完整无环详情树。
     */
    @Test
    void shouldGetDetail() {
        AppointmentDetailVO detail = appointmentService.getDetail(1);

        assertNotNull(detail);
        assertEquals("张三", detail.getPatient().getRealName());
        assertEquals("钟南", detail.getSchedule().getDoctor().getName());
        assertEquals("呼吸内科", detail.getSchedule().getDoctor().getDepartment().getName());
        assertEquals("本部院区", detail.getSchedule().getDoctor().getDepartment().getCampus().getName());
    }
}
