package io.github.decadedx.smarthealthcare.service;

import io.github.decadedx.smarthealthcare.entity.Schedule;
import io.github.decadedx.smarthealthcare.vo.DoctorScheduleItemVO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 医生排班流式查询与余量变更测试。
 *
 * @author Dx
 * @version 1.0
 */
@SpringBootTest
class ScheduleServiceTest {

    /** 排班服务。 */
    @Autowired
    private ScheduleService scheduleService;

    /**
     * 验证排班列表经 Stream 转换后包含医生摘要与排班余量。
     */
    @Test
    void shouldFindDoctorScheduleSnapshot() {
        List<DoctorScheduleItemVO> snapshots = scheduleService.findDoctorScheduleSnapshot(1);

        assertFalse(snapshots.isEmpty());
        DoctorScheduleItemVO firstSnapshot = snapshots.get(0);
        assertEquals("钟南", firstSnapshot.getDoctorName());
        assertEquals(1, firstSnapshot.getScheduleId());
        assertNotNull(firstSnapshot.getCapacity());
    }

    /**
     * 验证更新前可取得医生主键，并以原余量回写避免污染初始化数据。
     */
    @Test
    void shouldUpdateCapacityAndReturnDoctorId() {
        Schedule schedule = scheduleService.getById(1);

        assertNotNull(schedule);
        assertEquals(1, scheduleService.updateCapacity(1, schedule.getCapacity()));
    }
}
