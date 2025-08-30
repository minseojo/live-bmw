import React, { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useStationSearch } from "../hooks/useStationSearch";
import { usePortalPosition } from "../hooks/usePortalPosition";
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

    const wrapRef = useRef(null);
    const inputRef = useRef(null);
    const rect = usePortalPosition(inputRef, { offsetY: 6 });

    useEffect(() => { setQuery(value || ""); }, [value, setQuery]);

    useEffect(() => {
        const onDoc = (e) => {
            if (!wrapRef.current?.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    const apply = (item) => {
        const name = item?.station_name || item?.stationName || "";
        onChange?.({ target: { value: name } });
        onSelect?.(item);
        setQuery(name);
        setOpen(false);
    };

    const Panel = (
        <ul
            className="ssi-panel"
            style={{
                position: "fixed",
                top: rect?.top ?? -9999,
                left: rect?.left ?? -9999,
                width: rect?.width ?? undefined,
                zIndex: 200, // 검색 드롭다운: 최상위
            }}
        >
            {results.length === 0 ? (
                <li className="ssi-item ssi-empty">결과 없음</li>
            ) : (
                results.map((s, i) => (
                    <li
                        key={s._key || `${s.station_name || s.stationName}-${i}`}
                        className={`ssi-item ${i === active ? "is-active" : ""}`}
                        onMouseEnter={() => setActive(i)}
                        onMouseDown={(e) => { e.preventDefault(); apply(s); }}
                    >
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <LineBadge
                                lineId={s.line_id || s.lineId}
                                lineName={s.line_name || s.lineName}
                                label={s.line_name || s.lineName}
                                size="sm"
                            />
                            <span className="ssi-name">{s.station_name || s.stationName}</span>
                        </div>
                    </li>
                ))
            )}
        </ul>
    );

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
            {open && rect && ReactDOM.createPortal(Panel, document.body)}
        </div>
    );
}
