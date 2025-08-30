import { useEffect, useLayoutEffect, useState } from "react";

export function usePortalPosition(anchorRef, { offsetY = 6 } = {}) {
    const [rect, setRect] = useState(null);

    const update = () => {
        const el = anchorRef.current;
        if (!el) return;
        const r = el.getBoundingClientRect();
        setRect({
            top: Math.round(r.bottom + offsetY),
            left: Math.round(r.left),
            width: Math.round(r.width),
        });
    };

    const useIso = typeof window !== "undefined" ? useLayoutEffect : useEffect;
    useIso(update, [anchorRef.current]);

    useEffect(() => {
        update();
        const onScroll = () => update();
        const onResize = () => update();
        window.addEventListener("scroll", onScroll, true);
        window.addEventListener("resize", onResize);
        return () => {
            window.removeEventListener("scroll", onScroll, true);
            window.removeEventListener("resize", onResize);
        };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    return rect;
}
