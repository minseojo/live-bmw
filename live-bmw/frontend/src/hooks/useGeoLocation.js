export async function getBrowserLocation() {
    if (!("geolocation" in navigator)) {
        throw new Error("이 기기에서 위치를 사용할 수 없어요");
    }
    return new Promise((resolve, reject) =>
        navigator.geolocation.getCurrentPosition(
            pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
            () => reject(new Error("위치 권한을 허용해주세요")),
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
        )
    );
}
