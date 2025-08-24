package com.livebmw.device.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceSaveRequest(
        @NotBlank(message = "deviceId는 비워둘 수 없습니다.")
        String deviceId
) {}