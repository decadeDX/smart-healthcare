package io.github.decadedx.smarthealthcare.exception;

import io.github.decadedx.smarthealthcare.common.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将参数校验、业务异常和未预期异常统一转换为不泄露内部实现的 HTTP 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 转换业务层已分类的异常。
     *
     * @param exception 业务异常
     * @return 包含 traceId 的统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(Result.error(exception.getCode(), exception.getMessage(), traceId()));
    }

    /**
     * 转换路径参数等方法级 Bean Validation 失败。
     *
     * @param exception 参数约束异常
     * @return 400 统一错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return badRequest(exception.getMessage());
    }

    /**
     * 转换请求体 DTO 校验失败。
     *
     * @param exception 请求体校验异常
     * @return 400 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() == null
                ? "请求参数不合法"
                : exception.getBindingResult().getFieldError().getDefaultMessage();
        return badRequest(message);
    }

    /**
     * 兜底处理未分类的服务端异常，避免将堆栈、SQL 或基础设施地址暴露给客户端。
     *
     * @param exception 未预期异常
     * @return 500 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("INTERNAL_ERROR", "服务处理失败", traceId()));
    }

    /**
     * 构造参数错误响应。
     *
     * @param message 参数错误说明
     * @return 400 统一错误响应
     */
    private ResponseEntity<Result<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Result.error("INVALID_PARAMETER", message, traceId()));
    }

    /**
     * 创建单次请求错误响应的链路标识。
     *
     * @return 不包含业务敏感信息的追踪标识
     */
    private String traceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
