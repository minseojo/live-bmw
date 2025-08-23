package com.livebmw.device.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_device_device_id", columnList = "deviceId", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 클라이언트가 생성하는 UUID or 토큰
     * - UUID: 36자리 (하이픈 포함), 또는 32자리 (하이픈 제거)
     * - Firebase token 등 외부 토큰까지 고려하면 더 길게
     */
    @Column(nullable = false, unique = true, length = 128)
    private String deviceId;

    /**
     * User-Agent 헤더
     * - 브라우저/앱 UA는 길 수 있음 (최대 255~512 정도 잡는게 안전)
     * - 너무 길 경우 잘라서 저장하는 것도 고려
     */
    @Column(length = 512)
    private String userAgent;

    /**
     * IPv4/IPv6 주소
     * - VARCHAR(45): IPv6 full 표현 최대 길이 (39자) + 여유
     * - RDBMS가 지원하면 INET 타입 권장 (PostgreSQL 등)
     */
    @Column(length = 45)
    private String remoteIp;

    /** 최초 접속 시각 */
    @Column(nullable = false, updatable = false)
    private Instant firstSeen;

    /** 마지막 접속 시각 */
    @Column(nullable = false)
    private Instant lastSeen;

    public Device(String deviceId, Instant firstSeen) {
        this.deviceId = deviceId;
        this.firstSeen = firstSeen;
        this.lastSeen = firstSeen;
    }

    public void updateLastSeen(Instant when, String userAgent, String remoteIp) {
        this.lastSeen = when;
        this.userAgent = userAgent;
        this.remoteIp = remoteIp;
    }
}
