import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
import { LineBadge } from "./LineBadge";
import "./NearestStationsButton.css";

export function NearestStationsButton({
                                          seedStations = [],
                                          limit = 5,
                                          onApply,
                                          label = "주변 역 목록",
                                      }) {
    const [open, setOpen] = useState(false);
    const [list, setList] = useState(seedStations || []);
    const [alignRight, setAlignRight] = useState(false);
    const [panelWidth, setPanelWidth] = useState(null);

    const wrapRef = useRef(null);
    const btnRef = useRef(null);

    useEffect(() => { if (seedStations?.length) setList(seedStations); }, [seedStations]);

    useEffect(() => {
        const onDoc = (e) => { if (!wrapRef.current?.contains(e.target)) setOpen(false); };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    useLayoutEffect(() => {
        if (!open) return;
        const update = () => {
            const anchor = btnRef.current;
            if (!anchor) return;
            const r = anchor.getBoundingClientRect();
            const vw = window.innerWidth || document.documentElement.clientWidth;
            const MIN = 360;
            const maxPx = Math.floor(vw * 0.92);
            const desired = Math.min(Math.max(r.width, MIN), maxPx);
            const margin = 8;
            const overflowRight = r.left + desired > vw - margin;
            setAlignRight(overflowRight);
            setPanelWidth(desired);
        };
        update();
        window.addEventListener("resize", update);
        window.addEventListener("scroll", update, true);
        return () => {
            window.removeEventListener("resize", update);
            window.removeEventListener("scroll", update, true);
        };
    }, [open]);

    return (
        <div className="nsb-wrap" ref={wrapRef}>
            <button ref={btnRef} className="nsb-trigger" onClick={() => setOpen(v => !v)}>
                {label}
            </button>

            {open && (
                <div
                    className={`nsb-panel ${alignRight ? "is-right" : "is-left"}`}
                    style={{ width: panelWidth ?? "100%" }}
                >
                    <ul className="nsb-list">
                        {list.slice(0, limit).map((s, i) => (
                            <li key={`${s.lineName}-${s.stationName}-${s.distanceM}-${i}`} className="nsb-item">
                                <div className="nsb-meta">
                                    <LineBadge
                                        lineId={s.lineId || s.line_id}
                                        lineName={s.lineName}
                                        label={s.lineName}
                                        size="sm"
                                    />
                                    <span className="nsb-name">{s.stationName}</span>
                                    <span className="nsb-dist">{s.distanceM} m</span>
                                </div>
                                <button className="nsb-apply" onClick={() => { onApply?.(s); setOpen(false); }}>
                                    출발지로 선택
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
