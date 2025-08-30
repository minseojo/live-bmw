import {get} from './baseApi';

function adaptArrival(arrival) {
    const eta = Number.isFinite(arrival?.etaSeconds) ? Math.max(0, arrival.etaSeconds) : null;
    return {
        lineId: arrival.lineId,
        lineName: arrival.lineName,
        trainNo: arrival.trainNumber,
        trainNumber: arrival.trainNumber,
        direction: arrival.direction,
        trainLineSummary: arrival.trainLineSummary,
        arrivalMessage: arrival.message,
        arrivalMessageSub: arrival.messageSub,
        minutesLeft: eta != null ? Math.floor(eta / 60) : null,
        secondsLeft: eta != null ? (eta % 60) : null,
        etaSeconds: eta,
        receivedAt: arrival.receivedAt,
    };
}

function buildKeyStationString(ks) {
    return `KeyStation[stationName=${ks.stationName}, direction=${ks.direction}]`;
}

function adaptShortestPathResponse(res = {}) {
    const plan = res?.plan || null;
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

    const flatArrivals = groups.flatMap(g => g.arrivals.map(a => ({ ...a, _stationName: g.stationName, _direction: g.direction })));

    return {
        plan,
        keyStations,
        groups,
        arrivalsByStation: groups.reduce((acc, g) => { acc[buildKeyStationString(g)] = g.arrivals; return acc; }, {}),
        flatArrivals,
    };
}

/** 최단경로 + 환승 도착 */
export async function fetchShortestPathWithArrivals({ from, to, when, searchType = 'duration' }) {
    const raw = await get('/api/shortest-path/with-arrivals', { query: { from, to, when: when, searchType } });
    return adaptShortestPathResponse(raw || {});
}

/** 근처 역 조회 */
export async function fetchNearestStations(lat, lng, limit = 5) {
    if (lat == null || lng == null) {
        throw new Error("lat/lng is required");
    }
    return get("/api/metro/stations/nearest", {
        query: { lat, lng, limit }
    });
}