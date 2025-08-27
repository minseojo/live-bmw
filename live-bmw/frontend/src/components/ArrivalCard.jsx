import React from "react";

const LINE2_COLOR = "#00A84D"; // 2호선 공식 컬러

// ✅ 2호선 판별 (코드 "1002" 또는 이름에 "2호선")
function isLine2(idLike, nameLike) {
    const s1 = String(idLike ?? "");
    const s2 = String(nameLike ?? "");
    return s1 === "1002" || /2호선/.test(s2);
}

function formatEtaKorean(remainingSeconds) {
    if (remainingSeconds == null || Number.isNaN(remainingSeconds)) return "—";
    if (remainingSeconds <= 0) return "도착";
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    if (minutes === 0) return `${seconds}초 후`;
    if (seconds === 0) return `${minutes}분 후`;
    return `${minutes}분 ${seconds}초 후`;
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
    const lineId         = arrivalItem.lineId;                     // "1002"
    const lineName       = arrivalItem.lineName;                   // 있으면 활용
    const direction      = arrivalItem.direction;
    const trainLine      = contextLabel ?? "도착 정보";
    const trainLineSummary = arrivalItem.trainLineSummary ?? "";
    // const receivedAt     = arrivalItem.receivedAt ?? "—";
    const eta            = getRemainingSeconds(arrivalItem);

    const isInner        = direction === "내선" || direction === "상행";

    const isL2 = isLine2(lineId, lineName);
    const badgeBg = isL2 ? "rgba(0,168,77,0.12)" : "#f1f5f9";
    const badgeFg = isL2 ? LINE2_COLOR : "#334155";
    const badgeBd = isL2 ? "rgba(0,168,77,.28)" : "#e2e8f0";
    const badgeDot = isL2 ? LINE2_COLOR : "#94a3b8";
    const badgeLabel = lineName || lineId || "—";

    return (
        <div style={{ padding: 14, border: "1px solid #e5e7eb", borderRadius: 14, background: "#fff", boxShadow: "0 1px 2px rgba(0,0,0,0.03)" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{
                        display: "inline-flex", alignItems: "center", gap: 6,
                        padding: "4px 8px", borderRadius: 999, fontSize: 12, fontWeight: 700,
                        background: badgeBg, color: badgeFg, border: `1px solid ${badgeBd}`}}>
                    <span style={{ width: 8, height: 8, borderRadius: 999, background: badgeDot }} />{badgeLabel}
                </span>

                <div style={{ fontWeight: 700 }}>{trainLine}</div>

                <span style={{
                    marginLeft: 8, fontSize: 12, padding: "2px 8px", borderRadius: 999,
                    background: isInner ? "#d1fae5" : "#e0e7ff",
                    color: isInner ? "#065f46" : "#3730a3"
                        }}>
                  {direction || "—"}
                </span>
            </div>

            <div style={{ marginTop: 4, color: "#6b7280", fontSize: 14 }}>
                {trainLineSummary || "—"}
            </div>

            <div style={{ marginTop: 8, display: "flex", alignItems: "baseline", gap: 12 }}>
                <div style={{ fontSize: 28, fontWeight: 800 }}>{formatEtaKorean(eta)}</div>
            </div>
        </div>
    );
}
