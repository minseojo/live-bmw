import React, { useEffect, useRef, useState } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { ArrivalCard } from "./components/ArrivalCard";
import { fetchShortestPathWithArrivals, fetchNearestStations } from "./api/metroApi";
import { getBrowserLocation } from "./hooks/useGeoLocation";
import { NearestStationsButton } from "./components/NearestStationsButton";
import { RouteStationInput } from "./components/RouteStationInput";

import { stations as stationsData } from "./data/stations.js"; // 로컬 자동완성 데이터
import "./styles/App.css";

import { RouteChip } from "./components/RouteChip.jsx";

export default function App() {
    const {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    } = useDeviceRegistration();

    // 검색 입력/상태
    const [fromName, setFromName] = useState("");
    const [toName, setToName] = useState("");
    const [criteria, setCriteria] = useState("duration");

    // 사용자가 "정확히" 선택한 라인의 ID를 별도로 유지
    const [fromLineId, setFromLineId] = useState(""); // "1002" 같은 값
    const [toLineId, setToLineId] = useState("");

    const [fromSelected, setFromSelected] = useState(false);
    const [toSelected, setToSelected] = useState(false);

    // 근처 역(초기 자동 채움 + 버튼 seed)
    const [nearestStations, setNearestStations] = useState([]);

    // 요청/결과
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [result, setResult] = useState(null);

    const lastReqKeyRef = useRef("");
    const isMobile =
        typeof window !== "undefined" &&
        window.matchMedia &&
        window.matchMedia("(max-width: 480px)").matches;

    /** 이름+라인ID로 정확 매칭된 메타 가져오기 */
    const findStationMeta = (name, lineId) => {
        if (!name || !lineId) return null;
        const hit =
            stationsData.find(
                (s) =>
                    (s.station_name || s.stationName) === name &&
                    String(s.line_id || s.lineId) === String(lineId)
            ) ||
            nearestStations.find(
                (s) =>
                    (s.stationName || s.station_name) === name &&
                    String(s.lineId || s.line_id) === String(lineId)
            );
        if (!hit) return null;
        return {
            stationName: hit.station_name || hit.stationName || name,
            lineId: String(hit.line_id || hit.lineId || ""),
            lineName: hit.line_name || hit.lineName || "",
            stationId: String(hit.station_id || hit.stationId || ""),
        };
    };

    // 앱/웹 최초 1회: 현재 위치 -> 근처역 -> 출발지 자동 설정 (가장 가까운 1개로 확정)
    useEffect(() => {
        const INIT_KEY = "once:auto_nearest_origin";
        if (sessionStorage.getItem(INIT_KEY)) return;
        sessionStorage.setItem(INIT_KEY, "1");

        (async () => {
            try {
                const coords = await getBrowserLocation();
                const list = await fetchNearestStations({ lat: coords.lat, lng: coords.lng, limit: 3 });
                if (Array.isArray(list) && list.length > 0) {
                    const first = list[0];
                    setNearestStations(list);
                    setFromName(first.stationName ?? first.name);
                    setFromLineId(String(first.lineId || first.line_id || "")); // 라인도 함께 고정
                    setFromSelected(true); // 자동 채움은 확정으로 간주
                }
            } catch {
                // 권한 거부/실패: 조용히 무시
            }
        })();
    }, []);

    // 출발/도착이 모두 확정되면 자동 경로 조회
    useEffect(() => {
        if (!fromSelected || !toSelected) return;
        const reqKey = `${fromName}|${toName}|${criteria}`;
        if (lastReqKeyRef.current === reqKey) return;
        lastReqKeyRef.current = reqKey;

        (async () => {
            setLoading(true);
            setError("");
            setResult(null);
            try {
                const res = await fetchShortestPathWithArrivals({
                    from: fromName,
                    to: toName,
                    searchType: criteria,
                });
                setResult(res);
            } catch (e) {
                setError(e?.message || "검색 실패");
            } finally {
                setLoading(false);
            }
        })();
    }, [fromSelected, toSelected, fromName, toName, criteria]);

    // 스왑 (선택 상태 + 라인까지 함께)
    const swap = () => {
        setFromName(toName);
        setToName(fromName);
        const prevFromSel = fromSelected;
        const prevFromLine = fromLineId;

        setFromSelected(toSelected);
        setToSelected(prevFromSel);

        setFromLineId(toLineId);
        setToLineId(prevFromLine);
    };

    return (
        <div className="app-root">
            <div className="bg-gradient" />

            <div className="container">
                {/* 헤더 */}
                <header className="header">
                    <div className="brand-row">
                        <div className="brand-mark" aria-hidden />
                        <h1 className="title">Live BMW</h1>
                    </div>
                    <div className="status-wrap">
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

                {/* 검색 패널 */}
                {/* 검색 패널 */}
                <section className="panel panel-search route-form">
                    {/* 상단: 주변역 + 초기화 */}
                    <div className="route-top-row">
                        {/* 주변 역 버튼 */}
                        <NearestStationsButton
                            seedStations={nearestStations}
                            limit={5}
                            onApply={(s) => {
                                setFromName(s.stationName);
                                setFromLineId(String(s.lineId || s.line_id || ""));
                                setFromSelected(true);
                            }}
                        />

                        {/* 초기화 버튼 (✕) */}
                        <button
                            className="reset-btn"
                            onClick={() => {
                                setFromName("");
                                setToName("");
                                setFromLineId("");
                                setToLineId("");
                                setFromSelected(false);
                                setToSelected(false);
                            }}
                        >
                            ✕
                        </button>
                    </div>

                    {/* 하단: 스왑 + 출발/도착 입력 */}
                    <div className="route-form-row">
                        {/* 스왑 버튼 */}
                        <div className="swap-col">
                            <button className="swap-btn" onClick={swap} title="출발/도착 교체">⇅</button>
                        </div>

                        {/* 출발/도착 인풋 */}
                        <div className="route-inputs">
                            {/* 출발지 입력 */}
                            <RouteStationInput
                                stations={stationsData}
                                placeholder="출발지"
                                value={fromName}
                                lineId={fromLineId}
                                onChangeText={(txt) => { setFromName(txt); setFromLineId(""); setFromSelected(false); }}
                                onSelectStation={(s) => {
                                    setFromName(s.station_name || s.stationName);
                                    setFromLineId(String(s.line_id || s.lineId || ""));
                                    setFromSelected(true);
                                }}
                            />

                            {/* 도착지 입력 */}
                            <RouteStationInput
                                stations={stationsData}
                                placeholder="도착지"
                                value={toName}
                                lineId={toLineId}
                                onChangeText={(txt) => { setToName(txt); setToLineId(""); setToSelected(false); }}
                                onSelectStation={(s) => {
                                    setToName(s.station_name || s.stationName);
                                    setToLineId(String(s.line_id || s.lineId || ""));
                                    setToSelected(true);
                                }}
                            />
                        </div>
                    </div>
                </section>

                {/* 결과 패널 */}
                {result && (
                    <section className="panel">
                        {(() => {
                            const ksRaw = Array.isArray(result.keyStations) ? result.keyStations : [];
                            const groups = Array.isArray(result.groups) ? result.groups : [];
                            const searchType = result.plan?.searchType;

                            let ks = searchType === "duration" ? ksRaw.slice(1) : ksRaw;
                            if (searchType === "duration" && ks.length === 0 && ksRaw.length > 0) {
                                ks = ksRaw.slice(0, 1);
                            }

                            return (
                                <div className="mt-6">
                                    {result.plan && (
                                        <div className="muted plan-label">
                                            {searchType === "duration"
                                                ? "최소시간"
                                                : searchType === "distance"
                                                    ? "최단거리"
                                                    : "최소환승"}{" "}
                                            경로
                                        </div>
                                    )}

                                    {ks.map((k, idx) => {
                                        const label =
                                            searchType !== undefined
                                                ? ksRaw.length > 1
                                                    ? `${k.stationName} (${idx + 1}회 환승)`
                                                    : `${k.stationName}`
                                                : idx === 0
                                                    ? `${k.stationName}`
                                                    : `${k.stationName} (${idx}회 환승)`;

                                        const group =
                                            groups.find(
                                                (g) => g.stationName === k.stationName && g.direction === k.direction
                                            ) || { arrivals: [] };

                                        const topArrivals = (group.arrivals || []).slice(0, isMobile ? 1 : 2);

                                        return (
                                            <div key={`${k.stationName}-${k.direction}-${idx}`} className="mt-14">
                                                <div className="step-title">{label}</div>
                                                <div className="arrival-grid">
                                                    {topArrivals.length > 0 ? (
                                                        topArrivals.map((a, i) => (
                                                            <ArrivalCard
                                                                key={`${k.stationName}-${k.direction}-${i}`}
                                                                arrivalItem={a}
                                                                contextLabel={label}
                                                            />
                                                        ))
                                                    ) : (
                                                        <div className="arrival-empty">도착 정보가 없습니다</div>
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

                <footer className="footer">
                    <div className="muted-small">© {new Date().getFullYear()} Live BMW</div>
                    <div className="muted-small">v0.1 · Experimental</div>
                </footer>
            </div>
        </div>
    );
}

/* --- 보조 컴포넌트 --- */

function StatusPill({ tone = "neutral", text }) {
    return <span className={`pill pill-${tone}`}><span className="pill-dot" />{text}</span>;
}

function Select(props) {
    return <select {...props} className={`input select ${props.className || ""}`} />;
}

function ellipsize(str = "", head = 6, tail = 4) {
    if (!str || str.length <= head + tail + 1) return str;
    return `${str.slice(0, head)}…${str.slice(-tail)}`;
}
