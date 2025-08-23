import { useEffect, useState } from "react";
import "./App.css";

export default function App() {
    const [deviceId, setDeviceId] = useState(null);

    useEffect(() => {
        // 1. 로컬스토리지 확인
        let id = localStorage.getItem("deviceId");
        if (!id) {
            // 2. 없으면 새로 생성
            id = crypto.randomUUID(); // 브라우저 내장 UUID 생성기
            localStorage.setItem("deviceId", id);

            // 3. 서버에 등록 요청
            fetch("https://your-server.com/api/register-device", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ deviceId: id }),
            }).catch((err) => console.error("서버 등록 실패:", err));
        } else {
            // 4. 기존 id가 있으면 서버에 체크인(or 요청) 보내기
            fetch(`https://your-server.com/api/ping?deviceId=${id}`).catch((err) =>
                console.error("서버 요청 실패:", err)
            );
        }
        setDeviceId(id);
    }, []);

    return (
        <div style={{ padding: 20 }}>
            <h1>Live BMW</h1>
            <p>이 디바이스 ID: {deviceId}</p>
        </div>
    );
}