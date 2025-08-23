package com.livebmw.device;

import com.livebmw.device.domain.Device;
import com.livebmw.device.domain.dto.DeviceSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;

    private MockHttpServletRequest makeHttpRequest(String userAgent, String ip) {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("User-Agent", userAgent);
        httpRequest.addHeader("X-Forwarded-For", ip);
        return httpRequest;
    }

    @Test
    void 새_디바이스를_등록하면_DB에_저장되고_필드가_세팅된다() {
        // given
        String deviceId = "func-uuid-1";
        DeviceSaveRequest request = new DeviceSaveRequest(deviceId);
        MockHttpServletRequest httpRequest = makeHttpRequest("JUnit-UA/1.0", "203.0.113.10");

        Instant before = Instant.now();

        // when
        Device saved = deviceService.registerNew(request, httpRequest);

        Instant after = Instant.now();

        // then
        assertNotNull(saved.getId());
        assertEquals(deviceId, saved.getDeviceId());
        assertNotNull(saved.getFirstSeen());
        assertNotNull(saved.getLastSeen());
        assertEquals("JUnit-UA/1.0", saved.getUserAgent());
        assertEquals("203.0.113.10", saved.getRemoteIp());

        // 시간 범위 검증
        assertTrue(!saved.getFirstSeen().isBefore(before) && !saved.getFirstSeen().isAfter(after));
        assertTrue(!saved.getLastSeen().isBefore(before) && !saved.getLastSeen().isAfter(after));
    }


    @Test
    void 존재하지않는_띠바이스를_찾으면_예외가_발생한다() {
        // given
        String deviceId = "not-found-id";
        MockHttpServletRequest httpRequest = makeHttpRequest("UA-1", "203.0.113.9");

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> deviceService.getDeviceById(deviceId, httpRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void 중복된_deviceId로_등록하면_예외가_발생한다() {
        // given
        String deviceId = "dup-id";
        DeviceSaveRequest request = new DeviceSaveRequest(deviceId);
        MockHttpServletRequest httpRequest = makeHttpRequest("UA", "198.51.100.5");

        deviceService.registerNew(request, httpRequest);

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> deviceService.registerNew(new DeviceSaveRequest(deviceId), httpRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));

    }

    @Test
    void updateLastSeen을_호출하면_lastSeen과_UserAgent_IP가_갱신된다() throws InterruptedException {
        // given
        String deviceId = "touch-id";
        MockHttpServletRequest httpRequest1 = makeHttpRequest("UA-1", "192.0.2.1");
        Device created = deviceService.registerNew(new DeviceSaveRequest(deviceId), httpRequest1);

        Thread.sleep(5);

        MockHttpServletRequest httpRequest2 = makeHttpRequest("UA-2", "192.0.2.2");
        Instant before = Instant.now();

        // when
        Device updated = deviceService.updateLastSeen(deviceId, httpRequest2, Instant.now());

        Instant after = Instant.now();

        // then
        assertEquals(created.getFirstSeen(), updated.getFirstSeen());
        assertEquals("UA-2", updated.getUserAgent());
        assertEquals("192.0.2.2", updated.getRemoteIp());
        assertTrue(updated.getLastSeen().isAfter(created.getLastSeen()));
        assertTrue(!updated.getLastSeen().isBefore(before) && !updated.getLastSeen().isAfter(after));
    }

    @Test
    void getDeviceById를_호출하면_엔티티가_반환되고_lastSeen이_갱신된다() throws InterruptedException {
        // given
        String deviceId = "get-id";
        MockHttpServletRequest httpRequest1 = makeHttpRequest("UA-1", "203.0.113.9");
        Device created = deviceService.registerNew(new DeviceSaveRequest(deviceId), httpRequest1);

        Thread.sleep(5);

        MockHttpServletRequest httpRequest2 = makeHttpRequest("UA-2", "203.0.113.10");
        Instant before = Instant.now();

        // when
        Device found = deviceService.getDeviceById(deviceId, httpRequest2);

        Instant after = Instant.now();

        // then
        assertEquals(deviceId, found.getDeviceId());
        assertEquals("UA-2", found.getUserAgent());
        assertEquals("203.0.113.10", found.getRemoteIp());
        assertTrue(found.getLastSeen().isAfter(created.getLastSeen()));
        assertTrue(!found.getLastSeen().isBefore(before) && !found.getLastSeen().isAfter(after));
    }
}
