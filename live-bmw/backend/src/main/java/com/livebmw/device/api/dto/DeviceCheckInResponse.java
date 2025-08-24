package com.livebmw.device.api.dto;

import com.livebmw.device.domain.model.Device;

import java.time.Instant;

public record DeviceCheckInResponse(
        String deviceId,
        Instant firstSeen,
        Instant lastSeen
) {
    public static DeviceCheckInResponse fromEntity(Device device) {
        return new DeviceCheckInResponse(
                device.getDeviceId(),
                device.getFirstSeen(),
                device.getLastSeen()
        );
    }
}