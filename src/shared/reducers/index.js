// @flow
import { AsyncStorage } from 'react-native';
import { combineReducers } from 'redux';

import { UPDATE_STATE, SET_OPTIONS } from '../actions';

export default function api(
  state = { persistent: { options: { initCount: 1 } } },
  action
) {
  switch (action.type) {
    case UPDATE_STATE:
      const newState = Object.assign({}, state, action.state);

      AsyncStorage.setItem('persistent', JSON.stringify(newState.persistent));

      return newState;
    case SET_OPTIONS:
      return Object.assign({}, state, {
        persistent: { options: action.options }
      });
    default:
      return state;
  }
}
