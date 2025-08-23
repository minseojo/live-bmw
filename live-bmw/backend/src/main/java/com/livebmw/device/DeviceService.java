package com.livebmw.device;

import com.livebmw.device.domain.Device;
import com.livebmw.device.domain.dto.DeviceSaveRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.livebmw.device.DeviceException.alreadyExists;
import static com.livebmw.device.DeviceException.notFound;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /** 새 디바이스 등록 (이미 있으면 409를 유도하기 위해 예외) */
    public Device registerNew(DeviceSaveRequest request, HttpServletRequest httpRequest) {
        Instant now = now();

        // 중복 체크
        if (deviceRepository.findByDeviceId(request.deviceId()).isPresent()) {
            throw alreadyExists(request.deviceId());
        }

        Device device = new Device(request.deviceId(), now);
        updateLastSeen(device, httpRequest, now);
        return deviceRepository.save(device);
    }

    public Device getDeviceById(String deviceId, HttpServletRequest httpRequest) {
        var now = now();
        return updateLastSeen(deviceId, httpRequest, now);
    }

    /** 존재하는 디바이스의 마지막 접속 갱신 */
    public Device updateLastSeen(String deviceId, HttpServletRequest httpRequest, Instant now) {
        Device device = deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> notFound(deviceId));

        return updateLastSeen(device, httpRequest, now);
    }

    public Device updateLastSeen(Device device, HttpServletRequest httpRequest, Instant now) {
        device.updateLastSeen(
                now,
                Optional.ofNullable(httpRequest.getHeader("User-Agent")).orElse("unknown"),
                extractClientIp(httpRequest)
        );

        return deviceRepository.save(device);
    }


    /* ===== 공통 ===== */
    protected Instant now() {
        return Instant.now();
    }

    private String extractClientIp(HttpServletRequest request) {
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP" };
        for (String h : headers) {
            String v = request.getHeader(h);
            if (v != null && !v.isBlank()) return v.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
