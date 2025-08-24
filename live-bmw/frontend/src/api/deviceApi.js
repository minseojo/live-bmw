import { get, post, del } from './baseApi';


export const registerDevice = (deviceIdentifier) =>
    post('/api/devices', { deviceId: deviceIdentifier });


export const getDeviceById = (deviceIdentifier) =>
    get(`/api/devices/${encodeURIComponent(deviceIdentifier)}`);


export const deleteDeviceById = (deviceIdentifier) =>
    del(`/api/devices/${encodeURIComponent(deviceIdentifier)}`);