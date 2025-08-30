import React, { useEffect, useRef, useState } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { getBrowserLocation } from "./hooks/useGeoLocation";
import { NearestStationsButton } from "./components/NearestStationsButton";
import { RouteStationInput } from "./components/RouteStationInput";

import { stations as stationsData } from "./data/stations.js"; // 로컬 자동완성 데이터
import "./App.css";
import { RouteArrivalsSection } from "./components/RouteArrivalsSection.jsx";

export default function App() {
    const {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    } = useDeviceRegistration();

    // 검색 입력/상태
    const [fromName, setFromName] = useState("");
    const [toName, setToName] = useState("");
    const [criteria, setCriteria] = useState("duration"); // 최단시간/최소환승/최단거리

    // 사용자가 "정확히" 선택한 라인의 ID를 별도로 유지
    const [fromLineId, setFromLineId] = useState(""); // "1002" 같은 값
    const [toLineId, setToLineId] = useState("");

    const [fromSelected, setFromSelected] = useState(false);
    const [toSelected, setToSelected] = useState(false);

    // 근처 역(초기 자동 채움 + 버튼 seed)
    const [nearestStations, setNearestStations] = useState([]);

    const isMobile =
        typeof window !== "undefined" &&
        window.matchMedia &&
        window.matchMedia("(max-width: 480px)").matches;

    // 앱/웹 최초 1회: 현재 위치 -> 근처역 -> 출발지 자동 설정 (가장 가까운 1개로 확정)
    useEffect(() => {
        const INIT_KEY = "once:auto_nearest_origin";
        if (sessionStorage.getItem(INIT_KEY)) return;
        sessionStorage.setItem(INIT_KEY, "1");

        (async () => {
            try {
                const coords = await getBrowserLocation();
                // 근처역 API는 외부에 구현되어 있다고 가정 (hooks/useGeoLocation, api/metro 등)
                // 여기서는 props로 받은 NearestStationsButton의 seed로만 사용
                const res = await fetch(`/api/metro/stations/nearest?lat=${coords.lat}&lng=${coords.lng}&limit=3`);
                const list = await res.json();
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

    // 두 역이 확정되었는지 여부 (RouteArrivalsSection 렌더링 제어용)
    const ready = fromSelected && toSelected && fromName && toName;

    return (
        <div className="app-root">
            <div className="bg-gradient" />

            <div className="container">
                {/* 헤더 */}
                <header className="header">
                    <div className="brand-row">
                        {/* ✅ public 아래 자원은 /로 시작 */}
                        <img src="/icons/icon-192.svg" alt="로고" width="32" height="32" />
                        <h2 className="title">지하철 실시간 알리미</h2>
                    </div>
                </header>

                {/* 검색 패널 */}
                <section className="panel panel-search route-form">
                    {/* 상단: 주변역 | 초기화 */}
                    <div className="route-top-row">
                        <NearestStationsButton
                            seedStations={nearestStations}
                            limit={5}
                            onApply={(s) => {
                                setFromName(s.stationName);
                                setFromLineId(String(s.lineId || s.line_id || ""));
                                setFromSelected(true);
                            }}
                        />
                        <button
                            className="reset-btn"
                            onClick={() => {
                                setFromName(""); setToName("");
                                setFromLineId(""); setToLineId("");
                                setFromSelected(false); setToSelected(false);
                            }}
                        >
                            ✕
                        </button>
                    </div>

                    {/* 하단: 스왑 + 입력 */}
                    <div className="route-form-row">
                        <div className="swap-col">
                            <button className="swap-btn" onClick={swap} title="출발/도착 교체">⇅</button>
                        </div>
                        <div className="route-inputs">
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

                    {/* 기준 선택 (선택사항) */}
                    {/*<div className="route-criteria-row">*/}
                    {/*    <select*/}
                    {/*        value={criteria}*/}
                    {/*        onChange={(e) => setCriteria(e.target.value)}*/}
                    {/*        className="input select"*/}
                    {/*        style={{ width: "100%", marginTop: 8 }}*/}
                    {/*    >*/}
                    {/*        <option value="duration">최단시간</option>*/}
                    {/*        <option value="transfer">최소환승</option>*/}
                    {/*        <option value="distance">최단거리</option>*/}
                    {/*    </select>*/}
                    {/*</div>*/}
                </section>

                {/* 결과 패널: 내부에서 fetch + 카운트다운 */}
                {ready ? (
                    <RouteArrivalsSection
                        fromName={fromName}
                        toName={toName}
                        // searchType={criteria}
                        when={new Date().toISOString() } /* 필요시 ISO 문자열 등 전달 */
                    />
                ) : (
                    <section className="panel" style={{ textAlign: "center", color: "#6b7280" }}>
                        출발지와 도착지를 선택하세요.
                    </section>
                )}
            </div>

            <footer className="footer">
                <div className="muted-small">지하철 실시간 알리미 · v0.1</div>
                {registeredDeviceIdentifier && (
                    <div className="muted-small" style={{ color: "#94a3b8" }}>
                        유저 ID {ellipsize(registeredDeviceIdentifier)}
                    </div>
                )}
                <div className="muted-small">
                    © {new Date().getFullYear()} ·{" "}
                    <a
                        href="https://qr.kakaopay.com/FTvfwZnrm"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="coffee-link"
                    >
                        ☕ 카카오페이로 커피 한 잔 사주기
                    </a>
                </div>
            </footer>
        </div>
    );
}

/* --- 보조 컴포넌트 --- */

function ellipsize(str = "", head = 6, tail = 4) {
    if (!str || str.length <= head + tail + 1) return str;
    return `${str.slice(0, head)}…${str.slice(-tail)}`;
}
