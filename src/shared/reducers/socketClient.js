// @flow
import { combineReducers } from 'redux';

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
} from '../actions/constants';

// ------------------------------------
// Socket connection
//
const socketConnected = (state: boolean = false, action: any) => {
  switch (action.type) {
    case SOCKET_CONNECTED:
      return true;
    case SOCKET_DISCONNECTED:
      return false;
    default:
      return state;
  }
};

// ------------------------------------
// Socket init
//
const initialSocketInit = {
  id: ''
};

const socketInit = (state: any = initialSocketInit, action: any) => {
  switch (action.type) {
    case SOCKET_INIT:
      return action.response.result;
    default:
      return state;
  }
};

const socketInitErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_INIT_FAILURE:
      return action.message;
    case SOCKET_INIT:
      return '';
    default:
      return state;
  }
};

// ------------------------------------
// Socket update location
//
const initialSocketUpdateLoc = {};

const socketUpdateLoc = (state: any = initialSocketUpdateLoc, action: any) => {
  switch (action.type) {
    case SOCKET_UPDATE_LOCATION:
      return action.response.result;
    default:
      return state;
  }
};

const socketUpdateLocErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_UPDATE_LOCATION_FAILURE:
      return action.message;
    case SOCKET_UPDATE_LOCATION:
      return '';
    default:
      return state;
  }
};

// ------------------------------------
// Socket receive job
//
const initialSocketReceiveJob = {};

const socketReceiveJob = (
  state: any = initialSocketReceiveJob,
  action: any
) => {
  switch (action.type) {
    case SOCKET_RECEIVE_JOB:
      return action.response.result;
    default:
      return state;
  }
};

// ------------------------------------
// Socket submit job
//
const initialSocketSubmitJob = {};

const socketSubmitJob = (state: any = initialSocketSubmitJob, action: any) => {
  switch (action.type) {
    case SOCKET_SUBMIT_JOB:
      return action.response.result;
    default:
      return state;
  }
};

const socketSubmitJobErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_SUBMIT_JOB_FAILURE:
      return action.message;
    case SOCKET_SUBMIT_JOB:
      return '';
    default:
      return state;
  }
};

const socketClient = combineReducers({
  socketConnected,
  socketInit,
  socketInitErrorMessage,
  socketUpdateLoc,
  socketUpdateLocErrorMessage,
  socketReceiveJob,
  socketSubmitJob,
  socketSubmitJobErrorMessage
});

export default socketClient;

export const getSocketConnected = (state: any) => state.socketConnected;

export const getSocketInit = (state: any) => state.socketInit;
export const getSocketInitErrorMessage = (state: any) =>
  state.socketInitErrorMessage;

export const getSocketUpdateLoc = (state: any) => state.socketUpdateLoc;
export const getSocketUpdateLocErrorMessage = (state: any) =>
  state.socketUpdateLocErrorMessage;

export const getSocketReceiveJob = (state: any) => state.socketReceiveJob;

export const getSocketSubmitJob = (state: any) => state.socketSubmitJob;
export const getSocketSubmitJobErrorMessage = (state: any) =>
  state.socketSubmitJobErrorMessage;
