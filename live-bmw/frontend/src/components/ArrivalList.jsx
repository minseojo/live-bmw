import React from "react";
import { ArrivalCard } from "./ArrivalCard";
import { useCountdownList } from "../hooks/useCountdownList";

export function ArrivalList({ items = [], contextLabel }) {
    // 카운트다운 적용
    const arrivals = useCountdownList(items)
        .slice()
        .sort((a, b) => (a.etaSeconds ?? 9e12) - (b.etaSeconds ?? 9e12)); // 남은 시간 순

    if (!arrivals.length) {
        return <div style={{ color:"#6b7280" }}>도착 정보가 없습니다.</div>;
    }

    return (
        <div style={{ display:"grid", gap:12 }}>
            {arrivals.map((it, idx) => (
                <ArrivalCard key={it.id ?? it.trainId ?? idx} arrivalItem={it} contextLabel={contextLabel} />
            ))}
        </div>
    );
}
