// @flow
// device actions
import { OPTIONS_SUCCESS, OPTIONS_FAILURE } from './constants';

export const updateDeviceInfo: (data: any) => any = data => ({
  type: OPTIONS_SUCCESS,
  data: {
    walletAddress: data.walletAddress,
    wifiOnly: data.wifiOnly
  }
});

export const updateOptionsFail: (data: any) => any = data => ({
  type: OPTIONS_FAILURE,
  message: 'Failure setting user options'
});
