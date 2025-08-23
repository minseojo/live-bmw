package com.livebmw.common.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class RequestUtils {

    private RequestUtils() {}

    /** User-Agent 헤더 추출 */
    public static String getUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"))
                .filter(s -> !s.isBlank())
                .orElse("unknown");
    }

    /** 클라이언트 IP 추출 */
    public static String getClientIp(HttpServletRequest request) {
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP" };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
