import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
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
    const [alignRight, setAlignRight] = useState(false);
    const [panelWidth, setPanelWidth] = useState(null);

    const wrapRef = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => { setQuery(value || ""); }, [value, setQuery]);

    useEffect(() => {
        const handleDoc = (e) => {
            if (!wrapRef.current?.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", handleDoc);
        return () => document.removeEventListener("mousedown", handleDoc);
    }, []);

    useLayoutEffect(() => {
        if (!open) return;
        const update = () => {
            const anchor = inputRef.current;
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

    const apply = (item) => {
        const name = item?.station_name || item?.stationName || "";
        onChange?.({ target: { value: name } });
        onSelect?.(item);
        setQuery(name);
        setOpen(false);
    };

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

            {open && results.length > 0 && (
                <ul
                    className={`ssi-panel ${alignRight ? "is-right" : "is-left"}`}
                    style={{ width: panelWidth ?? "100%" }}
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
                </ul>
            )}
        </div>
    );
}
