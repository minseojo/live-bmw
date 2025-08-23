package com.livebmw.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* 1) 프로젝트 표준 비즈니스 예외 -> ApiError 로 변환 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex,
                                                            HttpServletRequest req) {
        ApiError body = ApiError.of(
                ex.getStatus().value(),
                ex.getCode().name(),
                ex.getMessage(),
                req.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    /* 2) 스프링/DB 예외 →  (프로젝트 표준으로 변환) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException ex,
                                                   HttpServletRequest req) {
        ApiError body = ApiError.simple(
                HttpStatus.CONFLICT.value(),
                ErrorCode.DATA_INTEGRITY_VIOLATION.name(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex,
                                                   HttpServletRequest req) {
        ApiError body = ApiError.simple(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.NOT_FOUND.name(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex,
                                                     HttpServletRequest req) {
        ApiError body = ApiError.simple(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_ARGUMENT.name(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /* 3) Bean Validation (@Valid) 에러를 예쁘게 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                details.put(err.getField(), err.getDefaultMessage())
        );

        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_ARGUMENT.name(),
                "Validation failed",
                req.getRequestURI(),
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    /* 4) 그 외 모든 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex,
                                                HttpServletRequest req) {
        ApiError body = ApiError.simple(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR.name(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
