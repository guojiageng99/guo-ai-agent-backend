package com.guo.guoaiagentbackend.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.guo.guoaiagentbackend.common.BaseResponse;
import com.guo.guoaiagentbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(ErrorCode.PARAMS_ERROR.getMessage());
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, msg);
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage(), e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public BaseResponse<?> cannotGetJdbcConnectionExceptionHandler(CannotGetJdbcConnectionException e) {
        log.error("CannotGetJdbcConnectionException: 无法获取数据库连接", e);
        Throwable cause = e.getCause();
        String message = "无法获取数据库连接";

        if (cause instanceof SQLException sqlException) {
            String sqlMessage = sqlException.getMessage();

            if (sqlMessage != null) {
                if (sqlMessage.contains("Access denied")) {
                    message = "数据库访问被拒绝，请检查用户名和密码是否正确";
                } else if (sqlMessage.contains("Communications link failure")) {
                    message = "数据库连接失败，请检查数据库服务是否启动";
                } else if (sqlMessage.contains("Unknown database")) {
                    message = "数据库不存在，请检查数据库名称是否正确";
                } else {
                    message = "数据库连接失败: " + sqlMessage;
                }
            }
        }

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, message);
    }

    @ExceptionHandler(DataAccessException.class)
    public BaseResponse<?> dataAccessExceptionHandler(DataAccessException e) {
        log.error("DataAccessException: 数据访问异常", e);

        Throwable cause = e.getCause();
        String message = "数据库操作失败";

        if (cause instanceof SQLException sqlException) {
            message = "数据库操作失败: " + sqlException.getMessage();
        }

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, message);
    }

    @ExceptionHandler(SQLException.class)
    public BaseResponse<?> sqlExceptionHandler(SQLException e) {
        log.error("SQLException: SQL执行异常", e);
        String message = "数据库操作失败: " + e.getMessage();

        if (e.getMessage() != null && e.getMessage().contains("Access denied")) {
            message = "数据库访问被拒绝，请检查用户名和密码是否正确";
        }

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: JSON解析异常", e);
        String message = "请求参数格式错误";

        Throwable cause = e.getCause();
        if (cause instanceof MismatchedInputException mismatchedInputException) {
            String inputValue = mismatchedInputException.getPathReference();

            if (inputValue != null && inputValue.contains(",")) {
                String trimmed = inputValue.trim();
                if (trimmed.endsWith(",}") || trimmed.endsWith(",]")) {
                    message = "JSON格式错误：请移除末尾的逗号";
                } else {
                    message = "JSON格式错误，请检查请求体格式是否正确";
                }
            } else {
                message = "请求参数格式错误，请确保 Content-Type 为 application/json，且 JSON 格式正确";
            }
        } else if (e.getMessage() != null && e.getMessage().contains("JSON parse error")) {
            message = "JSON解析失败，请检查请求体格式是否正确。错误信息：" + e.getMessage();
        }

        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException: 系统运行时异常", e);
        String message = "系统错误: " + e.getMessage();

        if (e.getMessage() != null) {
            if (e.getMessage().contains("Communications link failure")) {
                message = "数据库连接失败，请检查数据库服务是否启动";
            } else if (e.getMessage().contains("Access denied")) {
                message = "数据库访问被拒绝，请检查用户名和密码是否正确";
            }
        }

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, message);
    }
}
