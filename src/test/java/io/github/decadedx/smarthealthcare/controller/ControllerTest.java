package io.github.decadedx.smarthealthcare.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.decadedx.smarthealthcare.common.Result;
import io.github.decadedx.smarthealthcare.dto.ScheduleCapacityUpdateDTO;
import io.github.decadedx.smarthealthcare.service.AppointmentService;
import io.github.decadedx.smarthealthcare.service.ScheduleService;
import io.github.decadedx.smarthealthcare.vo.AppointmentDetailVO;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleVO;
import io.github.decadedx.smarthealthcare.vo.ScheduleCapacityUpdateVO;
import org.junit.jupiter.api.Test;

/**
 * Controller 的委派与统一响应测试。
 */
class ControllerTest {

    /**
     * 验证挂号单 Controller 将路径参数传递给 Service，并包装成功响应。
     */
    @Test
    void shouldDelegateAppointmentDetailQuery() {
        AppointmentService appointmentService = mock(AppointmentService.class);
        AppointmentDetailVO detail = new AppointmentDetailVO();
        detail.setId(1);
        when(appointmentService.getDetail(1)).thenReturn(detail);

        Result<AppointmentDetailVO> result = new AppointmentController(appointmentService).getDetail(1);

        assertEquals("SUCCESS", result.code());
        assertEquals(detail, result.data());
        verify(appointmentService).getDetail(1);
    }

    /**
     * 验证医生排班 Controller 不直接查询数据库，只委派给 Service。
     */
    @Test
    void shouldDelegateDoctorScheduleQuery() {
        ScheduleService scheduleService = mock(ScheduleService.class);
        DoctorScheduleVO schedules = new DoctorScheduleVO();
        when(scheduleService.getDoctorSchedules(1)).thenReturn(schedules);

        Result<DoctorScheduleVO> result = new DoctorController(scheduleService).getSchedules(1);

        assertEquals("SUCCESS", result.code());
        assertEquals(schedules, result.data());
        verify(scheduleService).getDoctorSchedules(1);
    }

    /**
     * 验证排班调整 Controller 将 DTO 中的余量交给 Service。
     */
    @Test
    void shouldDelegateScheduleCapacityUpdate() {
        ScheduleService scheduleService = mock(ScheduleService.class);
        ScheduleCapacityUpdateDTO request = new ScheduleCapacityUpdateDTO();
        request.setCapacity(0);
        ScheduleCapacityUpdateVO updated = new ScheduleCapacityUpdateVO();
        updated.setId(1);
        updated.setCapacity(0);
        when(scheduleService.updateCapacityAndGetSnapshot(1, 0)).thenReturn(updated);

        Result<ScheduleCapacityUpdateVO> result = new ScheduleController(scheduleService)
            .updateCapacity(1, request);

        assertEquals("SUCCESS", result.code());
        assertEquals(updated, result.data());
        verify(scheduleService).updateCapacityAndGetSnapshot(1, 0);
    }
}
