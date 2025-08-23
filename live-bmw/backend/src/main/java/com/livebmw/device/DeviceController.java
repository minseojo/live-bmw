package com.livebmw.device;

import com.livebmw.device.domain.Device;
import com.livebmw.device.domain.dto.DeviceSaveRequest;
import com.livebmw.device.domain.dto.DeviceResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<DeviceResponse> register(
            @Valid @RequestBody DeviceSaveRequest request,
            HttpServletRequest httpRequest) {

        Device newDevice = deviceService.registerNew(request, httpRequest);

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
