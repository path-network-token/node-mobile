// @flow
import { combineReducers } from 'redux';

import { OPTIONS_SUCCESS, OPTIONS_FAILURE } from '../actions/constants';

const initialOptions = {
  walletAddress: '',
  wifiOnly: false
};

const optionsInit = (state: any = initialOptions, action: any) => {
  switch (action.type) {
    case OPTIONS_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const errorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case OPTIONS_FAILURE:
      return action.message;
    case OPTIONS_SUCCESS:
      return '';
    default:
      return state;
  }
};

const options = combineReducers({
  optionsInit,
  errorMessage
});

export default options;

export const getOptions = (state: any) => state.options;
export const getErrorMessage = (state: any) => state.errorMessage;
