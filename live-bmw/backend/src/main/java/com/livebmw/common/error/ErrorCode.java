package com.livebmw.common.error;

public enum ErrorCode {
    // 공통
    INVALID_ARGUMENT,
    NOT_FOUND,
    INTERNAL_ERROR,
    DATA_INTEGRITY_VIOLATION,

    // 디바이스 도메인 등 도메인별 코드
    DEVICE_ALREADY_EXISTS,
    DEVICE_NOT_FOUND
}