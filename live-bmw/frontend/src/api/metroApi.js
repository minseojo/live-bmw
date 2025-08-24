import {post} from './baseApi';

// 라우트 응답 전체 변환
function adaptRoute(res) {
    return {
        lineId: res.lineId,
        lineName: res.lineName,
        originStation: res.originStation,
        destinationStation: res.destinationStation,
        direction: res.direction,
        nextStation: res.nextStation,
        arrivals: (res.arrivals || []).map(adaptArrival),
    };
}


// 서버 DTO 1건 → 프론트 호환 1건
function adaptArrival(arrival) {
    const eta = Number.isFinite(arrival?.etaSeconds) ? Math.max(0, arrival.etaSeconds) : null;
    return {
        // 레거시/컴포넌트 호환 키
        lineId: arrival.lineId,
        lineName: arrival.lineName,
        trainNo: arrival.trainNumber,                 // ← 핵심
        trainNumber: arrival.trainNumber,             // ← 추가
        direction: arrival.direction,
        trainLineSummary: arrival.trainLineSummary,
        arrivalMessage: arrival.message,
        arrivalMessageSub: arrival.messageSub,
        minutesLeft: eta != null ? Math.floor(eta / 60) : null,
        secondsLeft: eta != null ? (eta % 60) : null,

        // 새 키도 같이 보관해두면 나중에 쉽게 전환 가능
        etaSeconds: eta,
        receivedAt: arrival.receivedAt,
    };
}


// 사용 예시
export async function fetchRouteArrivals(req) {
    const raw = await post("/api/metro/arrivals/route", {
        originStation: req.originStationName,
        destinationStation: req.destinationStationName,
        lineId: req.lineIdentifier,
        resultLimit: req.resultLimit,
    });
    return adaptRoute(raw);
}