package com.livebmw.device.domain.dto;

import java.time.Instant;

public record DeviceCheckInResponse(
        String deviceId,
        Instant firstSeen,
        Instant lastSeen
) { }