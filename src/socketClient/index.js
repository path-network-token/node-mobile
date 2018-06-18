// @flow
import { AsyncStorage } from 'react-native';
import io from 'socket.io-client';

import {
  socketConnected,
  socketDisconnected,
  receiveJob,
  submitJob,
  submitJobFail,
  updateLocation,
  updateLocationFail,
  minerInitSuccess,
  minerInitFail
} from '../shared/actions/socketClient';

const socketURL = 'http://localhost:3000/';

const socket = io(socketURL);

const socketClient = store => {
  const { dispatch } = store;

  socket.on('connect', e => {
    console.log('CONNECTED:', e);

    const data = {
      deviceId: '123456'
    };
    dispatch(socketConnected());
  });

  setupErrorHandlers(dispatch);

  // incoming job request
  socket.on('job', e => {
    console.log('JOB:', e);
    dispatch(receiveJob(dispatch));
  });
};

const emitInitMiner = data => {
  socket.emit('init', data, response => {});
};

const emitNewLocation = data => {
  socket.emit('miner_location', data);
};

const emitJobResult = data => {
  socket.emit('results', data);
};

// TODO: Should we give user feedback if connection drops?
const setupErrorHandlers = dispatch => {
  socket.on('reconnect', e => {
    console.log('RECONNECT:', e);
    dispatch(socketConnected());
  });

  socket.on('connect_error', e => {
    console.log('CONNECT ERROR:', e);
    dispatch(socketDisconnected());
  });

  socket.on('connect_timeout', e => {
    console.log('CONNECT TIMEOUT:', e);
    dispatch(socketDisconnected());
  });

  socket.on('error', e => {
    console.log('ERROR:', e);

    // will depend on whether job or location or init
    dispatch(minerInitFail({ message: 'Submit Job Failure' }));
    dispatch(updateLocationFail({ message: 'Submit Job Failure' }));
    dispatch(submitJobFail({ message: 'Submit Job Failure' }));
  });
};

export default socketClient;
