const API_BASE = import.meta.env.VITE_API_BASE ?? "https://api.livebmw.cloud/api";

async function request(path, { method = "GET", body, headers, signal } = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        method,
        headers: {
            "Content-Type": "application/json",
            ...(headers || {}),
        },
        body: body ? JSON.stringify(body) : undefined,
        signal,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}${text ? `: ${text}` : ""}`);
    }

    // 204 = no content
    if (res.status === 204) return null;

    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return res.json();
    }
    return res.text(); // JSON이 아니면 문자열 반환
}

/** 디바이스 조회 후 check-in (성공 시 디바이스 정보 반환) */
export async function getDevice(deviceId) {
    // 1) 조회
    const device = await request(`/devices/${encodeURIComponent(deviceId)}`);

    // 2) 체크인 (응답 204 기대)
    await request(`/devices/${encodeURIComponent(deviceId)}/check-in`, { method: "POST" });

    return device;
}

/** 디바이스 등록 (없으면 생성, 있으면 200/409 정책에 맞춰 처리) */
export async function registerDevice(deviceId) {
    return request("/devices", {
        method: "POST",
        body: { deviceId }
    });
}

/** 디바이스 삭제 */
export async function deleteDevice(deviceId) {
    await request(`/devices/${encodeURIComponent(deviceId)}`, { method: "DELETE" });
    return true;
}
