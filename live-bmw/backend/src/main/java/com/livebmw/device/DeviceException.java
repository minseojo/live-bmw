package com.livebmw.device;

import com.livebmw.common.error.BusinessException;
import com.livebmw.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class DeviceException extends BusinessException {

    /** 디바이스 도메인 에러 정의 (상태/코드/기본메시지) */
    public enum DeviceError {
        DEVICE_ALREADY_EXISTS(HttpStatus.CONFLICT, ErrorCode.DEVICE_ALREADY_EXISTS, "deviceId already exists"),
        DEVICE_NOT_FOUND     (HttpStatus.NOT_FOUND,  ErrorCode.DEVICE_NOT_FOUND,     "device not found");

        public final HttpStatus status;
        public final ErrorCode code;
        public final String defaultMessage;

        DeviceError(HttpStatus status, ErrorCode code, String defaultMessage) {
            this.status = status;
            this.code = code;
            this.defaultMessage = defaultMessage;
        }
    }

    private DeviceException(DeviceError de, String message, Map<String, Object> details) {
        super(de.status, de.code, message != null ? message : de.defaultMessage, details);
    }

    public static DeviceException alreadyExists(String deviceId) {
        return new DeviceException(
                DeviceError.DEVICE_ALREADY_EXISTS,
                "deviceId already exists: " + deviceId,
                Map.of("deviceId", deviceId)
        );
    }

    public static DeviceException notFound(String deviceId) {
        return new DeviceException(
                DeviceError.DEVICE_NOT_FOUND,
                "device not found: " + deviceId,
                Map.of("deviceId", deviceId)
        );
    }
}
