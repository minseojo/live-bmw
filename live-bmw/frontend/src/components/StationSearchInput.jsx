import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useStationSearch } from "../hooks/useStationSearch";
import { LineBadge } from "./LineBadge";
import "./StationSearchInput.css";

export function StationSearchInput({
                                       stations,
                                       placeholder = "역 이름",
                                       value,
                                       onChange,
                                       onSelect,
                                   }) {
    const { query, setQuery, results } = useStationSearch({ stations });
    const [open, setOpen] = useState(false);
    const [active, setActive] = useState(0);

    const [rect, setRect] = useState(null); // {top,left,width}
    const inputRef = useRef(null);
    const wrapRef = useRef(null);

    // 외부 value 동기화
    useEffect(() => { setQuery(value || ""); }, [value, setQuery]);

    // 외부 클릭 닫기
    useEffect(() => {
        const onDoc = (e) => { if (!wrapRef.current?.contains(e.target)) setOpen(false); };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    // 위치 계산 (viewport 기준)
    useLayoutEffect(() => {
        if (!open) return;
        const update = () => {
            const el = inputRef.current;
            if (!el) return;
            const r = el.getBoundingClientRect();
            const vw = window.innerWidth || document.documentElement.clientWidth;
            const margin = 12;
            const width = Math.min(r.width, Math.floor(vw * 0.92));
            let left = Math.max(margin, Math.min(r.left, vw - width - margin));
            const top = Math.round(r.bottom + 6);
            setRect({ top, left: Math.round(left), width: Math.round(width) });
        };
        update();
        window.addEventListener("resize", update);
        window.addEventListener("scroll", update, true);
        return () => {
            window.removeEventListener("resize", update);
            window.removeEventListener("scroll", update, true);
        };
    }, [open]);

    const apply = (item) => {
        const name = item?.station_name || item?.stationName || "";
        onChange?.({ target: { value: name } });
        onSelect?.(item);
        setQuery(name);
        setOpen(false);
    };

    // 드롭다운 노드
    const panel = open && results.length > 0 && rect
        ? createPortal(
            <ul
                className="ssi-panel"
                style={{
                    position: "fixed",
                    top: rect.top,
                    left: rect.left,
                    width: rect.width,
                    zIndex: 10000,       // 스태킹 이슈 완전 차단
                }}
            >
                {results.map((s, i) => (
                    <li
                        key={s._key || `${s.station_name || s.stationName}-${i}`}
                        className={`ssi-item ${i === active ? "is-active" : ""}`}
                        onMouseEnter={() => setActive(i)}
                        onMouseDown={(e) => { e.preventDefault(); apply(s); }}
                    >
                        <LineBadge
                            lineId={s.line_id || s.lineId}
                            lineName={s.line_name || s.lineName}
                            label={s.line_name || s.lineName}
                            size="sm"
                        />
                        <span className="ssi-name">{s.station_name || s.stationName}</span>
                    </li>
                ))}
            </ul>,
            document.body
        )
        : null;

    return (
        <div className="ssi-wrap" ref={wrapRef}>
            <input
                ref={inputRef}
                className="ssi-input"
                placeholder={placeholder}
                value={query}
                onChange={(e) => { setQuery(e.target.value); onChange?.(e); setOpen(true); }}
                onFocus={() => setOpen(true)}
                onKeyDown={(e) => {
                    if (!open || results.length === 0) return;
                    if (e.key === "ArrowDown") { e.preventDefault(); setActive((a) => Math.min(a + 1, results.length - 1)); }
                    if (e.key === "ArrowUp")   { e.preventDefault(); setActive((a) => Math.max(a - 1, 0)); }
                    if (e.key === "Enter")     { e.preventDefault(); apply(results[active]); }
                    if (e.key === "Escape")    { setOpen(false); }
                }}
            />
            {panel}
        </div>
    );
}
