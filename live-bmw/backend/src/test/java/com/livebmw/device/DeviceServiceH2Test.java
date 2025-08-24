package com.livebmw.device;

import com.livebmw.device.application.DeviceException;
import com.livebmw.device.application.DeviceService;
import com.livebmw.device.domain.model.Device;
import com.livebmw.device.api.dto.DeviceCheckInResponse;
import com.livebmw.device.api.dto.DeviceResponse;
import com.livebmw.device.api.dto.DeviceSaveRequest;
import com.livebmw.device.domain.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class DeviceServiceH2Test {

    String CLIENT_IP = "203.0.113.10";
    String USER_AGENT = "JUnit USER_AGENT";

    @Autowired
    DeviceService deviceService;

    @Autowired
    DeviceRepository deviceRepository;

    @BeforeEach
    void clean() {
        deviceRepository.deleteAll();
    }
    

    @Test
    void addDevice_신규등록과_checkIn까지_성공() {
        // given
        String deviceId = "dev-h2-1";
        DeviceSaveRequest req = new DeviceSaveRequest(deviceId);
        Instant now = Instant.now();

        // when
        DeviceResponse saved = deviceService.addDevice(req, CLIENT_IP, USER_AGENT, now);

        // then
        assertThat(saved.deviceId()).isEqualTo(deviceId);

        Device fromDb = deviceRepository.findByDeviceId(deviceId).orElseThrow();
        assertThat(fromDb.getDeviceId()).isEqualTo(deviceId);

        // addDevice 내부에서 checkIn을 호출하므로 lastSeen/USER_AGENT/IP도 세팅되어 있어야 함
        assertThat(fromDb.getLastSeen()).isEqualTo(now);
        assertThat(fromDb.getRemoteIp()).isEqualTo(CLIENT_IP);
        assertThat(fromDb.getUserAgent()).isEqualTo(USER_AGENT);
    }

    @Test
    void addDevice_중복이면_예외_alreadyExists() {
        // given
        String deviceId = "dup-1";
        deviceRepository.save(new Device(deviceId, CLIENT_IP, USER_AGENT, Instant.now()));

        // when / then
        assertThatThrownBy(() ->
                deviceService.addDevice(new DeviceSaveRequest(deviceId), CLIENT_IP, USER_AGENT, Instant.now()))
                .isInstanceOf(DeviceException.class)
                .hasMessageContaining(deviceId);
    }

    @Test
    void getDeviceById_성공() {
        String deviceId = "dev-get-1";
        deviceRepository.save(new Device(deviceId, CLIENT_IP, USER_AGENT,Instant.now()));

        DeviceResponse got = deviceService.getDeviceById(deviceId);

        assertThat(got.deviceId()).isEqualTo(deviceId);
    }

    @Test
    void getDeviceById_없으면_notFound_예외() {
        assertThatThrownBy(() -> deviceService.getDeviceById("nope"))
                .isInstanceOf(DeviceException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void checkIn_기존_디바이스의_마지막접속_갱신() {
        String deviceId = "dev-check-1";
        Device d = new Device(deviceId, CLIENT_IP, USER_AGENT, Instant.now().minusSeconds(3600));
        deviceRepository.save(d);

        Instant now = Instant.now();
        String CLIENT_IP = "198.51.100.7";
        String USER_AGENT = "USER_AGENT-CheckIn";

        DeviceCheckInResponse updated = deviceService.checkIn(deviceId, CLIENT_IP, USER_AGENT, now);

        assertThat(updated.lastSeen()).isEqualTo(now);

        Device fromDb = deviceRepository.findByDeviceId(deviceId).orElseThrow();
        assertThat(fromDb.getLastSeen()).isEqualTo(now);
        assertThat(fromDb.getRemoteIp()).isEqualTo(CLIENT_IP);
        assertThat(fromDb.getUserAgent()).isEqualTo(USER_AGENT);
    }

    @Test
    void deleteDevice_성공하면_DB에서_사라짐() {
        String deviceId = "dev-del-1";
        deviceRepository.save(new Device(deviceId, CLIENT_IP, USER_AGENT, Instant.now()));

        deviceService.deleteDevice(deviceId);

        assertThat(deviceRepository.findByDeviceId(deviceId)).isEmpty();
    }

    @Test
    void deleteDevice_없으면_notFound_예외() {
        assertThatThrownBy(() -> deviceService.deleteDevice("missing"))
                .isInstanceOf(DeviceException.class)
                .hasMessageContaining("missing");
    }
}
