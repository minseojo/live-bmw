import {post} from './baseApi';
import {get} from './baseApi';

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


// =========================
// 최단경로 + 환승 도착 (신규)
// GET /api/shortestPath/with-arrivals?from=...&to=...&searchType=duration&when=...
// 응답 예시
// {
//   keyStations: [{ stationName, direction }, ...],
//   arrivalsByStation: {
//     "KeyStation[stationName=봉천, direction=외선]": [ { ...arrival }, ... ],
//     ...
//   }
// }

function buildKeyStationString(ks) {
    // 서버 키 포맷과 동일하게 생성
    return `KeyStation[stationName=${ks.stationName}, direction=${ks.direction}]`;
}

function adaptShortestPathResponse(res) {
    const keyStations = Array.isArray(res?.keyStations) ? res.keyStations : [];
    const arrivalsByStation = res?.arrivalsByStation || {};

    const groups = keyStations.map((ks) => {
        const key = buildKeyStationString(ks);
        const arrivals = Array.isArray(arrivalsByStation[key]) ? arrivalsByStation[key] : [];
        return {
            stationName: ks.stationName,
            direction: ks.direction,
            arrivals: arrivals.map(adaptArrival)
        };
    });

    // 편의상 평탄화 리스트도 제공
    const flatArrivals = groups.flatMap(g => g.arrivals.map(a => ({ ...a, _stationName: g.stationName, _direction: g.direction })));

    return {
        keyStations,
        groups,
        arrivalsByStation: groups.reduce((acc, g) => { acc[buildKeyStationString(g)] = g.arrivals; return acc; }, {}),
        flatArrivals,
    };
}

export async function fetchShortestPathWithArrivals({ from, to, when, searchType = 'duration' }) {
    const raw = await get('/api/shortestPath/with-arrivals', { query: { from, to, when, searchType } });
    return adaptShortestPathResponse(raw || {});
}

// 여러 기준을 병렬로 조회 (duration, distance, transfer)
export async function fetchMultiCriteriaShortestPaths({ from, to, when, criteriaList = ['duration'] }) {
    const unique = Array.from(new Set(criteriaList));
    const results = await Promise.all(unique.map(async (c) => {
        const data = await fetchShortestPathWithArrivals({ from, to, when, searchType: c });
        return [c, data];
    }));
    return Object.fromEntries(results);
}