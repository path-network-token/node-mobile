// @flow
// TODO update flow typing

import {
  SOCKET_CONNECTED,
  SOCKET_DISCONNECTED,
  SOCKET_SET_MINER_ID,
  SOCKET_CHECK_IN,
  SOCKET_RECEIVE_JOB,
  SOCKET_JOB_RESULTS,
  SOCKET_SERVER_ERROR,
  SOCKET_PING,
  SOCKET_PONG
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

export const submitJobSuccess: (data: any) => any = data => ({
  type: SOCKET_JOB_RESULTS,
  results: data.results
});

export const minerCheckIn: (data: any) => any = data => ({
  type: SOCKET_CHECK_IN,
  deviceId: data.deviceId,
  asn: data.asn,
  lat: data.lat,
  lng: data.lng
});

export const minerSetId: (data: any) => any = data => ({
  type: SOCKET_SET_MINER_ID,
  deviceId: data.deviceId
});

export const serverError: (data: any) => any = data => ({
  type: SOCKET_SERVER_ERROR,
  message: data.message
});

export const serverPing: (data: any) => any = data => ({
  type: SOCKET_PING
});

export const serverPong: (data: any) => any = data => ({
  type: SOCKET_PONG
});
