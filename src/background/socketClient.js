// @flow
import { AsyncStorage } from 'react-native';

import io from 'socket.io-client';

const socket = io('http://nodejs.nyc.path.network:3000/path');

// // eslint-disable-next-line
// chrome.runtime.onMessage.addListener((req, sender, sendResponse) => {
//   if (req.action === 'updateState') {
//     store.dispatch({
//       type: 'UPDATE_STATE',
//       state: req.state
//     });
//   }
//
//   if (req.action === 'getState') {
//     sendResponse(store.getState());
//   }
// });

socket.on('connect', () => {
  const index = socket.io.engine.upgrade ? 1 : 0;
  console.info(socket.io.engine.transports[index] + '.');
});

socket.on('message-all', data => {
  console.info(data);
});

socket.on('message-room', data => {
  console.info(data);
});
