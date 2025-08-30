import React, { useEffect, useRef, useState } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { ArrivalCard } from "./components/ArrivalCard";
import { fetchShortestPathWithArrivals, fetchNearestStations } from "./api/metroApi";
import { getBrowserLocation } from "./hooks/useGeoLocation";
import { StationSearchInput } from "./components/StationSearchInput";
import { NearestStationsButton } from "./components/NearestStationsButton";
import { stations as stationsData } from "./data/stations.js"; // 로컬 자동완성 데이터
import "./styles/App.css";

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

    // 앱/웹 최초 1회: 현재 위치 -> 근처역 -> 출발지 자동 설정
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

    // 스왑 (선택 상태까지 함께)
    const swap = () => {
        setFromName(toName);
        setToName(fromName);
        const prevFromSel = fromSelected;
        setFromSelected(toSelected);
        setToSelected(prevFromSel);
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
                <section className="panel">
                    <div className="panel-head">
                        <h2 className="h2">{fromName || "-"} → {toName || "-"}</h2>
                    </div>

                    <div className="controls-row-sp">
                        <div className="grid-gap-6">
                            {/* 출발역 입력 */}
                            <StationSearchInput
                                stations={stationsData}
                                placeholder="출발역"
                                value={fromName}
                                onChange={(e) => { setFromName(e.target.value); setFromSelected(false); }}
                                onSelect={(s) => { setFromName(s.station_name); setFromSelected(true); }}
                            />

                            {/* 주변 역 목록 버튼 */}
                            <NearestStationsButton
                                seedStations={nearestStations}
                                limit={5}
                                onApply={(s) => {
                                    setFromName(s.stationName);
                                    setFromSelected(true);
                                }}
                            />
                        </div>

                        <button className="swap-button" onClick={swap} title="출발/도착 교체">⇄</button>

                        {/* 도착역 입력 */}
                        <StationSearchInput
                            stations={stationsData}
                            placeholder="도착역"
                            value={toName}
                            onChange={(e) => { setToName(e.target.value); setToSelected(false); }}
                            onSelect={(s) => { setToName(s.station_name); setToSelected(true); }}
                        />
                    </div>

                    <div className="criteria-row">
                        <label className="criteria-label">기준</label>
                        <Select value={criteria} onChange={(e) => setCriteria(e.target.value)}>
                            <option value="duration">최소시간</option>
                            <option value="transfer">최소환승</option>
                        </Select>
                        {loading && <span className="muted" style={{ marginLeft: 8 }}>계산 중…</span>}
                        {error && <span className="muted" style={{ marginLeft: 8 }}>{error}</span>}
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
                                            {searchType === "duration" ? "최소시간" :
                                                searchType === "distance" ? "최단거리" : "최소환승"} 경로
                                        </div>
                                    )}

                                    {ks.map((k, idx) => {
                                        const label =
                                            searchType !== undefined
                                                ? (ksRaw.length > 1 ? `${k.stationName} (${idx + 1}회 환승)` : `${k.stationName}`)
                                                : (idx === 0 ? `${k.stationName}` : `${k.stationName} (${idx}회 환승)`);

                                        const group =
                                            groups.find(g => g.stationName === k.stationName && g.direction === k.direction) || { arrivals: [] };

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
