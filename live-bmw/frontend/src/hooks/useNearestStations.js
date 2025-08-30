// 근처역 로딩을 캡슐화한 훅
import { useCallback, useState } from "react";
import { getBrowserLocation } from "./useGeoLocation";
import { fetchNearestStations } from "../api/metroApi";

export function useNearestStations({ limit = 5, seedStations = [] } = {}) {
    const [list, setList] = useState(seedStations);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const loadNearby = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            const { lat, lng } = await getBrowserLocation();
            const res = await fetchNearestStations({ lat, lng, limit });
            if (!Array.isArray(res) || res.length === 0) {
                throw new Error("근처 역을 찾지 못했습니다.");
            }
            setList(res);
        } catch (e) {
            setError(e?.message || "근처역 조회 실패");
        } finally {
            setLoading(false);
        }
    }, [limit]);

    return { list, loading, error, loadNearby, setList };
}
