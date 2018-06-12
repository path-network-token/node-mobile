// @flow
import { combineReducers } from 'redux';

import {
  DEVICE_INFO_PENDING,
  DEVICE_INFO_SUCCESS,
  DEVICE_INFO_FAILURE
} from '../actions/constants';

const initialInfo = {
  id: '',
  lat: '',
  lng: '',
  ip: '',
  asn: ''
};

const info = (state: any = initialInfo, action: any) => {
  switch (action.type) {
    case DEVICE_INFO_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const infoPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case DEVICE_INFO_PENDING:
      return true;
    case DEVICE_INFO_SUCCESS:
    case DEVICE_INFO_FAILURE:
      return false;
    default:
      return state;
  }
};

const errorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case DEVICE_INFO_FAILURE:
      return action.message;
    case DEVICE_INFO_PENDING:
    case DEVICE_INFO_SUCCESS:
      return '';
    default:
      return state;
  }
};

const device = combineReducers({
  info,
  infoPending,
  errorMessage
});

export default device;

export const getInfo = (state: any) => state.info;
export const getInfoPending = (state: any) => state.infoPending;
export const getErrorMessage = (state: any) => state.errorMessage;
