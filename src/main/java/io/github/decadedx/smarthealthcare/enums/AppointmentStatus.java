package io.github.decadedx.smarthealthcare.enums;

import java.util.Arrays;

/**
 * 挂号单允许持久化和对外展示的业务状态。
 */
public enum AppointmentStatus {

    /** 已预约，尚未取消或就诊。 */
    BOOKED,

    /** 已取消预约。 */
    CANCELLED,

    /** 已完成就诊。 */
    VISITED;

    /**
     * 判断数据库中的状态值是否属于当前业务支持范围。
     *
     * @param value 数据库存储的状态文本
     * @return 合法状态返回 true
     */
    public static boolean isSupported(String value) {
        return Arrays.stream(values()).anyMatch(status -> status.name().equals(value));
    }
}
