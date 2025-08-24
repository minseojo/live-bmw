export const API_BASE_URL = import.meta.env.VITE_API_BASE ?? "https://api.livebmw.cloud/api";


function buildUrl(path, query) {
    const url = new URL(path, API_BASE_URL || window.location.origin);
    if (query) Object.entries(query).forEach(([k, v]) => {
        if (v !== undefined && v !== null) url.searchParams.append(k, v);
    });
    return url.toString();
}


function tryParseJson(text) {
    try { return text ? JSON.parse(text) : null; } catch { return null; }
}


/** 공통 fetch 래퍼: 기본 헤더, 타임아웃, 에러 표준화, deviceId 헤더 자동 첨부 */
export async function apiFetch(path, { method = 'GET', headers = {}, body, query, timeoutMs = 15000, credentials = 'same-origin' } = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);


    const finalHeaders = { Accept: 'application/json', ...headers };
    let payload = body;


    // JSON 자동 직렬화
    const isJsonBody = body && typeof body === 'object' && !(body instanceof FormData);
    if (isJsonBody && !finalHeaders['Content-Type']) {
        finalHeaders['Content-Type'] = 'application/json; charset=utf-8';
        payload = JSON.stringify(body);
    }


    // deviceId 헤더 자동 첨부(있을 때만)
    try {
        const deviceId = localStorage.getItem('deviceId');
        if (deviceId && !finalHeaders['X-Device-Id']) finalHeaders['X-Device-Id'] = deviceId;
    } catch {
        console.log("error");
    }


    const url = buildUrl(path, query);


    try {
        const res = await fetch(url, { method, headers: finalHeaders, body: payload, signal: controller.signal, credentials });
        const text = await res.text();
        const json = tryParseJson(text);
        if (!res.ok) {
            const err = new Error(json?.message || text || `HTTP ${res.status}`);
            err.status = res.status; err.data = json; err.url = url; err.method = method;
            throw err;
        }
        return json ?? true; // 204 같은 경우 true 반환
    } finally {
        clearTimeout(timer);
    }
}


export const get = (path, opts) => apiFetch(path, { ...opts, method: 'GET' });
export const post = (path, body, opts) => apiFetch(path, { ...opts, method: 'POST', body });
export const del = (path, opts) => apiFetch(path, { ...opts, method: 'DELETE' });