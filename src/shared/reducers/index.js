// @flow
import { combineReducers } from 'redux';
import device, * as fromDevice from './device';
import jobs, * as fromJobs from './job';
import socketClient, * as fromSocketClient from './socketClient';

const reducers = combineReducers({
  jobs,
  socketClient
});

export default reducers;

export const getDevice = (state: any) => fromDevice.getInfo(state.device);
export const getDevicePending = (state: any) =>
  fromDevice.getInfoPending(state.device);
export const getDeviceErrorMessage = (state: any) =>
  fromDevice.getErrorMessage(state.device);

export const getJob = (state: any) => fromJobs.getJob(state.job);
export const getJobPending = (state: any) => fromJobs.getJobPending(state.job);
export const getJobErrorMessage = (state: any) =>
  fromJobs.getErrorMessage(state.job);

export const getSocketInit = (state: any) =>
  fromSocketClient.getSocketInit(state.socketClient);
export const getSocketInitPending = (state: any) =>
  fromSocketClient.getSocketInitPending(state.socketClient);
export const getSocketInitErrorMessage = (state: any) =>
  fromSocketClient.getSocketInitErrorMessage(state.socketClient);

export const getSocketUpdateLoc = (state: any) =>
  fromSocketClient.getSocketUpdateLoc(state.socketClient);
export const getSocketUpdateLocPending = (state: any) =>
  fromSocketClient.getSocketUpdateLocPending(state.socketClient);
export const getSocketUpdateLocErrorMessage = (state: any) =>
  fromSocketClient.getSocketInitErrorMessage(state.socketClient);

export const getSocketReceiveJob = (state: any) =>
  fromSocketClient.getSocketReceiveJob(state.socketClient);
export const getSocketReceiveJobPending = (state: any) =>
  fromSocketClient.getSocketReceiveJobPending(state.socketClient);
export const getSocketReceiveJobErrorMessage = (state: any) =>
  fromSocketClient.getSocketReceiveJobErrorMessage(state.socketClient);

export const getSocketSubmitJob = (state: any) =>
  fromSocketClient.getSocketSubmitJob(state.socketClient);
export const getSocketSubmitJobPending = (state: any) =>
  fromSocketClient.getSocketSubmitJobPending(state.socketClient);
export const getSocketSubmitJobErrorMessage = (state: any) =>
  fromSocketClient.getSocketSubmitJobErrorMessage(state.socketClient);
