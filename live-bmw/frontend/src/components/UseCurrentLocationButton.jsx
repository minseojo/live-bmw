import React, { useEffect, useState } from "react";
import { get } from "../api/baseApi";

export function UseCurrentLocationButton({ onPick, onFound, autoPickOnMount = false }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    async function useHere() {
        setLoading(true);
        setError(null);
        if (!("geolocation" in navigator)) {
            setError("이 기기에서 위치를 사용할 수 없어요");
            setLoading(false);
            return;
        }
        navigator.geolocation.getCurrentPosition(async (pos) => {
            try {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;
                const data = await get('/api/metro/stations/nearest', { query: { lat, lng, limit: 3 } });
                if (!Array.isArray(data) || data.length === 0) {
                    setError("근처 역을 찾지 못했습니다.");
                } else {
                    onFound?.(data);      // 전체 목록 부모에게 전달
                    onPick?.(data[0]);    // 가장 가까운 역 자동 선택
                }
            } catch (e) {
                setError(e?.message || "서버 응답 오류");
            } finally {
                setLoading(false);
            }
        }, () => {
            setError("위치 권한을 허용해주세요");
            setLoading(false);
        }, { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 });
    }

    useEffect(() => {
        if (autoPickOnMount) useHere();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div style={{ display: "inline-flex", flexDirection: "column" }}>
            <button onClick={useHere} disabled={loading} style={buttonStyle} className="btn-compact">
                {loading ? "위치 확인 중…" : "내 주변 역 찾기"}
            </button>
            {error && <div style={errorStyle}>{error}</div>}
        </div>
    );
}

const buttonStyle = {
    height: 32,
    padding: "6px 10px",
    borderRadius: 10,
    border: "1px solid #e5e7eb",
    background: "#fff",
    fontSize: 13,
    color: "#0f172a",
    cursor: "pointer",
};

const errorStyle = {
    fontSize: 12,
    color: "#ef4444",
    marginTop: 6,
};
