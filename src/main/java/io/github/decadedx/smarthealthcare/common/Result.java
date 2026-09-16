package io.github.decadedx.smarthealthcare.common;


/**
 * HTTP 接口统一响应包装。
 *
 * @param <T> 成功响应中的业务数据类型
 */
public record Result<T>(String code, String message, T data, String traceId) {

    /**
     * 构造成功响应。
     *
     * @param data 业务响应数据
     * @param <T> 业务响应数据类型
     * @return 统一成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>("SUCCESS", "success", data, null);
    }

    /**
     * 构造失败响应。
     *
     * @param code 项目统一错误码
     * @param message 面向调用方的错误说明
     * @param traceId 请求链路追踪标识
     * @return 统一失败响应
     */
    public static Result<Void> error(String code, String message, String traceId) {
        return new Result<>(code, message, null, traceId);
    }
}
