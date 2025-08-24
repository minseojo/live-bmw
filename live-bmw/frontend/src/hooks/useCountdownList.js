import { useEffect, useState } from "react";

function getEtaSeconds(it) {
    if (Number.isFinite(it?.etaSeconds)) return it.etaSeconds;
    if (Number.isFinite(it?.etaSec))     return it.etaSec;
    if (Number.isFinite(it?.minutesLeft)) {
        const s = Number.isFinite(it.secondsLeft) ? it.secondsLeft : 0;
        return it.minutesLeft * 60 + s;
    }
    return null;
}

export function useCountdownList(list = []) {
    const [items, setItems] = useState(list);

    // 입력 리스트 바뀌면 그대로 반영(+ 파생 필드 채우기)
    useEffect(() => {
        setItems(list.map(it => {
            const base = getEtaSeconds(it);
            if (base == null) return { ...it };
            const m = Math.floor(base / 60), s = base % 60;
            return { ...it, etaSeconds: base, etaSec: base, minutesLeft: m, secondsLeft: s };
        }));
    }, [list]);

    // 1초마다 카운트다운 (모든 필드 보존)
    useEffect(() => {
        if (!items.length) return;
        const id = setInterval(() => {
            setItems(prev => prev.map(it => {
                const base = getEtaSeconds(it);
                if (base == null) return it;                     // 필드 유지, 카운트다운만 생략
                const next = Math.max(0, base - 1);
                return {
                    ...it,                                         // ★ 기존 모든 키 유지
                    etaSeconds: next,
                    etaSec: next,
                    minutesLeft: Math.floor(next / 60),
                    secondsLeft: next % 60,
                };
            }));
        }, 1000);
        return () => clearInterval(id);
    }, [items.length]);

    return items;
}
