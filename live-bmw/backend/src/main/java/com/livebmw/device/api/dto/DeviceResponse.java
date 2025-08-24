package com.livebmw.device.api.dto;

import com.livebmw.device.domain.model.Device;

import java.util.ArrayList;
import java.util.List;

public record DeviceResponse(
        String deviceId
) {
    public static DeviceResponse fromEntity(Device device) {
        return new DeviceResponse(device.getDeviceId());
    }

    public static List<DeviceResponse> fromEntities(List<Device> devices) {
        var response = new ArrayList<DeviceResponse>();
        for (Device device : devices) {
            response.add(fromEntity(device));
        }
        return response;
    }
}