// @flow
import { AsyncStorage } from 'react-native';

const init = socket => {
  socket.on('connect', e => {
    console.log('CONNECTED:', e);
  });

  socket.on('reconnect', e => {
    console.log('RECONNECT:', e);
  });

  socket.on('connect_error', e => {
    console.log('CONNECT ERROR:', e);
  });

  socket.on('connect_timeout', e => {
    console.log('CONNECT TIMEOUT:', e);
  });

  socket.on('error', e => {
    console.log('ERROR:', e);
  });
};

const socketClient = {
  init
};

export default socketClient;
