import React from "react";
import { ArrivalCard } from "./ArrivalCard";

// keyStation 그룹을 심플한 카드 스택으로 표시
function KeyStationGroup({ stationName, direction, arrivals }) {
    const label = `${stationName} • ${direction}`;
    return (
        <div style={{ marginTop: 12 }}>
            <div style={{ fontWeight: 700, marginBottom: 8 }}>{label}</div>
            <div style={{ display: "grid", gap: 10 }}>
                {(arrivals || []).slice(0, 3).map((a, idx) => (
                    <ArrivalCard key={`${stationName}-${direction}-${idx}`} arrivalItem={a} contextLabel={label} />
                ))}
                {(!arrivals || arrivals.length === 0) && (
                    <div style={{ padding: 12, border: "1px dashed #e5e7eb", borderRadius: 10, color: "#6b7280" }}>
                        도착 정보가 없습니다
                    </div>
                )}
            </div>
        </div>
    );
}

export function RouteSummary({ routeResponse, shortestPath }) {
    // 기존 단일 라우트 요약 표시 (호환)
    const showLegacy = !!routeResponse;
    const showShortest = !!shortestPath;
    if (!showLegacy && !showShortest) return null;

    return (
        <div style={{ marginTop: 16, padding: 12, border: "1px solid #e5e7eb", borderRadius: 14, background: "#fff" }}>
            {showLegacy && (
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                    <strong>{routeResponse.lineName}</strong>
                    <span style={{ color: "#6b7280" }}>{routeResponse.lineId}</span>
                    <span style={{ color: "#9ca3af" }}>•</span>
                    <span>{routeResponse.direction}</span>
                    <span style={{ color: "#9ca3af" }}>•</span>
                    <span>{routeResponse.originStation} → {routeResponse.destinationStation}</span>
                    <span style={{ marginLeft: "auto", color: "#374151" }}>다음 역 <b>{routeResponse.nextStation}</b></span>
                </div>
            )}

            {showShortest && (
                <div style={{ marginTop: showLegacy ? 16 : 0 }}>
                    {(shortestPath.groups || []).map(g => (
                        <KeyStationGroup key={`${g.stationName}-${g.direction}`} stationName={g.stationName} direction={g.direction} arrivals={g.arrivals} />
                    ))}
                </div>
            )}
        </div>
    );
}