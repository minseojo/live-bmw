import React, { useEffect, useState } from "react";
import { fetchShortestPathWithArrivals } from "../api/metroApi";
import { KeyStationArrivals } from "./KeyStationArrivals";

export function RouteArrivalsSection({ fromName, toName, searchType = "duration", when }) {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(false);

    // 최초/입력 변경 시 호출
    useEffect(() => {
        if (!fromName || !toName) return;
        let on = true;
        setLoading(true);
        (async () => {
            try {
                const res = await fetchShortestPathWithArrivals({ from: fromName, to: toName, when, searchType });
                if (on) setGroups(res.groups ?? []);
            } finally {
                if (on) setLoading(false);
            }
        })();
        return () => { on = false; };
    }, [fromName, toName, searchType, when]);

    // 선택: 20초 주기 갱신
    useEffect(() => {
        if (!fromName || !toName) return;
        let on = true;
        const id = setInterval(async () => {
            try {
                const res = await fetchShortestPathWithArrivals({ from: fromName, to: toName, when, searchType });
                if (on) setGroups(res.groups ?? []);
            } catch {}
        }, 20000);
        return () => { on = false; clearInterval(id); };
    }, [fromName, toName, searchType, when]);

    return (
        <section className="panel">
            <div className="mt-6">
                <div className="muted plan-label">
                    {searchType === "duration" ? "최소시간" : searchType === "distance" ? "최단거리" : "최소환승"} 경로
                    {loading && <span style={{ marginLeft: 8, fontSize: 12, color: "#6b7280" }}>갱신 중…</span>}
                </div>

                {groups.length === 0 ? (
                    <div className="arrival-empty">도착 정보가 없습니다</div>
                ) : (
                    <div className="mt-14" style={{ display: "grid", gap: 24 }}>
                        {groups.map((g, idx) => (
                            <KeyStationArrivals
                                key={`${g.stationName}-${g.direction}-${idx}`}
                                stationName={g.stationName}
                                direction={g.direction}
                                items={g.arrivals || []}
                            />
                        ))}
                    </div>
                )}
            </div>
        </section>
    );
}
