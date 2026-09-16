package io.github.decadedx.smarthealthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * 表示可安全暴露给调用方的业务失败，并携带统一响应所需的状态语义。
 */
public class BusinessException extends RuntimeException {

    /** 对应 HTTP 状态。 */
    private final HttpStatus status;

    /** 项目统一错误码；具体编码由后续错误码注册表收敛。 */
    private final String code;

    /**
     * 创建业务异常。
     *
     * @param status 对应 HTTP 状态
     * @param code 对外错误码
     * @param message 对外错误消息
     */
    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /**
     * 获取对应 HTTP 状态。
     *
     * @return HTTP 状态
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * 获取对外错误码。
     *
     * @return 项目统一错误码
     */
    public String getCode() {
        return code;
    }
}
