const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

/** 디바이스 조회 후 check-in (성공 시 디바이스 정보 반환) */
export async function getDevice(deviceId) {
    // 1) 조회
    const resp = await fetch(`${API_BASE}/devices/${encodeURIComponent(deviceId)}`, {
        method: "GET"
    });

    if (!resp.ok) {
        // 404면 상위에서 register 흐름 태움
        throw new Error(`등록 실패: ${resp.status}`);
    }

    // 2) 체크인 (바디 필요 없으면 생략; 204 기대)
    const checkIn = await fetch(`${API_BASE}/devices/${encodeURIComponent(deviceId)}/check-in`, {
        method: "POST"
    });
    if (!checkIn.ok) {
        throw new Error(`체크인 실패: ${checkIn.status}`);
    }

    // 3) 디바이스 정보 반환 (200/JSON 가정)
    //   응답이 204면 .json() 호출 금지!
    const contentType = resp.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return await resp.json();
    }
    return null; // 혹은 필요 시 resp.text()
}

/** 디바이스 등록 (없으면 생성, 있으면 200/409 정책에 맞춰 처리) */
export async function registerDevice(deviceId) {
    const resp = await fetch(`${API_BASE}/devices`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ deviceId })
    });

    if (!resp.ok) {
        throw new Error(`등록 실패: ${resp.status}`);
    }

    // 201/200 + JSON 가정. 204가 오면 null 반환.
    if ((resp.headers.get("content-type") || "").includes("application/json")) {
        return await resp.json();
    }
    return null;
}

/** 디바이스 삭제 */
export async function deleteDevice(deviceId) {
    const resp = await fetch(`${API_BASE}/devices/${encodeURIComponent(deviceId)}`, {
        method: "DELETE"
    });
    if (!resp.ok) {
        throw new Error(`삭제 실패: ${resp.status}`);
    }
    // 보통 204
    return true;
}
