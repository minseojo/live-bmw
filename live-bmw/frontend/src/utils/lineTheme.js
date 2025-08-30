import { getLineColor } from "../constants/lineColors";

/** hex → rgba */
function hexToRgba(hex, alpha = 1) {
    if (!hex) return `rgba(0,0,0,${alpha})`;
    const m = hex.replace("#", "");
    const r = parseInt(m.length === 3 ? m[0] + m[0] : m.slice(0, 2), 16);
    const g = parseInt(m.length === 3 ? m[1] + m[1] : m.slice(2, 4), 16);
    const b = parseInt(m.length === 3 ? m[2] + m[2] : m.slice(4, 6), 16);
    return `rgba(${r},${g},${b},${alpha})`;
}

/** 라인ID/이름 → 배지 색 세트 */
export function getLineBadgeTheme(lineId, lineName) {
    const color = getLineColor(lineId, lineName);
    return {
        bg: hexToRgba(color, 0.12), // 연한 배경
        fg: color,                  // 텍스트/아이콘
        bd: hexToRgba(color, 0.28), // 테두리
        dot: color,                 // 점
    };
}
