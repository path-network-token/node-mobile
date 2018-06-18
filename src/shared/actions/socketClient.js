// @flow
// TODO update flow typing

import {
  SOCKET_CONNECTED,
  SOCKET_DISCONNECTED,
  SOCKET_INIT,
  SOCKET_INIT_FAILURE,
  SOCKET_UPDATE_LOCATION,
  SOCKET_UPDATE_LOCATION_FAILURE,
  SOCKET_RECEIVE_JOB,
  SOCKET_SUBMIT_JOB,
  SOCKET_SUBMIT_JOB_FAILURE
} from './constants';

export const socketConnected: () => any = () => ({
  type: SOCKET_CONNECTED
});

export const socketDisconnected: () => any = () => ({
  type: SOCKET_DISCONNECTED
});

export const receiveJob: (data: any) => any = data => ({
  type: SOCKET_RECEIVE_JOB,
  jobData: data.jobData
});

export const submitJob: (data: any) => any = data => ({
  type: SOCKET_SUBMIT_JOB,
  results: data.results
});

export const submitJobFail: (data: any) => any = data => ({
  type: SOCKET_SUBMIT_JOB_FAILURE,
  message: data.message
});

export const updateLocation: (data: any) => any = data => ({
  type: SOCKET_UPDATE_LOCATION,
  deviceId: data.deviceId,
  asn: data.asn,
  lat: data.lat,
  lng: data.lng
});

export const updateLocationFail: (data: any) => any = data => ({
  type: SOCKET_UPDATE_LOCATION_FAILURE,
  message: data.message
});

export const minerInitSuccess: (data: any) => any = data => ({
  type: SOCKET_INIT,
  deviceId: data.deviceId
});

export const minerInitFail: (data: any) => any = data => ({
  type: SOCKET_INIT_FAILURE,
  message: data.message
});
