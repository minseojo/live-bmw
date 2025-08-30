import React, { useEffect, useRef, useState, useCallback } from "react";
import { get } from "../api/baseApi";
import { getBrowserLocation } from "../hooks/useGeoLocation";  // ← 외부에서 import

async function fetchNearestStations({ lat, lng, limit }) {
    return await get("/api/metro/stations/nearest", { query: { lat, lng, limit } });
}

const INIT_KEY = "once:geo_init"; // 이 탭에서 한 번만 실행

export function UseCurrentLocationButton({
                                             onPick,
                                             onFound,
                                             autoPickOnInit = false,
                                             limit = 3,
                                         }) {
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const runningRef = useRef(false);

    const run = useCallback(async () => {
        if (runningRef.current) return;
        runningRef.current = true;
        setLoading(true);
        setErr("");

        try {
            const coords = await getBrowserLocation();   // ← 외부 함수 사용
            const list = await fetchNearestStations({ ...coords, limit });
            if (!Array.isArray(list) || list.length === 0) {
                throw new Error("근처 역을 찾지 못했습니다.");
            }
            onFound?.(list);
            onPick?.(list[0]);
        } catch (e) {
            setErr(e?.message || "오류가 발생했습니다.");
        } finally {
            setLoading(false);
            runningRef.current = false;
        }
    }, [limit, onFound, onPick]);

    useEffect(() => {
        if (!autoPickOnInit) return;
        if (sessionStorage.getItem(INIT_KEY)) return; // 이미 실행됨
        sessionStorage.setItem(INIT_KEY, "1");
        void run();
    }, [autoPickOnInit, run]);

    return (
        <div style={{ display: "inline-flex", flexDirection: "column" }}>
            <button
                onClick={run}
                disabled={loading}
                className="btn-compact"
                style={{
                    height: 32,
                    padding: "6px 10px",
                    borderRadius: 10,
                    border: "1px solid #e5e7eb",
                    background: "#fff",
                    fontSize: 13,
                    color: "#0f172a",
                    cursor: loading ? "default" : "pointer",
                    opacity: loading ? 0.7 : 1,
                }}
            >
                {loading ? "위치 확인 중…" : "내 주변 역 찾기"}
            </button>
            {err && <div style={{ fontSize: 12, color: "#ef4444", marginTop: 6 }}>{err}</div>}
        </div>
    );
}
