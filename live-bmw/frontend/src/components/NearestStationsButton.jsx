import React, { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useNearestStations } from "../hooks/useNearestStations";
import { usePortalPosition } from "../hooks/usePortalPosition";
import { LineBadge } from "./LineBadge";
import "./NearestStationsButton.css";

export function NearestStationsButton({
                                          seedStations = [],
                                          limit = 5,
                                          onApply,
                                          label = "주변 역 목록",
                                      }) {
    const { list, loading, error, loadNearby, setList } = useNearestStations({ limit, seedStations });
    const [open, setOpen] = useState(false);

    const wrapRef = useRef(null);
    const btnRef = useRef(null);
    const rect = usePortalPosition(btnRef, { offsetY: 6 });

    useEffect(() => { if (seedStations?.length) setList(seedStations); }, [seedStations, setList]);

    useEffect(() => {
        const onDoc = (e) => {
            if (!wrapRef.current?.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    useEffect(() => {
        if (open && (!list || list.length === 0) && !loading) void loadNearby();
    }, [open, list, loading, loadNearby]);

    const Panel = (
        <div
            className="nsb-panel"
            style={{
                position: "fixed",
                top: rect?.top ?? -9999,
                left: rect?.left ?? -9999,
                width: rect?.width ?? 320,
                zIndex: 100, // 주변역 목록: 두 번째
            }}
        >
            {loading && <div className="nsb-row nsb-muted">가까운 역을 찾는 중…</div>}
            {error && <div className="nsb-row nsb-error">{error}</div>}

            {!loading && !error && list?.length > 0 && (
                <ul className="nsb-list">
                    {list.map((s, i) => (
                        <li key={`${s.lineName}-${s.stationName}-${s.distanceM}-${i}`} className="nsb-item">
                            <div className="nsb-meta">
                                <LineBadge
                                    lineId={s.lineId ?? s.line_id}
                                    lineName={s.lineName}
                                    label={s.lineName}
                                    size="sm"
                                />
                                <span className="nsb-name">{s.stationName}</span>
                                <span className="nsb-dist">{s.distanceM} m</span>
                            </div>
                            <button
                                className="nsb-apply"
                                onClick={() => { onApply?.(s); setOpen(false); }}
                                title="출발지로 선택"
                            >
                                출발지로 선택
                            </button>
                        </li>
                    ))}
                </ul>
            )}

            {!loading && !error && (!list || list.length === 0) && (
                <div className="nsb-empty">
                    근처역 정보가 없습니다.
                    <button className="nsb-retry" onClick={() => loadNearby()}>다시 시도</button>
                </div>
            )}
        </div>
    );

    return (
        <div className="nsb-wrap" ref={wrapRef}>
            <button ref={btnRef} className="nsb-trigger" onClick={() => setOpen(v => !v)} aria-expanded={open}>
                {label}
            </button>
            {open && rect && ReactDOM.createPortal(Panel, document.body)}
        </div>
    );
}
