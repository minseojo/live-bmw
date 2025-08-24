import { useEffect, useState } from "react";
import { registerDevice, getDeviceById, deleteDeviceById } from "../api/deviceApi";


/**
 * 로컬 스토리지 deviceId 관리 + 서버 등록/검증 훅
 */
export function useDeviceRegistration() {
    const [registeredDeviceIdentifier, setRegisteredDeviceIdentifier] = useState(null);
    const [deviceRegistrationErrorMessage, setDeviceRegistrationErrorMessage] = useState("");
    const [isDeviceRegistrationInProgress, setIsDeviceRegistrationInProgress] = useState(false);

    useEffect(() => {
        async function initializeOrRepairDeviceRegistration() {
            setIsDeviceRegistrationInProgress(true);
            try {
                let persistedDeviceIdentifier = localStorage.getItem("deviceId");
                if (!persistedDeviceIdentifier) {
                    persistedDeviceIdentifier = crypto.randomUUID();
                    localStorage.setItem("deviceId", persistedDeviceIdentifier);
                }

                // 서버 조회로 유효성 확인
                await getDeviceById(persistedDeviceIdentifier);
                setRegisteredDeviceIdentifier(persistedDeviceIdentifier);
                setDeviceRegistrationErrorMessage("");
            } catch (initializationError) {
                console.warn("기존 deviceId가 유효하지 않아 재발급 시도", initializationError?.message);
                setDeviceRegistrationErrorMessage("기존 deviceId 실패 → 새 deviceId 발급");

                try {
                    await deleteDeviceById(localStorage.getItem("deviceId"));
                } catch (_) {
                }

                const newDeviceIdentifier = crypto.randomUUID();
                localStorage.setItem("deviceId", newDeviceIdentifier);
                try {
                    await registerDevice(newDeviceIdentifier);
                    setRegisteredDeviceIdentifier(newDeviceIdentifier);
                    setDeviceRegistrationErrorMessage("");
                } catch (registrationError) {
                    console.error("새 deviceId 등록 실패", registrationError?.message);
                    setDeviceRegistrationErrorMessage("새 deviceId 등록 실패");
                }
            } finally {
                setIsDeviceRegistrationInProgress(false);
            }
        }

        initializeOrRepairDeviceRegistration();
    }, []);

    return {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    };
}