import React, { useEffect, useState } from "react";
import { ArrivalList } from "../components/ArrivalList";
import { fetchMetroArrivals } from "../api/metroApi";

export function ArrivalsSection({ stationName }) {
    const [raw, setRaw] = useState([]);
    const [loading, setLoading] = useState(false);

    // 최초/역 변경 시 API 호출
    useEffect(() => {
        let on = true;
        setLoading(true);
        (async () => {
            try {
                const list = await fetchMetroArrivals(stationName);
                if (on) setRaw(list);
            } finally {
                if (on) setLoading(false);
            }
        })();
        return () => { on = false; };
    }, [stationName]);

    // 주기 갱신(선택사항: 20~30초 등)
    useEffect(() => {
        let on = true;
        const id = setInterval(async () => {
            try {
                const list = await fetchMetroArrivals(stationName);
                if (on) setRaw(list);
            } catch {}
        }, 20000);
        return () => { on = false; clearInterval(id); };
    }, [stationName]);

    return (
        <section style={{ display:"grid", gap:12 }}>
            <div style={{ display:"flex", alignItems:"center", gap:10 }}>
                <h3 style={{ fontWeight:800, margin:0 }}>{stationName} 도착 정보</h3>
                {loading && <span style={{ fontSize:12, color:"#6b7280" }}>갱신 중…</span>}
            </div>
            <ArrivalList items={raw} contextLabel="도착 정보" />
        </section>
    );
}
