import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { fetchNearestStations } from "../api/metroApi";
import { getBrowserLocation } from "../hooks/useGeoLocation";
import { LineBadge } from "./LineBadge";
import "./NearestStationsButton.css";

export function NearestStationsButton({ seedStations = [], limit = 5, onApply }) {
    const [open, setOpen] = useState(false);
    const [stations, setStations] = useState(seedStations);
    const btnRef = useRef(null);
    const [rect, setRect] = useState(null);

    useEffect(() => { setStations(seedStations); }, [seedStations]);

    useEffect(() => {
        const onDoc = (e) => { if (!btnRef.current?.parentElement?.contains(e.target)) setOpen(false); };
        document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, []);

    useLayoutEffect(() => {
        if (!open) return;
        const update = () => {
            const el = btnRef.current;
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

    const toggle = async () => {
        if (!open && stations.length === 0) {
            try {
                const coords = await getBrowserLocation();
                const list = await fetchNearestStations({ lat: coords.lat, lng: coords.lng, limit });
                setStations(list);
            } catch (e) { /* ignore */ }
        }
        setOpen((v) => !v);
    };

    const panel = open && stations.length > 0 && rect
        ? createPortal(
            <ul
                className="nsb-panel"
                style={{
                    position: "fixed",
                    top: rect.top,
                    left: rect.left,
                    width: rect.width,
                    zIndex: 10000,
                }}
            >
                {stations.map((s, i) => (
                    <li
                        key={`${s.stationName || s.station_name}-${i}`}
                        className="nsb-item"
                        onMouseDown={(e) => { e.preventDefault(); onApply?.(s); setOpen(false); }}
                    >
                        <LineBadge
                            lineId={s.lineId || s.line_id}
                            lineName={s.lineName || s.line_name}
                            label={s.lineName || s.line_name}
                            size="sm"
                        />
                        <span className="nsb-name">{s.stationName || s.station_name}</span>
                        <span className="nsb-dist">{s.distanceM} m</span>
                    </li>
                ))}
            </ul>,
            document.body
        )
        : null;

    return (
        <div className="nsb-wrap">
            <button ref={btnRef} className="nsb-btn" onClick={toggle}>주변 역 목록</button>
            {panel}
        </div>
    );
}
