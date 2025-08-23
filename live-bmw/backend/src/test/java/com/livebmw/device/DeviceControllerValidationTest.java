package com.livebmw.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livebmw.common.error.GlobalExceptionHandler;
import com.livebmw.device.domain.Device;
import com.livebmw.device.domain.dto.DeviceSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
@Import(GlobalExceptionHandler.class) // ApiError로 변환되도록 핸들러 등록
class DeviceControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean                   // ← 컨트롤러가 의존하는 서비스 목킹
    private DeviceService deviceService;

    @Test
    void deviceId가_비어있으면_400과_검증에러를_반환한다() throws Exception {
        // given: 공백 → @NotBlank 위반
        DeviceSaveRequest invalidRequest = new DeviceSaveRequest("");

        // when & then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.details.deviceId").value("deviceId는 비워둘 수 없습니다."));
    }

    @Test
    void deviceId가_스페이스면_400과_검증에러를_반환한다() throws Exception {
        // given: 공백 → @NotBlank 위반
        DeviceSaveRequest invalidRequest = new DeviceSaveRequest(" ");

        // when & then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.details.deviceId").value("deviceId는 비워둘 수 없습니다."));
    }

    @Test
    void 올바른_deviceId면_등록되고_200_OK를_반환한다() throws Exception {
        // given
        DeviceSaveRequest validRequest = new DeviceSaveRequest("uuid-1234");
        Device fakeDevice = new Device("uuid-1234", Instant.now());
        when(deviceService.registerNew(any(DeviceSaveRequest.class), any()))
                .thenReturn(fakeDevice);

        // when & then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("uuid-1234"));
    }
}
