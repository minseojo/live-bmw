import { useEffect, useState } from "react";
import {registerDevice, getDevice, deleteDevice} from "./api";

export default function App() {
    const [deviceId, setDeviceId] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function initDevice() {
            let deviceId = localStorage.getItem("deviceId");
            if (!deviceId) {
                deviceId = crypto.randomUUID();
                localStorage.setItem("deviceId", deviceId);
            }

            try {
                await getDevice(deviceId);
                setDeviceId(deviceId);
            } catch (e) {
                console.error("등록 실패, 기존 deviceId 삭제 후 재발급", e.message);
                setError("기존 deviceId 실패 → 새 deviceId 발급");
                try {
                    // 1) 서버에 삭제 요청
                    await deleteDevice(deviceId);
                } catch (delErr) {
                    console.warn("서버 삭제 실패(무시 가능):", delErr.message);
                }

                // 2) localStorage 비우고 새 deviceId 발급
                localStorage.removeItem("deviceId");
                const newDeviceId = crypto.randomUUID();
                localStorage.setItem("deviceId", newDeviceId);

                try {
                    await registerDevice(newDeviceId);
                    setDeviceId(newDeviceId);
                    setError(null);
                } catch (e2) {
                    console.error("새 deviceId 등록도 실패:", e2.message);
                    setError("새 deviceId 등록 실패");
                }
            }
        }

        initDevice();
    }, []);

    return (
        <div style={{ padding: 20 }}>
            <h1>Live BMW</h1>
            {error ? (
                <p style={{ color: "red" }}>{error}</p>
            ) : (
                <p>디바이스 deviceId: {deviceId}</p>
            )}
        </div>
    );
}
