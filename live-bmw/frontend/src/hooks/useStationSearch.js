// 간단한 로컬 검색 훅 (디바운스 + startsWith 우선, includes 보조)
import { useEffect, useMemo, useRef, useState } from "react";

// 정규화: 소문자/공백/괄호 제거
function norm(s = "") {
    return s.toString().trim().toLowerCase().replace(/\s+/g, "").replace(/[()]/g, "");
}

export function useStationSearch({ stations, delay = 150, limit = 8 } = {}) {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState([]);
    const timerRef = useRef(null);

    // 검색 인덱스 전처리
    const index = useMemo(() => {
        const list = Array.isArray(stations) ? stations : [];
        return list.map((s) => {
            const name = s.station_name ?? s.stationName ?? "";
            const line = s.line_name ?? s.lineName ?? "";
            return {
                ...s,
                _key: `${name}|${line}|${s.station_id ?? s.stationId ?? ""}`,
                _n: norm(`${name}${line}`),
            };
        });
    }, [stations]);

    useEffect(() => {
        if (timerRef.current) clearTimeout(timerRef.current);
        timerRef.current = setTimeout(() => {
            const q = norm(query);
            if (!q || index.length === 0) {
                setResults([]);
                return;
            }

            const starts = [];
            const includes = [];
            for (const s of index) {
                if (!s._n) continue;
                if (s._n.startsWith(q)) starts.push(s);
                else if (s._n.includes(q)) includes.push(s);
            }
            setResults([...starts, ...includes].slice(0, limit));
        }, delay);

        return () => timerRef.current && clearTimeout(timerRef.current);
    }, [query, index, delay, limit]);

    return { query, setQuery, results };
}
