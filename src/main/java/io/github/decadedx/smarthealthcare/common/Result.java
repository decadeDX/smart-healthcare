package io.github.decadedx.smarthealthcare.common;


/**
 * HTTP 接口统一响应包装。
 *
 * @param <T> 成功响应中的业务数据类型
 */
public record Result<T>(int code, String message, T data, String traceId) {

    /**
     * 构造成功响应。
     *
     * @param data 业务响应数据
     * @param <T> 业务响应数据类型
     * @return 统一成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data, null);
    }

    /**
     * 构造失败响应。
     *
     * @param code 与 HTTP 状态一致的数字状态码
     * @param message 面向调用方的错误说明
     * @param traceId 请求链路追踪标识
     * @return 统一失败响应
     */
    public static Result<Void> error(int code, String message, String traceId) {
        return new Result<>(code, message, null, traceId);
    }
}
