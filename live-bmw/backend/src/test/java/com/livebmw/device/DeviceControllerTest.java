package com.livebmw.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livebmw.device.api.DeviceController;
import com.livebmw.device.application.DeviceService;
import com.livebmw.device.domain.model.Device;
import com.livebmw.device.api.dto.DeviceResponse;
import com.livebmw.device.api.dto.DeviceSaveRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    String CLIENT_IP = "203.0.113.10";
    String USER_AGENT = "JUnit USER_AGENT";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    DeviceService deviceService;

    @Test
    void register_성공하면_200_과_deviceId반환() throws Exception {
        String deviceId = "dev-abc";
        DeviceSaveRequest req = new DeviceSaveRequest(deviceId);

        when(deviceService.addDevice(eq(req), anyString(), anyString(), any()))
                .thenReturn(DeviceResponse.fromEntity(new Device(deviceId, CLIENT_IP, USER_AGENT, Instant.now())));

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req))
                        .header("User-Agent", "JUnit UA")
                        .header("X-Forwarded-For", "203.0.113.10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(deviceId));
    }

    @Test
    void get_단건조회_200() throws Exception {
        String deviceId = "dev-1";
        Device device = new Device(deviceId, CLIENT_IP, USER_AGENT, Instant.now());

        when(deviceService.getDeviceById(deviceId)).thenReturn(DeviceResponse.fromEntity(device));

        mockMvc.perform(get("/api/devices/{deviceId}", deviceId)
                        .header("User-Agent", "UA")
                        .header("X-Forwarded-For", "198.51.100.5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        // 반환 JSON 구조는 Device 직렬화 형태에 따라 달라집니다.
        // 필요 시 jsonPath로 상세 필드 검증을 추가하세요.
    }

    @Test
    void getDevices_목록조회_200() throws Exception {
        List<Device> list = List.of(
                new Device("d1", CLIENT_IP, USER_AGENT, Instant.now()),
                new Device("d2", CLIENT_IP, USER_AGENT, Instant.now())
        );
        when(deviceService.getAllDevice()).thenReturn(DeviceResponse.fromEntities(list));

        mockMvc.perform(get("/api/devices")
                        .header("User-Agent", "UA")
                        .header("X-Forwarded-For", "192.0.2.1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void delete_204() throws Exception {
        Mockito.doNothing().when(deviceService).deleteDevice("del-1");

        mockMvc.perform(delete("/api/devices/{deviceId}", "del-1"))
                .andExpect(status().isNoContent());
    }
}
