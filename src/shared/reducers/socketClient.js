// @flow
import { combineReducers } from 'redux';

import {
  SOCKET_INIT_PENDING,
  SOCKET_INIT_SUCCESS,
  SOCKET_INIT_FAILURE,
  SOCKET_UPDATE_LOCATION_PENDING,
  SOCKET_UPDATE_LOCATION_SUCCESS,
  SOCKET_UPDATE_LOCATION_FAILURE,
  SOCKET_RECEIVE_JOB_PENDING,
  SOCKET_RECEIVE_JOB_SUCCESS,
  SOCKET_RECEIVE_JOB_FAILURE,
  SOCKET_SUBMIT_JOB_PENDING,
  SOCKET_SUBMIT_JOB_SUCCESS,
  SOCKET_SUBMIT_JOB_FAILURE
} from '../actions/constants';

// ------------------------------------
// Socket init
//
const initialSocketInit = {};

const socketInit = (state: any = initialSocketInit, action: any) => {
  switch (action.type) {
    case SOCKET_INIT_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const socketInitPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case SOCKET_INIT_PENDING:
      return true;
    case SOCKET_INIT_SUCCESS:
    case SOCKET_INIT_FAILURE:
      return false;
    default:
      return state;
  }
};

const socketInitErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_INIT_FAILURE:
      return action.message;
    case SOCKET_INIT_PENDING:
    case SOCKET_INIT_SUCCESS:
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
    case SOCKET_UPDATE_LOCATION_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const socketUpdateLocPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case SOCKET_UPDATE_LOCATION_PENDING:
      return true;
    case SOCKET_UPDATE_LOCATION_SUCCESS:
    case SOCKET_UPDATE_LOCATION_FAILURE:
      return false;
    default:
      return state;
  }
};

const socketUpdateLocErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_UPDATE_LOCATION_FAILURE:
      return action.message;
    case SOCKET_UPDATE_LOCATION_PENDING:
    case SOCKET_UPDATE_LOCATION_SUCCESS:
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
    case SOCKET_RECEIVE_JOB_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const socketReceiveJobPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case SOCKET_RECEIVE_JOB_PENDING:
      return true;
    case SOCKET_RECEIVE_JOB_SUCCESS:
    case SOCKET_RECEIVE_JOB_FAILURE:
      return false;
    default:
      return state;
  }
};

const socketReceiveJobErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_RECEIVE_JOB_FAILURE:
      return action.message;
    case SOCKET_RECEIVE_JOB_PENDING:
    case SOCKET_RECEIVE_JOB_SUCCESS:
      return '';
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
    case SOCKET_SUBMIT_JOB_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const socketSubmitJobPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case SOCKET_SUBMIT_JOB_PENDING:
      return true;
    case SOCKET_SUBMIT_JOB_SUCCESS:
    case SOCKET_SUBMIT_JOB_FAILURE:
      return false;
    default:
      return state;
  }
};

const socketSubmitJobErrorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case SOCKET_SUBMIT_JOB_FAILURE:
      return action.message;
    case SOCKET_SUBMIT_JOB_PENDING:
    case SOCKET_SUBMIT_JOB_SUCCESS:
      return '';
    default:
      return state;
  }
};

const socketClient = combineReducers({
  socketInit,
  socketInitPending,
  socketInitErrorMessage,
  socketUpdateLoc,
  socketUpdateLocPending,
  socketUpdateLocErrorMessage,
  socketReceiveJob,
  socketReceiveJobPending,
  socketReceiveJobErrorMessage,
  socketSubmitJob,
  socketSubmitJobPending,
  socketSubmitJobErrorMessage
});

export default socketClient;

export const getSocketInit = (state: any) => state.socketInit;
export const getSocketInitPending = (state: any) => state.socketInitPending;
export const getSocketInitErrorMessage = (state: any) =>
  state.socketInitErrorMessage;

export const getSocketUpdateLoc = (state: any) => state.socketUpdateLoc;
export const getSocketUpdateLocPending = (state: any) =>
  state.socketUpdateLocPending;
export const getSocketUpdateLocErrorMessage = (state: any) =>
  state.socketUpdateLocErrorMessage;

export const getSocketReceiveJob = (state: any) => state.socketReceiveJob;
export const getSocketReceiveJobPending = (state: any) =>
  state.socketReceiveJobPending;
export const getSocketReceiveJobErrorMessage = (state: any) =>
  state.socketReceiveJobErrorMessage;

export const getSocketSubmitJob = (state: any) => state.socketSubmitJob;
export const getSocketSubmitJobPending = (state: any) =>
  state.socketSubmitJobPending;
export const getSocketSubmitJobErrorMessage = (state: any) =>
  state.socketSubmitJobErrorMessage;
