import React, { useState, useEffect } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { fetchShortestPathWithArrivals, fetchMultiCriteriaShortestPaths } from "./api/metroApi";
import { ArrivalCard } from "./components/ArrivalCard";
import { UseCurrentLocationButton } from "./components/UseCurrentLocationButton";

export default function App() {
    const {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    } = useDeviceRegistration();

    const [originStationName, setOriginStationName] = useState("");
    const [destinationStationName, setDestinationStationName] = useState("");

    const [criteria, setCriteria] = useState("duration");
    const [showAllCriteria, setShowAllCriteria] = useState(false);

    const [shortestPathError, setShortestPathError] = useState("");
    const [isShortestPathLoading, setIsShortestPathLoading] = useState(false);
    const [shortestPathResult, setShortestPathResult] = useState(null);
    const [multiCriteriaResults, setMultiCriteriaResults] = useState(null);

    const [nearestStations, setNearestStations] = useState([]); // 근처역 목록 저장
    const [nearestPick, setNearestPick] = useState(null); // { stationId, stationName, lineId, lineName, distanceM }

    const isMobile = typeof window !== "undefined" && window.matchMedia && window.matchMedia("(max-width: 480px)").matches;

    function isValidNames() {
        return Boolean((originStationName || "").trim() && (destinationStationName || "").trim());
    }

    async function requestShortestPath() {
        if (!isValidNames()) {
            setShortestPathError("출발/도착을 입력해주세요");
            return;
        }
        setIsShortestPathLoading(true);
        setShortestPathError("");
        setShortestPathResult(null);
        setMultiCriteriaResults(null);
        try {
            if (showAllCriteria) {
                const res = await fetchMultiCriteriaShortestPaths({ from: originStationName, to: destinationStationName, when: undefined, criteriaList: ["duration", "distance", "transfer"] });
                setMultiCriteriaResults(res);
            } else {
                const res = await fetchShortestPathWithArrivals({ from: originStationName, to: destinationStationName, when: undefined, searchType: criteria });
                setShortestPathResult(res);
            }
        } catch (e) {
            setShortestPathError(e?.message || "최단경로 조회 실패");
        } finally {
            setIsShortestPathLoading(false);
        }
    }

    useEffect(() => {
        if (isValidNames()) {
            requestShortestPath();
        } else {
            setShortestPathResult(null);
            setMultiCriteriaResults(null);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [originStationName, destinationStationName, criteria, showAllCriteria]);

    function swapStations() {
        setOriginStationName(destinationStationName);
        setDestinationStationName(originStationName);
    }

    function handlePickNearest(station) {
        setNearestPick(station);
        setOriginStationName(station?.stationName || "");
    }

    return (
        <div style={styles.appRoot}>
            <div style={styles.bgGradient} />

            <div style={{
                ...styles.container,
                maxWidth: isMobile ? "100%" : styles.container.maxWidth,
                padding: isMobile ? "16px 12px 28px" : styles.container.padding,
            }}>
                <header style={{
                    ...styles.header,
                    marginBottom: isMobile ? 12 : styles.header.marginBottom,
                }}>
                    <div style={styles.brandRow}>
                        <div style={{
                            ...styles.brandMark,
                            width: isMobile ? 32 : styles.brandMark.width,
                            height: isMobile ? 32 : styles.brandMark.height,
                        }} aria-hidden />
                        <h1 style={{
                            ...styles.title,
                            fontSize: isMobile ? 20 : styles.title.fontSize,
                        }}>Live BMW</h1>
                    </div>

                    <div style={styles.statusWrap}>
                        {deviceRegistrationErrorMessage ? (
                            <StatusPill tone="danger" text={deviceRegistrationErrorMessage} />
                        ) : (
                            <StatusPill
                                tone={registeredDeviceIdentifier ? "success" : "neutral"}
                                text={
                                    registeredDeviceIdentifier
                                        ? `유저 ID ${ellipsize(registeredDeviceIdentifier)}`
                                        : isDeviceRegistrationInProgress
                                            ? "디바이스 등록 중"
                                            : "디바이스 미등록"
                                }
                            />
                        )}
                    </div>
                </header>

                <section style={{
                    ...styles.panel,
                    padding: isMobile ? 12 : styles.panel.padding,
                    marginTop: isMobile ? 10 : styles.panel.marginTop,
                }}>
                    <div style={styles.panelHead}>
                        <h2 style={{
                            ...styles.h2,
                            fontSize: isMobile ? 18 : styles.h2.fontSize,
                        }}>
                            {originStationName || "-"} → {destinationStationName || "-"}
                        </h2>
                    </div>

                    <div style={{
                            ...styles.controlsRowSp,
                            gridTemplateColumns: isMobile ? "1fr" : styles.controlsRowSp.gridTemplateColumns,
                            gap: isMobile ? 8 : styles.controlsRowSp.gap,
                        }}>

                        <div style={{ display: "grid", gap: 6 }}>
                            <TextInput
                                placeholder="출발역"
                                value={originStationName}
                                onChange={(e) => setOriginStationName(e.target.value)}
                            />
                            <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
                                <UseCurrentLocationButton
                                    autoPickOnMount={true} // 페이지 진입 즉시 자동 설정 원하면 true
                                    onPick={(station) => {
                                        // API 응답 객체 필드명에 맞게 조정: station.name / station.stationName 등
                                        setOriginStationName(station.stationName ?? station.name);
                                    }}
                                    onFound={(list) => setNearestStations(list)}
                                />

                                {nearestStations.length > 0 && (
                                    <div style={{ marginTop: 10, fontSize: 13 }}>
                                        <div style={{ marginTop: 10, color: "#6b7280" }}>근처 역</div>
                                        <ul style={{ marginTop: 6, paddingLeft: 18 }}>
                                            {nearestStations.map((s) => (
                                                <NearestChip station={s} onApply={() => setOriginStationName(s.stationName)} />
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>
                        </div>
                        <button onClick={swapStations} title="출발/도착 교체" style={{
                            ...styles.swapButton,
                            width: isMobile ? 40 : styles.swapButton.width,
                            height: isMobile ? 40 : styles.swapButton.height,
                        }}>
                            ⇄
                        </button>
                        <TextInput
                            placeholder="도착역"
                            value={destinationStationName}
                            onChange={(e) => setDestinationStationName(e.target.value)}
                        />
                    </div>

                    <div style={{
                        ...styles.criteriaRow,
                        flexWrap: isMobile ? "wrap" : undefined,
                    }}>
                        <label style={styles.criteriaLabel}>기준</label>
                        <Select value={criteria} onChange={(e) => setCriteria(e.target.value)}>
                            <option value="duration">최소시간</option>
                            {/*<option value="distance">최단거리</option>*/}
                            <option value="transfer">최소환승</option>
                        </Select>
                        {isShortestPathLoading && <span style={styles.muted}>계산 중…</span>}
                    </div>

                    {shortestPathError && (
                        <div style={styles.alertDanger} role="alert">
                            <div style={styles.alertTitle}>요청 실패</div>
                            <div style={styles.muted}>{shortestPathError}</div>
                        </div>
                    )}
                </section>

                {(shortestPathResult || multiCriteriaResults) && (
                    <section style={{
                        ...styles.panel,
                        paddingTop: isMobile ? 12 : styles.panel.paddingTop || 14,
                        padding: isMobile ? 12 : styles.panel.padding,
                        marginTop: isMobile ? 10 : styles.panel.marginTop,
                    }}>
                        {shortestPathResult && (() => {
                            const ksRaw = Array.isArray(shortestPathResult.keyStations) ? shortestPathResult.keyStations : [];
                            const groups = Array.isArray(shortestPathResult.groups) ? shortestPathResult.groups : [];
                            const searchType = shortestPathResult.plan?.searchType;

                            let ks = searchType === "duration" ? ksRaw.slice(1) : ksRaw;
                            if (searchType === "duration" && ks.length === 0 && ksRaw.length > 0) {
                                ks = ksRaw.slice(0, 1);
                            }

                            return (
                                <div style={{ marginTop: 6 }}>
                                    {shortestPathResult.plan && (
                                        <div style={{ marginBottom: 8, color: "#64748b" }}>
                                            {searchType === "duration" ? "최소시간" : searchType === "distance" ? "최단거리" : "최소환승"} 경로
                                        </div>
                                    )}

                                    {ks.map((k, idx) => {
                                        const label = searchType !== undefined
                                            ? (ksRaw.length > 1 ? `${k.stationName} (${idx + 1}회 환승)` : `${k.stationName}`)
                                            : (idx === 0 ? `${k.stationName}` : `${k.stationName} (${idx}회 환승)`);
                                        const group = groups.find(g => g.stationName === k.stationName && g.direction === k.direction) || { arrivals: [] };
                                        const topArrivals = (group.arrivals || []).slice(0, isMobile ? 1 : 2);
                                        return (
                                            <div key={`${k.stationName}-${k.direction}-${idx}`} style={{ marginTop: isMobile ? 10 : 14 }}>
                                                <div style={{ fontWeight: 700, marginBottom: 6 }}>{label}</div>
                                                <div style={{ display: "grid", gap: 10 }}>
                                                    {topArrivals.length > 0 ? (
                                                        topArrivals.map((a, i) => (
                                                            <ArrivalCard key={`${k.stationName}-${k.direction}-${i}`} arrivalItem={a} contextLabel={label} />
                                                        ))
                                                    ) : (
                                                        <div style={{ padding: 12, border: "1px dashed #e5e7eb", borderRadius: 10, color: "#6b7280" }}>
                                                            도착 정보가 없습니다
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            );
                        })()}
                    </section>
                )}

                <footer style={styles.footer}>
                    <div style={styles.mutedSmall}>© {new Date().getFullYear()} Live BMW</div>
                    <div style={styles.mutedSmall}>v0.1 · Experimental</div>
                </footer>
            </div>
        </div>
    );
}

function StatusPill({ tone = "neutral", text }) {
    const toneMap = {
        neutral: { bg: "#eef2ff", fg: "#3730a3", dot: "#6366f1" },
        success: { bg: "#ecfdf5", fg: "#065f46" },
        danger: { bg: "#fef2f2", fg: "#991b1b" },
    };
    const t = toneMap[tone] || toneMap.neutral;
    return (
        <span style={{ ...styles.pill, background: t.bg, color: t.fg }}>
      <span style={{ ...styles.pillDot }} /> {text}
    </span>
    );
}

function NearestChip({ station, onApply }) {
    const badgeBg = "rgba(0,0,0,0.04)";
    const badgeBd = "#e5e7eb";
    return (
        <button onClick={onApply} style={{ display: "inline-flex", alignItems: "center", gap: 8, padding: "6px 10px", borderRadius: 999, border: `1px solid ${badgeBd}`, background: "#fff", cursor: "pointer" }}>
            <span style={{ display: "inline-flex", alignItems: "center", gap: 6, padding: "2px 8px", borderRadius: 999, background: badgeBg, border: `1px solid ${badgeBd}`, fontSize: 12, fontWeight: 700 }}>{station.lineName}</span>
            <span style={{ fontWeight: 600 }}>{station.stationName}</span>
            <span style={{ color: "#64748b", fontSize: 12 }}>{station.distanceM} m</span>
        </button>
    );
}

function TextInput(props) {
    return (
        <input
            {...props}
            style={{
                ...styles.input,
                ...(props.style || {}),
            }}
        />
    );
}

function Select(props) {
    return (
        <select
            {...props}
            style={{
                ...styles.input,
                height: 40,
                paddingRight: 28,
            }}
        />
    );
}

function ellipsize(str = "", head = 6, tail = 4) {
    if (!str || str.length <= head + tail + 1) return str;
    return `${str.slice(0, head)}…${str.slice(-tail)}`;
}

function PlanStepper({ legs }) {
    if (!Array.isArray(legs) || legs.length === 0) return null;
    return (
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 8 }}>
            {legs.map((leg, idx) => (
                <div key={idx} style={{ display: "inline-flex", alignItems: "center", gap: 6, padding: "6px 10px", borderRadius: 999, background: "#f1f5f9", border: "1px solid #e5e7eb" }}>
                    <span style={{ fontWeight: 700 }}>{leg.fromName}</span>
                    <span style={{ color: "#94a3b8" }}>→</span>
                    <span style={{ fontWeight: 700 }}>{leg.toName}</span>
                    <span style={{ color: "#64748b", fontSize: 12 }}>{leg.direction}</span>
                </div>
            ))}
        </div>
    );
}

const styles = {
    appRoot: {
        minHeight: "100dvh",
        fontFamily:
            "ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, Apple Color Emoji, Segoe UI Emoji",
        color: "#0f172a",
        position: "relative",
        background: "#f8fafc",
    },
    bgGradient: {
        position: "fixed",
        inset: 0,
        background:
            "radial-gradient(1200px 600px at 80% -10%, rgba(99,102,241,0.08), transparent 55%), radial-gradient(1000px 600px at -10% 10%, rgba(14,165,233,0.08), transparent 50%)",
        pointerEvents: "none",
    },
    container: { maxWidth: 440, margin: "0 auto", padding: "32px 20px 40px" },
    header: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 16 },
    brandRow: { display: "flex", alignItems: "center", gap: 12 },
    brandMark: {
        width: 36,
        height: 36,
        borderRadius: 12,
        background: "linear-gradient(135deg, #4f46e5 0%, #3b82f6 40%, #06b6d4 100%)",
        boxShadow: "0 6px 18px rgba(79,70,229,0.25)",
    },
    title: { margin: 0, fontSize: 22, letterSpacing: 0.2, fontWeight: 700 },
    statusWrap: { display: "flex", gap: 8, alignItems: "center" },

    panel: {
        background: "rgba(255,255,255,0.82)",
        backdropFilter: "saturate(1.4) blur(6px)",
        WebkitBackdropFilter: "saturate(1.4) blur(6px)",
        border: "1px solid rgba(15,23,42,0.06)",
        borderRadius: 20,
        boxShadow: "0 8px 24px rgba(2,6,23,0.06)",
        padding: 16,
        marginTop: 14,
    },
    panelHead: { display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 },
    kicker: { fontSize: 12, letterSpacing: 0.6, textTransform: "uppercase", color: "#64748b", marginBottom: 4 },
    h2: { margin: 0, fontSize: 20, fontWeight: 700 },
    h3: { margin: 0, fontSize: 16, fontWeight: 700 },

    controlsRowSp: { display: "grid", gridTemplateColumns: "1fr auto 1fr", gap: 8, marginTop: 10 },
    criteriaRow: { display: "flex", alignItems: "center", gap: 10, marginTop: 8 },
    criteriaLabel: { fontSize: 12, color: "#64748b" },
    checkRow: { display: "inline-flex", alignItems: "center", gap: 4 },

    input: {
        height: 40,
        padding: "10px 12px",
        borderRadius: 12,
        border: "1px solid #e5e7eb",
        outline: "none",
        background: "#fff",
        fontSize: 14,
        boxSizing: "border-box",
    },
    swapButton: {
        width: 40,
        height: 40,
        borderRadius: 12,
        border: "1px solid #e5e7eb",
        background: "#fff",

        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        cursor: "pointer",
    },

    alertDanger: {
        marginTop: 12,
        border: "1px solid #fecaca",
        background: "#fff1f2",
        color: "#7f1d1d",
        borderRadius: 14,
        padding: "10px 12px",
    },
    alertTitle: { fontWeight: 600, marginBottom: 2 },
    muted: { color: "#475569" },
    mutedSmall: { color: "#64748b", fontSize: 12 },

    sectionHead: { display: "flex", alignItems: "baseline", justifyContent: "space-between", marginBottom: 8 },

    pill: { display: "inline-flex", alignItems: "center", gap: 8, padding: "6px 10px", borderRadius: 999, fontSize: 12, fontWeight: 600 },
    pillDot: { width: 8, height: 8, borderRadius: 999, flex: "0 0 auto" },
};

const injected = typeof document !== "undefined" && document.getElementById("bmw-keyframes");
if (!injected && typeof document !== "undefined") {
    const styleEl = document.createElement("style");
    styleEl.id = "bmw-keyframes";
    styleEl.textContent = `
    @keyframes bmwspin { to { transform: rotate(360deg); } }
    @keyframes bmwshimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    button:active { transform: translateY(1px); }

    @media (max-width: 480px) {
      body { background: #f8fafc; }
    }

    @media (max-width: 360px) {
      .btn-compact { font-size: 13px !important; padding: 10px 14px !important; line-height: 18px !important; }
    }

    @media (hover:hover) { button:hover { box-shadow: 0 10px 24px rgba(79,70,229,0.35); } }
  `;
    document.head.appendChild(styleEl);
}