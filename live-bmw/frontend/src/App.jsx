import React, { useMemo, useState } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { useCountdownList } from "./hooks/useCountdownList";
import { fetchRouteArrivals } from "./api/metroApi";
import { ArrivalCard } from "./components/ArrivalCard";
import { RouteSummary } from "./components/RouteSummary";

/**
 * UI revamp notes
 * - Soft glass panels, subtle gradient header, and clean system font stack
 * - Consistent spacing scale, rounded-2xl, gentle shadows
 * - Primary button with loading spinner
 * - Inline, framework-agnostic styles (no Tailwind dependency)
 */

// === DTO Normalizers ===============================================
// 백엔드 DTO가 개편되어도 프론트는 이 정규화 함수를 통해 동일한
// 뷰모델 형태로 받도록 함(하위 호환):
//   Route: { lineId, lineName, originStation, destinationStation, direction, nextStation, arrivals: [] }
//   Arrival: { lineName, direction, desc, trainNo, message, dest, etaSeconds, receivedAt }
function metroArrivalResponse(apiItem = {}) {
    return {
        lineId: apiItem.lineId,
        lineName: apiItem.lineName,
        direction: apiItem.direction,
        trainLineSummary: apiItem.trainLineSummary,
        trainNumber: apiItem.trainNumber,
        message: apiItem.message,
        messageSub: apiItem.messageSub,
        etaSeconds: apiItem.etaSeconds,
        receivedAt: apiItem.receivedAt,
    };

}

function routeArrivalsResponse(api = {}) {
    const arrivalsRaw = api.arrivals ?? api.arrivalList ?? [];
    return {
        lineId: api.lineId ?? api.metroLineId ?? api.lineIdentifier ?? "",
        lineName: api.lineName ?? api.metroLineName ?? api.line ?? "",
        originStation: api.originStation ?? api.originStationName ?? api.origin ?? "",
        destinationStation: api.destinationStation ?? api.destinationStationName ?? api.destination ?? "",
        direction: api.direction ?? api.direction ?? "",
        nextStation: api.nextStation ?? api.nextStationName ?? api.nextStationIdName ?? "",
        arrivals: arrivalsRaw.map(metroArrivalResponse),
    };
}
// ===================================================================

export default function App() {
    // ===== device registration =====
    const {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    } = useDeviceRegistration();

    // ===== route state =====
    const [routeResponsePayload, setRouteResponsePayload] = useState(null);
    const [routeFetchErrorMessage, setRouteFetchErrorMessage] = useState("");
    const [isRouteFetchInProgress, setIsRouteFetchInProgress] = useState(false);

    // 고정 경로(요구사항): 봉천 → 구로디지털단지 / 2호선
    const fixedRouteRequest = useMemo(
        () => ({
            originStationName: "봉천",
            destinationStationName: "구로디지털단지",
            lineIdentifier: "1002",
            resultLimit: 5,
        }),
        []
    );

    const arrivalItemsWithCountdown = useCountdownList(
        (routeResponsePayload?.arrivals || [])
    );

    async function requestRouteArrivalsFromServer() {
        setIsRouteFetchInProgress(true);
        setRouteFetchErrorMessage("");
        try {
            const serverResponseJson = await fetchRouteArrivals(fixedRouteRequest);
            setRouteResponsePayload(routeArrivalsResponse(serverResponseJson));
        } catch (fetchError) {
            setRouteFetchErrorMessage(fetchError?.message || "도착정보 요청 실패");
        } finally {
            setIsRouteFetchInProgress(false);
        }
    }

    return (
        <div style={styles.appRoot}>
            {/* Background gradient */}
            <div style={styles.bgGradient} />

            <div style={styles.container}>
                {/* Top Bar */}
                <header style={styles.header}>
                    <div style={styles.brandRow}>
                        <div style={styles.brandMark} aria-hidden />
                        <h1 style={styles.title}>Live BMW</h1>
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

                {/* Controls Card */}
                <section style={styles.panel}>
                    <div style={styles.panelHead}>
                        <h2 style={{ ...styles.h2, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                            봉천 → 구로디지털단지 (2호선)
                        </h2>
                        <div>
                            <PrimaryButton
                                onClick={requestRouteArrivalsFromServer}
                                disabled={isRouteFetchInProgress}
                                loading={isRouteFetchInProgress}
                            >
                                {isRouteFetchInProgress ? "불러오는 중" : "도착정보 조회"}
                            </PrimaryButton>
                        </div>
                    </div>

                    {routeFetchErrorMessage && (
                        <div style={styles.alertDanger} role="alert">
                            <div style={styles.alertTitle}>요청 실패</div>
                            <div style={styles.muted}>{routeFetchErrorMessage}</div>
                        </div>
                    )}

                    {/* Route summary */}
                    <div style={{ marginTop: 12 }}>
                        <RouteSummary routeResponse={routeResponsePayload} />
                    </div>
                </section>

                {/* Arrivals List */}
                <section style={{ ...styles.panel, paddingTop: 14 }}>
                    <div style={styles.sectionHead}>
                        <h3 style={styles.h3}>실시간 도착</h3>
                        {isRouteFetchInProgress && (
                            <span style={styles.muted}>최신 데이터 로딩 중…</span>
                        )}
                    </div>

                    <div style={styles.grid}>
                        {isRouteFetchInProgress && arrivalItemsWithCountdown.length === 0 ? (
                            <>
                                <SkeletonCard />
                                <SkeletonCard />
                                <SkeletonCard />
                            </>
                        ) : arrivalItemsWithCountdown.length === 0 ? (
                            <EmptyState />
                        ) : (
                            arrivalItemsWithCountdown.slice(0, 3).map((arrivalItem, index) => (
                                <div key={arrivalItem.trainNumber + index} style={styles.cardWrap}>
                                    <ArrivalCard arrivalItem={arrivalItem} />
                                </div>
                            ))
                        )}
                    </div>
                </section>

                {/* Footer */}
                <footer style={styles.footer}>
                    <div style={styles.mutedSmall}>© {new Date().getFullYear()} Live BMW</div>
                    <div style={styles.mutedSmall}>v0.1 · Experimental</div>
                </footer>
            </div>
        </div>
    );
}

// ——— UI Pieces ———
function PrimaryButton({ loading, children, ...props }) {
    return (
        <button
            {...props}
            style={{
                ...styles.button,
                flexShrink: 0,
                ...(props.disabled ? styles.buttonDisabled : {}),
            }}
        >
            {loading && <Spinner />}
            <span>{children ?? "도착정보 조회"}</span>
        </button>
    );
}

function Spinner() {
    return (
        <span style={styles.spinner} aria-hidden>
      <svg viewBox="0 0 24 24" width={18} height={18} style={styles.spinnerSvg}>
        <circle cx="12" cy="12" r="9.5" fill="none" stroke="currentColor" strokeWidth="2" opacity="0.25" />
        <path d="M21.5 12a9.5 9.5 0 0 0-9.5-9.5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      </svg>
    </span>
    );
}

function StatusPill({ tone = "neutral", text }) {
    const toneMap = {
        neutral: { bg: "#eef2ff", fg: "#3730a3", dot: "#6366f1" },
        success: { bg: "#ecfdf5", fg: "#065f46", dot: "#10b981" },
        danger: { bg: "#fef2f2", fg: "#991b1b", dot: "#ef4444" },
    };
    const t = toneMap[tone] || toneMap.neutral;
    return (
        <span style={{ ...styles.pill, background: t.bg, color: t.fg }}>
      <span style={{ ...styles.pillDot, background: t.dot }} /> {text}
    </span>
    );
}

function EmptyState() {
    return (
        <div style={styles.emptyWrap}>
            <div style={styles.emptyIcon} aria-hidden>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <circle cx="12" cy="12" r="9" opacity="0.3" />
                    <path d="M8 13h8M8 10h8" opacity="0.6" />
                </svg>
            </div>
            <div style={styles.emptyTitle}>표시할 도착 정보가 없습니다</div>
            <div style={styles.emptyText}>상단의 ‘도착정보 조회’ 버튼을 눌러 최신 정보를 불러오세요.</div>
        </div>
    );
}

function SkeletonCard() {
    return (
        <div style={styles.skeletonCard}>
            <div style={styles.skeletonLineWide} />
            <div style={styles.skeletonRow}>
                <div style={styles.skeletonLine} />
                <div style={styles.skeletonBadge} />
            </div>
        </div>
    );
}

// ——— utils ———
function ellipsize(str = "", head = 6, tail = 4) {
    if (!str || str.length <= head + tail + 1) return str;
    return `${str.slice(0, head)}…${str.slice(-tail)}`;
}

// ——— styles ———
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
    container: { maxWidth: 880, margin: "0 auto", padding: "32px 20px 40px" },
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
    grid: { display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 12 },

    cardWrap: { borderRadius: 16, border: "1px solid #e5e7eb", background: "#fff", overflow: "hidden" },

    footer: { display: "flex", alignItems: "center", justifyContent: "space-between", marginTop: 18, padding: "8px 2px" },

    button: {
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        background: "#4f46e5",
        color: "white",
        fontWeight: 600,
        fontSize: 14,
        border: 0,
        borderRadius: 12,
        padding: "12px 18px",
        lineHeight: "20px",
        minHeight: 40,
        boxSizing: "border-box",
        whiteSpace: "nowrap",
        width: "fit-content",
        maxWidth: "100%",
        overflow: "visible",
        boxShadow: "0 8px 20px rgba(79,70,229,0.28)",
        cursor: "pointer",
        transition: "transform 120ms ease, box-shadow 120ms ease, opacity 120ms",
    },

    buttonDisabled: { opacity: 0.7, cursor: "default", boxShadow: "none" },
    spinner: { display: "inline-flex" },
    spinnerSvg: { display: "block", transformOrigin: "center", animation: "bmwspin 0.9s linear infinite" },

    pill: { display: "inline-flex", alignItems: "center", gap: 8, padding: "6px 10px", borderRadius: 999, fontSize: 12, fontWeight: 600 },
    pillDot: { width: 8, height: 8, borderRadius: 999, flex: "0 0 auto" },

    emptyWrap: {
        gridColumn: "1 / -1",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        padding: "28px 12px",
        border: "1px dashed #e2e8f0",
        borderRadius: 16,
        background: "#fff",
    },
    emptyIcon: { color: "#94a3b8", marginBottom: 6 },
    emptyTitle: { fontWeight: 700, marginBottom: 4 },
    emptyText: { color: "#64748b" },

    skeletonCard: { borderRadius: 16, border: "1px solid #e5e7eb", background: "#fff", padding: 12 },
    skeletonRow: { display: "flex", alignItems: "center", justifyContent: "space-between", marginTop: 10 },
    skeletonLineWide: { height: 12, borderRadius: 6, background: "linear-gradient(90deg, #f1f5f9, #e2e8f0, #f1f5f9)", backgroundSize: "200% 100%", animation: "bmwshimmer 1.2s ease-in-out infinite" },
    skeletonLine: { width: "50%", height: 10, borderRadius: 6, background: "linear-gradient(90deg, #f1f5f9, #e2e8f0, #f1f5f9)", backgroundSize: "200% 100%", animation: "bmwshimmer 1.2s ease-in-out infinite" },
    skeletonBadge: { width: 60, height: 20, borderRadius: 999, background: "linear-gradient(90deg, #f1f5f9, #e2e8f0, #f1f5f9)", backgroundSize: "200% 100%", animation: "bmwshimmer 1.2s ease-in-out infinite" },
};

// Global keyframes injection (once)
const injected = typeof document !== "undefined" && document.getElementById("bmw-keyframes");
if (!injected && typeof document !== "undefined") {
    const styleEl = document.createElement("style");
    styleEl.id = "bmw-keyframes";
    styleEl.textContent = `
    @keyframes bmwspin { to { transform: rotate(360deg); } }
    @keyframes bmwshimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    button:active { transform: translateY(1px); }

    @media (max-width: 360px) {
      .btn-compact { font-size: 13px !important; padding: 10px 14px !important; line-height: 18px !important; }
    }

    @media (hover:hover) { button:hover { box-shadow: 0 10px 24px rgba(79,70,229,0.35); } }
  `;
    document.head.appendChild(styleEl);
}