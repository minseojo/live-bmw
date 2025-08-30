import React from "react";
import { useCountdownList } from "../hooks/useCountdownList";
import { ArrivalCard } from "./ArrivalCard";

export function KeyStationArrivals({ stationName, direction, items = [] }) {
    // 각 그룹별 카운트다운 적용
    const arrivals = useCountdownList(items)
        .slice()
        .sort((a, b) => (a.etaSeconds ?? 9e12) - (b.etaSeconds ?? 9e12));

    return (
        <section style={{ display:"grid", gap:10 }}>
            <div style={{ display:"flex", alignItems:"baseline", gap:8 }}>
                <h4 style={{ margin:0, fontWeight:800 }}>{stationName}</h4>
                <span style={{
                    fontSize:12, padding:"2px 8px", borderRadius:999,
                    background: direction === "내선" || direction === "상행" ? "#d1fae5" : "#e0e7ff",
                    color:      direction === "내선" || direction === "상행" ? "#065f46" : "#3730a3",
                }}>
          {direction}
        </span>
            </div>

            {arrivals.length === 0 ? (
                <div style={{ color:"#6b7280" }}>도착 정보가 없습니다.</div>
            ) : (
                <div style={{ display:"grid", gap:10 }}>
                    {arrivals.map((it, idx) => (
                        <ArrivalCard
                            key={it.trainNumber ?? it.trainNo ?? idx}
                            arrivalItem={it}
                            contextLabel="도착 정보"
                        />
                    ))}
                </div>
            )}
        </section>
    );
}
