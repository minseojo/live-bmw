import React, { useEffect, useMemo, useRef, useState } from "react";
import { useStationSearch } from "../hooks/useStationSearch";
import { getLineColor } from "../constants/lineColors";
import "./RouteStationInput.css";

export function RouteStationInput({
                                      stations,
                                      placeholder = "역 이름",
                                      value,
                                      lineId,
                                      onChangeText,
                                      onSelectStation,
                                      className = "",
                                  }) {
    const [editing, setEditing] = useState(!lineId);
    const { query, setQuery, results } = useStationSearch({ stations });
    const [open, setOpen] = useState(false);
    const [active, setActive] = useState(0);
    const wrapRef = useRef(null);
    const inputRef = useRef(null);

    // 외부 value 동기화
    useEffect(() => { setQuery(value || ""); }, [value, setQuery]);

    // 외부 lineId 변화 시 칩/인풋 모드 자동 전환
    useEffect(() => { setEditing(!lineId); }, [lineId]);

    // 바깥 클릭 시 닫기
    useEffect(() => {
        const onDoc = (e) => { if (!wrapRef.current?.contains(e.target)) setOpen(false); };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    const selectedMeta = useMemo(() => {
        if (!value || !lineId) return null;
        const hit =
            stations.find(
                s =>
                    (s.station_name || s.stationName) === value &&
                    String(s.line_id || s.lineId) === String(lineId)
            ) || null;
        if (!hit) return null;
        return {
            stationName: hit.station_name || hit.stationName || value,
            lineId: String(hit.line_id || hit.lineId || lineId),
            lineName: hit.line_name || hit.lineName || "",
        };
    }, [stations, value, lineId]);

    const apply = (item) => {
        onSelectStation?.(item);  // 상위에서 name/lineId 세팅
        setQuery(item.station_name || item.stationName || "");
        setOpen(false);
        setEditing(false);
    };

    return (
        <div className={`rsi-wrap ${className}`} ref={wrapRef}>
            {!editing && selectedMeta ? (
                <button
                    type="button"
                    className="rsi-chip"
                    onClick={() => { setEditing(true); setTimeout(()=>inputRef.current?.focus(), 0); }}
                    style={{
                        color: getLineColor(selectedMeta.lineId, selectedMeta.lineName),
                        borderColor: getLineColor(selectedMeta.lineId, selectedMeta.lineName),
                        background: `${getLineColor(selectedMeta.lineId, selectedMeta.lineName)}1A`,
                    }}
                    title="수정하려면 탭"
                >
          <span
              className="rsi-chip-dot"
              style={{ background: getLineColor(selectedMeta.lineId, selectedMeta.lineName) }}
          />
                    {(selectedMeta.lineName ? selectedMeta.lineName + " " : "") + selectedMeta.stationName}
                </button>
            ) : (
                <>
                    <input
                        ref={inputRef}
                        className="rsi-input"
                        placeholder={placeholder}
                        value={query}
                        onFocus={() => setOpen(true)}
                        onChange={(e) => { setQuery(e.target.value); onChangeText?.(e.target.value); setOpen(true); }}
                        onKeyDown={(e) => {
                            if (!open || results.length === 0) return;
                            if (e.key === "ArrowDown") { e.preventDefault(); setActive(a => Math.min(a + 1, results.length - 1)); }
                            if (e.key === "ArrowUp")   { e.preventDefault(); setActive(a => Math.max(a - 1, 0)); }
                            if (e.key === "Enter")     { e.preventDefault(); apply(results[active]); }
                            if (e.key === "Escape")    { setOpen(false); }
                        }}
                    />
                    {open && results.length > 0 && (
                        <ul className="rsi-panel">
                            {results.map((s, i) => {
                                const li = String(s.line_id || s.lineId || "");
                                const ln = s.line_name || s.lineName || "";
                                const color = getLineColor(li, ln);
                                return (
                                    <li
                                        key={s._key || `${s.station_name || s.stationName}-${li}-${i}`}
                                        className={`rsi-item ${i === active ? "is-active" : ""}`}
                                        onMouseEnter={() => setActive(i)}
                                        onMouseDown={(e) => { e.preventDefault(); apply(s); }}
                                    >
                    <span className="rsi-badge" style={{ color, borderColor: color, background: `${color}14` }}>
                      <i className="rsi-dot" style={{ background: color }} />
                        {ln || li}
                    </span>
                                        <span className="rsi-name">{s.station_name || s.stationName}</span>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </>
            )}
        </div>
    );
}
