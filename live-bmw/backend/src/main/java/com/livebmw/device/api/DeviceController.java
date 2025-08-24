package com.livebmw.device.api;

import com.livebmw.common.util.RequestUtils;
import com.livebmw.device.application.DeviceService;
import com.livebmw.device.api.dto.DeviceCheckInResponse;
import com.livebmw.device.api.dto.DeviceResponse;
import com.livebmw.device.api.dto.DeviceSaveRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> register(
            @Valid @RequestBody DeviceSaveRequest request,
            HttpServletRequest httpRequest) {
  
        final String clientIp = RequestUtils.getClientIp(httpRequest);
        final String userAgent = shortUserAgent(RequestUtils.getUserAgent(httpRequest));
        final String requestDeviceId = safe(request.deviceId()); // null-safe

        // deviceId를 MDC에 넣어 이후 서비스/리포지토리 로그에 자동 포함
        MDC.put("deviceId", requestDeviceId);
        try {
            log.info("register device - deviceId={}, clientIp={}, userAgent={}", requestDeviceId, clientIp, userAgent);
            log.debug("register device (full userAgent) userAgent={}", userAgent);
            var response = deviceService.addDevice(request, clientIp, userAgent, Instant.now());

            log.info("register device success - deviceId={}", response.deviceId());
            return ResponseEntity.ok(response);
        } finally {
            MDC.remove("deviceId");
        }
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> get(@PathVariable String deviceId, HttpServletRequest httpRequest) {
        final String clientIp = RequestUtils.getClientIp(httpRequest);
        final String userAgent =  RequestUtils.getUserAgent(httpRequest);

        MDC.put("deviceId", safe(deviceId));
        try {
            log.info("[GET] device - deviceId={}, clientIp={}, userAgent={}", deviceId, clientIp, shortUserAgent(userAgent));
            var device = deviceService.getDeviceById(deviceId);
            return ResponseEntity.ok().body(device);
        } finally {
            MDC.remove("deviceId");
        }
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevice(HttpServletRequest httpRequest) {
        final String clientIp = RequestUtils.getClientIp(httpRequest);
        final String userAgent = RequestUtils.getUserAgent(httpRequest);
        log.info("[GET] deviceAll - clientIp={}, userAgent={}", clientIp, shortUserAgent(userAgent));

        var response = deviceService.getAllDevice();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{deviceId}/check-in")
    public ResponseEntity<DeviceCheckInResponse> checkIn(@PathVariable String deviceId, HttpServletRequest httpRequest) {
        final String clientIp = RequestUtils.getClientIp(httpRequest);
        final String userAgent = RequestUtils.getUserAgent(httpRequest);
        log.info("[Post] checkIn - deviceId={}, clientIp={}, userAgent={}", deviceId, clientIp, shortUserAgent(userAgent));

        var checkInResponse = deviceService.checkIn(deviceId, clientIp, userAgent, Instant.now());
        return ResponseEntity.ok(checkInResponse);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> delete(@PathVariable String deviceId, HttpServletRequest httpRequest) {
        deviceService.deleteDevice(deviceId);

        final String clientIp = RequestUtils.getClientIp(httpRequest);
        final String userAgent = RequestUtils.getUserAgent(httpRequest);
        log.info("[Delete] device - deviceId={}, clientIp={}, userAgent={}", deviceId, clientIp, shortUserAgent(userAgent));
        return ResponseEntity.noContent().build();
    }

    private static String safe(String v) { return v == null ? "-" : v; }

    private static String shortUserAgent(String userAgent) {
        if (userAgent == null) return "-";
        return userAgent.length() > 120 ? userAgent.substring(0, 120) + "…" : userAgent;
    }
}
