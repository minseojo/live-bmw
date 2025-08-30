package com.livebmw.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        // 래퍼로 감싸야 상태코드/바디 길이 등을 안전하게 읽을 수 있음
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        MDC.put("requestId", requestId);
        try {
            // 들어오는 라인 로깅
            log.info(">>> {} {} CT={} UA={}",
                    req.getMethod(),
                    req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
                    safe(req.getContentType()),
                    trimUa(req.getHeader("User-Agent")));

            filterChain.doFilter(req, res);

            long took = System.currentTimeMillis() - start;
            log.info("<<< {} {} ({} ms, respLen={})",
                    res.getStatus(),
                    req.getRequestURI(),
                    took,
                    res.getContentSize());
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            log.error("xxx {} {} failed ({} ms): {}", req.getMethod(), req.getRequestURI(), took, e.toString(), e);
            throw e;
        } finally {
            // response body를 래퍼에서 실제로 복사해줘야 클라이언트로 나감
            res.copyBodyToResponse();
            MDC.remove("requestId");
        }
    }

    private static String safe(String v) { return v == null ? "-" : v; }

    private static String trimUa(String ua) {
        if (ua == null) return "-";
        return ua.length() > 120 ? ua.substring(0, 120) + "…" : ua;
    }
}
