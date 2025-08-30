import React from "react";
import { getLineColor } from "../constants/lineColors";

export function RouteChip({ lineId, lineName, stationName }) {
    const color = getLineColor(lineId, lineName);

    return (
        <span
            style={{
                display: "inline-flex",
                alignItems: "center",
                gap: 6,
                padding: "4px 10px",
                borderRadius: 999,
                fontSize: 13,
                fontWeight: 700,
                background: `${color}20`, // hex 투명 배경
                color: color,
                border: `1px solid ${color}60`,
            }}
        >
      {/* 작은 점 */}
            <span
                style={{
                    width: 8,
                    height: 8,
                    borderRadius: "50%",
                    background: color,
                    flexShrink: 0,
                }}
            />
            {`${lineName} ${stationName}`}
    </span>
    );
}
