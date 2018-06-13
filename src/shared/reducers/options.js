// @flow
import { combineReducers } from 'redux';

import {
  OPTIONS_PENDING,
  OPTIONS_SUCCESS,
  OPTIONS_FAILURE
} from '../actions/constants';

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

const optionsPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case OPTIONS_PENDING:
      return true;
    case OPTIONS_SUCCESS:
    case OPTIONS_FAILURE:
      return false;
    default:
      return state;
  }
};

const errorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case OPTIONS_FAILURE:
      return action.message;
    case OPTIONS_PENDING:
    case OPTIONS_SUCCESS:
      return '';
    default:
      return state;
  }
};

const options = combineReducers({
  optionsInit,
  optionsPending,
  errorMessage
});

export default options;

export const getOptions = (state: any) => state.options;
export const getOptionsPending = (state: any) => state.optionsPending;
export const getErrorMessage = (state: any) => state.errorMessage;
