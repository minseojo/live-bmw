// src/api.js
const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

export async function getDevice(deviceId) {
    const res = await fetch(`${API_BASE}/devices/${deviceId}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    });

    console.log(res);
    if (!res.ok) {
        throw new Error(`등록 실패: ${res.status}`);
    }
    return res.json();
}


export async function registerDevice(deviceId) {
    const res = await fetch(`${API_BASE}/devices`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ deviceId }),
    });

    console.log(res);
    if (!res.ok) {
        throw new Error(`등록 실패: ${res.status}`);
    }
    return res.json();
}

export async function deleteDevice(deviceId) {
    const res = await fetch(`${API_BASE}/devices/${deviceId}`, {
        method: "DELETE",
    });
    if (!res.ok) {
        throw new Error(`삭제 실패: ${res.status}`);
    }
    return true;
}
