// @flow
import { AsyncStorage } from 'react-native';
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

import reducers from '../reducers';

import initStorage from './initStorage';
import createInitState from './createInitState';

const reduxStore = async () => {
  const storage = await AsyncStorage.getItem('persistent');
  const initialState = createInitState(storage || initStorage);
  const middlewares = [thunk];

  return createStore(reducers, initialState, applyMiddleware(...middlewares));
};

export default reduxStore;
