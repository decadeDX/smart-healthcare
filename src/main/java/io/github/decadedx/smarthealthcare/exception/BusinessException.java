package io.github.decadedx.smarthealthcare.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 表示可安全暴露给调用方的业务失败，并携带统一响应所需的状态语义。
 */
public class BusinessException extends RuntimeException {

    /** 对应 HTTP 状态。
     * -- GETTER --
     *  获取对应 HTTP 状态。
     *
     * @return HTTP 状态
     */
    @Getter
    private final HttpStatus status;

    /**
     * 创建业务异常。
     *
     * @param status 对应 HTTP 状态
     * @param message 对外错误消息
     */
    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * 获取与 HTTP 状态一致的对外数字错误码。
     *
     * @return HTTP 数字状态码
     */
    public int getCode() {
        return status.value();
    }
}
