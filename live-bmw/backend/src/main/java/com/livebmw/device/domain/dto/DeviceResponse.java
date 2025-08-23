package com.livebmw.device.domain.dto;

import java.time.Instant;

public record DeviceResponse(
        String deviceId,
        Instant firstSeen,
        Instant lastSeen,
        String userAgent,
        String remoteIp
) {}