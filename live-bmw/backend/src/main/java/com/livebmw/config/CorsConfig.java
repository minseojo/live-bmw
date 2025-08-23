package com.livebmw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age-seconds}")
    private long maxAgeSeconds;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Origin 설정
        Stream.of(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(corsConfig::addAllowedOrigin);

        // 와일드카드 패턴 (예: https://*.vercel.app)
        if (allowedOriginPatterns != null && !allowedOriginPatterns.isBlank()) {
            // ★ allowCredentials=true인 경우 와일드카드/패턴은 브라우저가 거부합니다.
            Stream.of(allowedOriginPatterns.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(corsConfig::addAllowedOriginPattern);
        }

        // Methods/Headers
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.addAllowedHeader(CorsConfiguration.ALL);

        // 인증/쿠키
        corsConfig.setAllowCredentials(allowCredentials);

        // preflight 캐시 시간
        corsConfig.setMaxAge(Duration.ofSeconds(maxAgeSeconds));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
}
