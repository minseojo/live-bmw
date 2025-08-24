package com.livebmw.device.application;

import com.livebmw.device.domain.model.Device;
import com.livebmw.device.api.dto.DeviceCheckInResponse;
import com.livebmw.device.api.dto.DeviceResponse;
import com.livebmw.device.api.dto.DeviceSaveRequest;
import com.livebmw.device.domain.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.livebmw.device.application.DeviceException.alreadyExists;
import static com.livebmw.device.application.DeviceException.notFound;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /** 새 디바이스 등록 (이미 있으면 409를 유도하기 위해 예외) */
    public DeviceResponse addDevice(DeviceSaveRequest request, String clientIp, String userAgent, Instant now) {
        // 중복 체크
        if (deviceRepository.findByDeviceId(request.deviceId()).isPresent()) {
            throw alreadyExists(request.deviceId());
        }

        Device device = new Device(request.deviceId(), clientIp, userAgent, now);
        return DeviceResponse.fromEntity(deviceRepository.save(device));
    }

    public DeviceResponse getDeviceById(String deviceId) {
        return DeviceResponse.fromEntity(
                deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> notFound(deviceId)));
    }

    public List<DeviceResponse> getAllDevice() {
        return DeviceResponse.fromEntities(deviceRepository.findAll());
    }

    /** 존재하는 디바이스의 마지막 접속 갱신 */
    public DeviceCheckInResponse checkIn(String deviceId, String clientIp, String userAgent, Instant now) {
        Device device = deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> notFound(deviceId));
        device.updateLastSeen(clientIp, userAgent, now);
        return DeviceCheckInResponse.fromEntity(deviceRepository.save(device));
    }

    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> notFound(deviceId));
        deviceRepository.delete(device);
    }

}
