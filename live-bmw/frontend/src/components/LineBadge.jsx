import React from "react";
import { getLineBadgeTheme } from "../utils/lineTheme";

/**
 * props:
 * - lineId: "1002" | 1002
 * - lineName: "2호선" 등
 * - label: 뱃지 안에 표시할 텍스트 (기본: lineName || lineId)
 * - size: "sm" | "md"
 * - style: 인라인 스타일 확장
 */
export function LineBadge({ lineId, lineName, label, size = "sm", style }) {
    const badge = getLineBadgeTheme(lineId, lineName);
    const text = label ?? (lineName || lineId || "—");
    const pad = size === "md" ? "6px 10px" : "2px 8px";
    const font = size === "md" ? 12 : 12;

    return (
        <span
            style={{
                display: "inline-flex",
                alignItems: "center",
                gap: 6,
                padding: pad,
                borderRadius: 999,
                fontSize: font,
                fontWeight: 700,
                background: badge.bg,
                color: badge.fg,
                border: `1px solid ${badge.bd}`,
                ...style,
            }}
        >
      <span
          style={{
              width: 8,
              height: 8,
              borderRadius: 999,
              background: badge.dot,
              flex: "0 0 auto",
          }}
      />
            {text}
    </span>
    );
}
