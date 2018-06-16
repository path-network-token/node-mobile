// @flow
import { combineReducers } from 'redux';
import device, * as fromDevice from './device';
import job, * as fromJob from './job';
import options, * as fromOptions from './options';
import socketClient, * as fromSocketClient from './socketClient';
import stats, * as fromStats from './stats';

const reducers = combineReducers({
  device,
  job,
  options,
  socketClient,
  stats
});

export default reducers;

export const getDevice = (state: any) => fromDevice.getInfo(state.device);
export const getDeviceErrorMessage = (state: any) =>
  fromDevice.getErrorMessage(state.device);

export const getJob = (state: any) => fromJob.getJobSuccess(state.job);
export const getJobErrorMessage = (state: any) =>
  fromJob.getErrorMessage(state.job);

export const getOptions = (state: any) => fromOptions.getOptions(state.options);
export const getOptionsErrorMessage = (state: any) =>
  fromOptions.getErrorMessage(state.options);

export const getSocketInit = (state: any) =>
  fromSocketClient.getSocketInit(state.socketClient);
export const getSocketInitErrorMessage = (state: any) =>
  fromSocketClient.getSocketInitErrorMessage(state.socketClient);

export const getSocketUpdateLoc = (state: any) =>
  fromSocketClient.getSocketUpdateLoc(state.socketClient);
export const getSocketUpdateLocErrorMessage = (state: any) =>
  fromSocketClient.getSocketInitErrorMessage(state.socketClient);

export const getSocketReceiveJob = (state: any) =>
  fromSocketClient.getSocketReceiveJob(state.socketClient);
export const getSocketReceiveJobErrorMessage = (state: any) =>
  fromSocketClient.getSocketReceiveJobErrorMessage(state.socketClient);

export const getSocketSubmitJob = (state: any) =>
  fromSocketClient.getSocketSubmitJob(state.socketClient);
export const getSocketSubmitJobErrorMessage = (state: any) =>
  fromSocketClient.getSocketSubmitJobErrorMessage(state.socketClient);

export const getStats = (state: any) => fromStats.getStats(state.stats);
