export const LINE_COLOR_BY_ID = {
    "1001": "#0052A4","1002": "#00A84D","1003": "#EF7C1C","1004": "#00A5DE",
    "1005": "#996CAC","1006": "#CD7C2F","1007": "#747F00","1008": "#E6186C",
    "1009": "#BDB092",
    "1032": "#B6007A",
    "1061": "#77C4A3","1063": "#77C4A3","1065": "#0090D2","1067": "#0C8E72",
    "1069": "#7CA8D5","1071": "#F5A200","1075": "#F5A200","1077": "#D4003B",
    "1078": "#ED8B00","1079": "#FDA600","1080": "#6FB245","1081": "#003DA5",
    "1091": "#B7BF10","1092": "#B7BF10","1093": "#8FC31F","1094": "#6789CA",
};

export const DEFAULT_LINE_COLOR = "#334155";

/** ID 또는 이름 기반으로 컬러 가져오기 */
export function getLineColor(lineId, lineName) {
    if (lineId && LINE_COLOR_BY_ID[lineId]) return LINE_COLOR_BY_ID[lineId];
    const s = String(lineName || "");
    if (/1호선/.test(s)) return LINE_COLOR_BY_ID["1001"];
    if (/2호선/.test(s)) return LINE_COLOR_BY_ID["1002"];
    if (/3호선/.test(s)) return LINE_COLOR_BY_ID["1003"];
    if (/4호선/.test(s)) return LINE_COLOR_BY_ID["1004"];
    if (/5호선/.test(s)) return LINE_COLOR_BY_ID["1005"];
    if (/6호선/.test(s)) return LINE_COLOR_BY_ID["1006"];
    if (/7호선/.test(s)) return LINE_COLOR_BY_ID["1007"];
    if (/8호선/.test(s)) return LINE_COLOR_BY_ID["1008"];
    if (/9호선/.test(s)) return LINE_COLOR_BY_ID["1009"];
    if (/신분당/.test(s)) return LINE_COLOR_BY_ID["1077"];
    if (/경의|중앙/.test(s)) return LINE_COLOR_BY_ID["1063"];
    if (/분당|수인/.test(s)) return LINE_COLOR_BY_ID["1075"];
    return DEFAULT_LINE_COLOR;
}
