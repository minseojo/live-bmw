package com.livebmw.device;

import com.livebmw.device.domain.Device;
import com.livebmw.device.domain.dto.DeviceSaveRequest;
import com.livebmw.device.domain.dto.DeviceResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody DeviceSaveRequest request, HttpServletRequest httpServletRequest) {
        if (request.deviceId() == null || request.deviceId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","deviceId is required"));
        }

        Device newDevice = deviceService.registerNew(request, httpServletRequest);

        DeviceResponse response = new DeviceResponse(
                newDevice.getDeviceId(),
                newDevice.getFirstSeen(),
                newDevice.getLastSeen(),
                newDevice.getUserAgent(),
                newDevice.getRemoteIp()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<?> get(@PathVariable String deviceId, HttpServletRequest httpServletRequest) {
        var device = deviceService.getDeviceById(deviceId, httpServletRequest);

        return ResponseEntity.ok().body(device);
    }

}
