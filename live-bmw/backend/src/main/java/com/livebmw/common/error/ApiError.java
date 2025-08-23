package com.livebmw.common.error;

import java.time.Instant;
import java.util.Map;

/**
 * 클라이언트 응답용 DTO
 */

public record ApiError(
        int status,               // HTTP status code (e.g., 404)
        String code,              // 도메인별 에러 코드 (e.g., DEVICE_NOT_FOUND)
        String message,           // 에러 메시지
        String path,              // 요청 path
        Instant timestamp,        // 발생 시각
        Map<String, Object> details // 추가 데이터(선택)
) {
    public static ApiError of(int status, String code, String message, String path, Map<String, Object> details) {
        return new ApiError(status, code, message, path, Instant.now(), details);
    }

    public static ApiError simple(int status, String code, String message, String path) {
        return of(status, code, message, path, null);
    }
}
