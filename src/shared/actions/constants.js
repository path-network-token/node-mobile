// @flow
// Action constants

// device related
export const DEVICE_INFO_SUCCESS = 'DEVICE_INFO_SUCCESS';
export const DEVICE_INFO_FAILURE = 'DEVICE_INFO_FAILURE';

// job related actions
export const JOB_PENDING = 'JOB_PENDING';
export const JOB_SUCCESS = 'JOB_SUCCESS';
export const JOB_FAILURE = 'JOB_FAILURE';

// options
export const OPTIONS_SUCCESS = 'OPTIONS_SUCCESS';
export const OPTIONS_FAILURE = 'OPTIONS_FAILURE';

export const SOCKET_CONNECTED = 'SOCKET_CONNECTED';
export const SOCKET_DISCONNECTED = 'SOCKET_DISCONNECTED';

// connections via socket client
export const SOCKET_INIT = 'SOCKET_INIT';
export const SOCKET_INIT_FAILURE = 'SOCKET_INIT_FAILURE';

// update miner details via socket client
export const SOCKET_UPDATE_LOCATION = 'SOCKET_UPDATE_LOCATION';
export const SOCKET_UPDATE_LOCATION_FAILURE = 'SOCKET_UPDATE_LOCATION_FAILURE';

// receive job via socket client
export const SOCKET_RECEIVE_JOB = 'SOCKET_RECEIVE_JOB';

// submit job result via socket client
export const SOCKET_SUBMIT_JOB = 'SOCKET_SUBMIT_JOB';
export const SOCKET_SUBMIT_JOB_FAILURE = 'SOCKET_SUBMIT_JOB_FAILURE';

// options
export const STATS_SET_JOB_COUNT = 'STATS_SET_JOB_COUNT';
export const STATS_INCREMENT_JOB_COUNT = 'STATS_INCREMENT_JOB_COUNT';
