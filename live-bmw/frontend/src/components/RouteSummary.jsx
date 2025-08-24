import React from "react";

export function RouteSummary({ routeResponse }) {
    if (!routeResponse) return null;
    return (
        <div style={{ marginTop: 16, padding: 12, border: "1px solid #e5e7eb", borderRadius: 14, background: "#fff" }}>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                <strong>{routeResponse.lineName}</strong>
                <span style={{ color: "#6b7280" }}>{routeResponse.lineId}</span>
                <span style={{ color: "#9ca3af" }}>•</span>
                <span>{routeResponse.direction}</span>
                <span style={{ color: "#9ca3af" }}>•</span>
                <span>{routeResponse.originStation} → {routeResponse.destinationStation}</span>
                <span style={{ marginLeft: "auto", color: "#374151" }}>다음 역 <b>{routeResponse.nextStation}</b></span>
            </div>
        </div>
    );
}