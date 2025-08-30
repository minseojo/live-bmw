import { chosung, normalizeName } from "../utils/korean.js";

/** stations -> 인덱스 확장(_key, _cho) */
export function buildIndex(stations) {
    const seen = new Set();
    return stations
        .filter((s) => s && s.station_id != null && !seen.has(s.station_id) && seen.add(s.station_id))
        .map((s) => ({
            ...s,
            _key: normalizeName(s.station_name || ""),
            _cho: chosung(s.station_name || ""),
        }));
}

/** 경량 점수 기반 검색 (완전일치 > 접두사 > 포함 > 초성일치) */
export function searchStations(index, q, limit = 12) {
    if (!q) return [];
    const nq = normalizeName(q);
    const cq = chosung(q);
    const scored = [];
    for (const it of index) {
        let score = Infinity;
        if (it._key === nq) score = 0;
        else if (it._key.startsWith(nq)) score = 1;
        else if (it._key.includes(nq)) score = 2;
        else if (cq && it._cho.includes(cq)) score = 3;
        if (score !== Infinity) scored.push({ score, it });
    }
    return scored
        .sort((a, b) => a.score - b.score || a.it.station_name.localeCompare(b.it.station_name, "ko"))
        .slice(0, limit)
        .map((x) => x.it);
}
