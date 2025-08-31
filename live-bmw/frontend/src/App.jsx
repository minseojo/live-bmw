import React, { useEffect, useState, useMemo } from "react";
import { useDeviceRegistration } from "./hooks/useDeviceRegistration";
import { getBrowserLocation } from "./hooks/useGeoLocation";
import { NearestStationsButton } from "./components/NearestStationsButton";
import { RouteStationInput } from "./components/RouteStationInput";
import { RouteArrivalsSection } from "./components/RouteArrivalsSection.jsx";

import { stations as stationsData } from "./data/stations.js"; // 로컬 자동완성 데이터
import "./App.css";
import { fetchNearestStations } from "./api/metroApi.js";

export default function App() {
    const {
        registeredDeviceIdentifier,
        deviceRegistrationErrorMessage,
        isDeviceRegistrationInProgress,
    } = useDeviceRegistration();

    // 환경 감지
    const isIOS = useMemo(() => {
        const ua = navigator.userAgent || navigator.vendor || "";
        return /iPad|iPhone|iPod/.test(ua);
    }, []);
    const isStandalone = useMemo(() => {
        // iOS: window.navigator.standalone
        // 기타: display-mode: standalone
        const mq = window.matchMedia?.("(display-mode: standalone)")?.matches;
        return !!(window.navigator?.standalone) || !!mq;
    }, []);

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

    // iOS 위치 버튼 진행상태/에러
    const [geoBusy, setGeoBusy] = useState(false);
    const [geoError, setGeoError] = useState("");

    // ===== iOS: 탭 이후에만 위치 요청 / 기타: 자동 요청 =====
    useEffect(() => {
        if (isIOS) return; // iOS는 버튼으로만 실행
        (async () => {
            try {
                const coords = await getBrowserLocation(); // { lat, lng }
                const list = await fetchNearestStations(coords.lat, coords.lng, 3);
                if (Array.isArray(list) && list.length > 0) {
                    const first = list[0];
                    setNearestStations(list);
                    setFromName(first.stationName ?? first.name);
                    setFromLineId(String(first.lineId || first.line_id || ""));
                    setFromSelected(true);
                }
            } catch (e) {
                console.error("위치 기반 출발지 자동 설정 실패:", e);
            }
        })();
    }, [isIOS]);

    // iOS에서 호출할 위치 요청 핸들러 (사용자 제스처 필수)
    const requestLocationOnce = async () => {
        setGeoError("");
        setGeoBusy(true);
        try {
            const coords = await getBrowserLocation(); // 내부에서 geolocation 호출
            const list = await fetchNearestStations(coords.lat, coords.lng, 3);
            if (Array.isArray(list) && list.length > 0) {
                const first = list[0];
                setNearestStations(list);
                setFromName(first.stationName ?? first.name);
                setFromLineId(String(first.lineId || first.line_id || ""));
                setFromSelected(true);
            } else {
                setGeoError("근처 역을 찾지 못했습니다. 수동으로 입력해 주세요.");
            }
        } catch (e) {
            console.error(e);
            // 사파리/홈앱 권한/정확도/차단 케이스 안내
            setGeoError(
                "위치 권한이 거부되었거나 차단되었습니다.\n" +
                "1) 설정 > Safari(또는 홈앱의 앱 항목) > 위치를 '앱을 사용하는 동안'으로 허용\n" +
                "2) '정확한 위치(Precise)' ON\n" +
                "후 버튼을 다시 눌러 주세요."
            );
        } finally {
            setGeoBusy(false);
        }
    };

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
                        {/* PWA 아이콘 png로 교체 */}
                        <img src="/icons/icon-192.png" alt="로고" width="32" height="32" />
                        <h2 className="title">지하철 실시간 알리미</h2>
                    </div>

                    {/* iOS에서 홈앱/브라우저 여부 안내 (선택사항) */}
                    {isIOS && (
                        <div className="muted-small" style={{ color: "#94a3b8" }}>
                            {isStandalone ? "홈 화면 앱 모드" : "Safari 모드"}
                        </div>
                    )}
                </header>

                {/* 검색 패널 */}
                <section className="panel panel-search route-form">
                    {/* 상단: 주변역 | 초기화 */}
                    <div className="route-top-row">
                        <NearestStationsButton
                            seedStations={nearestStations}
                            limit={5}
                            onApply={(s) => {
                                setFromName(s.stationName ?? s.name);
                                setFromLineId(String(s.lineId || s.line_id || ""));
                                setFromSelected(true);
                            }}
                        />

                        {/* iOS 전용: 위치 요청 버튼 (제스처 필요) */}
                        {isIOS && (
                            <button
                                className="btn primary"
                                onClick={requestLocationOnce}
                                disabled={geoBusy}
                                title="현재 위치로 근처 역 찾기 (iOS는 탭 필요)"
                                style={{ marginLeft: 8 }}
                            >
                                {geoBusy ? "위치 확인 중…" : "현재 위치로 찾기"}
                            </button>
                        )}

                        <button
                            className="reset-btn"
                            onClick={() => {
                                setFromName(""); setToName("");
                                setFromLineId(""); setToLineId("");
                                setFromSelected(false); setToSelected(false);
                                setGeoError("");
                            }}
                            style={{ marginLeft: 8 }}
                        >
                            ✕
                        </button>
                    </div>

                    {geoError && (
                        <div className="muted-small" style={{ color: "#ef4444", whiteSpace: "pre-line", marginTop: 6 }}>
                            {geoError}
                        </div>
                    )}

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
                                    setFromName(s.station_name || s.stationName || s.name);
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
                                    setToName(s.station_name || s.stationName || s.name);
                                    setToLineId(String(s.line_id || s.lineId || ""));
                                    setToSelected(true);
                                }}
                            />
                        </div>
                    </div>

                    {/* 기준 선택 (선택사항)
          <div className="route-criteria-row">
            <select
              value={criteria}
              onChange={(e) => setCriteria(e.target.value)}
              className="input select"
              style={{ width: "100%", marginTop: 8 }}
            >
              <option value="duration">최단시간</option>
              <option value="transfer">최소환승</option>
              <option value="distance">최단거리</option>
            </select>
          </div>
          */}
                </section>

                {/* 결과 패널: 내부에서 fetch + 카운트다운 */}
                {ready ? (
                    <RouteArrivalsSection
                        fromName={fromName}
                        toName={toName}
                        searchType={criteria}
                        when={undefined} /* 필요시 ISO 문자열 전달 */
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
