import reducers from '../../reducers';
import * as actions from '../../actions/constants';
import expect from 'expect';
import defaultState from './defaultState';

describe('device reducer', () => {
  it('should return the initial state', () => {
    const initialState = {
      ...defaultState
    };

    expect(reducers(undefined, {})).toEqual(initialState);
  });

  it('should handle DEVICE_INFO_SUCCESS', () => {
    const deviceData = {
      id: 'testID',
      lat: '-37.0',
      lng: '37.0',
      ip: '1.2.3.4',
      asn: '100.200.0.0'
    };

    const expectedState = {
      ...defaultState,
      device: {
        errorMessage: '',
        info: deviceData
      }
    };

    const successAction = {
      type: actions.DEVICE_INFO_SUCCESS,
      data: deviceData
    };
    expect(reducers({}, successAction)).toEqual(expectedState);
  });

  it('should handle DEVICE_INFO_FAILURE', () => {
    const expectedState = {
      ...defaultState,
      device: {
        errorMessage: 'testing',
        info: {
          asn: '',
          id: '',
          ip: '',
          lat: '',
          lng: ''
        }
      }
    };

    const failAction = {
      type: actions.DEVICE_INFO_FAILURE,
      message: 'testing'
    };
    expect(reducers({}, failAction)).toEqual(expectedState);
  });
});
