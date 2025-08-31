import React from "react";
import { LineBadge } from "./LineBadge";

function formatEtaKorean(sec) {
    if (sec > 32400) sec -= 32400;
    if (sec == null || Number.isNaN(sec)) return "—";
    if (sec <= 10) return "도착";
    if (sec <= 40) return "곧 도착";
    if (sec < 80)  return "약 1분 내";
    const m = Math.floor(sec / 60), s = sec % 60;
    if (m === 0) return `${s}초 후`;
    if (s === 0) return `${m}분 후`;
    return `${m}분 ${s}초 후`;
}

function getRemainingSeconds(item = {}) {
    const eta =
        (Number.isFinite(item?.etaSeconds) ? item.etaSeconds : null) ??
        (Number.isFinite(item?.etaSec) ? item.etaSec : null) ??
        (Number.isFinite(item?.minutesLeft)
            ? item.minutesLeft * 60 + (Number.isFinite(item?.secondsLeft) ? item.secondsLeft : 0)
            : null);
    return eta == null ? null : Math.max(0, Math.floor(eta));
}

export function ArrivalCard({ arrivalItem = {}, contextLabel }) {
    const lineId   = arrivalItem.lineId;
    const lineName = arrivalItem.lineName;
    const direction = arrivalItem.direction;
    const eta = getRemainingSeconds(arrivalItem);

    const badgeLabel = lineName || lineId || "—";
    const isInner = direction === "내선" || direction === "상행";

    return (
        <div style={{ padding:14, border:"1px solid #e5e7eb", borderRadius:14, background:"#fff", boxShadow:"0 1px 2px rgba(0,0,0,0.03)" }}>
            <div style={{ display:"flex", alignItems:"center", gap:8 }}>
                <LineBadge lineId={lineId} lineName={lineName} label={badgeLabel} />
                <div style={{ fontWeight:700 }}>{contextLabel ?? "도착 정보"}</div>
                <span
                    style={{
                        zIndex: 0,
                        marginLeft:8, fontSize:12, padding:"2px 8px", borderRadius:999,
                        background: isInner ? "#d1fae5" : "#e0e7ff",
                        color:      isInner ? "#065f46" : "#3730a3",
                    }}
                >
          {direction || "—"}
        </span>
            </div>

            <div style={{ marginTop:4, color:"#6b7280", fontSize:14 }}>
                {arrivalItem.trainLineSummary ?? "—"}
            </div>

            <div style={{ marginTop:8, display:"flex", alignItems:"baseline", gap:12 }}>
                <div style={{ fontSize:28, fontWeight:800 }}>{formatEtaKorean(eta)}</div>
            </div>
        </div>
    );
}
