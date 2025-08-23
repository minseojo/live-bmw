package com.livebmw.common.web;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        try {
            MDC.put("requestId", requestId);
            chain.doFilter(request, response);
        } finally {
            long tookMs = System.currentTimeMillis() - start;
            MDC.put("tookMs", String.valueOf(tookMs));
            MDC.remove("tookMs");
            MDC.remove("requestId");
        }
    }
}
