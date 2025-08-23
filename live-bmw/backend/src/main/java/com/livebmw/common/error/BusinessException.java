package com.livebmw.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 *  모든 비즈니스 예외의 루트
 */

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode code;
    private final Map<String, Object> details;

    public BusinessException(HttpStatus status, ErrorCode code, String message) {
        this(status, code, message, null);
    }

    public BusinessException(HttpStatus status, ErrorCode code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

}
