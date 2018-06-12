// @flow
import { AsyncStorage } from 'react-native';
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

import type { PersistantStorage } from '../types/storage.types.js';
import api from '../reducers';

import initStorage from './initStorage';
import createInitState from './createInitState';

const reduxStore = async () => {
  let storage = await AsyncStorage.getItem('persistent');

  if (storage == null) {
    storage = initStorage;
  }

  const initialState = createInitState(storage);
  const middlewares = [thunk];

  return createStore(api, initialState, applyMiddleware(...middlewares));
};

export default reduxStore;
