import reducers from '../../reducers';
import * as actions from '../../actions/constants';
import expect from 'expect';
import defaultState from './defaultState';

describe('job reducer', () => {
  it('should return the initial state', () => {
    const initialState = {
      ...defaultState
    };

    expect(reducers(undefined, {})).toEqual(initialState);
  });

  it('should handle JOB_PENDING', () => {
    const jobData = {
      errorMessage: '',
      jobPending: true,
      jobSuccess: {}
    };

    const expectedState = {
      ...defaultState,
      job: jobData
    };

    const pendingAction = {
      type: actions.JOB_PENDING
    };
    expect(reducers({}, pendingAction)).toEqual(expectedState);
  });

  it('should handle JOB_SUCCESS', () => {
    const jobData = {
      errorMessage: '',
      jobPending: false,
      jobSuccess: {
        result_uuid: '1234567',
        customer_uuid: '1234567',
        miner_id: '123456745678',
        job_uuid: '1234567jhsdagjhadg',
        geo: 'Testing',
        asn: 12345,
        ip_range: '12.12.12.12',
        received_on: 1234567890,
        status: 'ok',
        response_time: 12345
      }
    };

    const expectedState = {
      ...defaultState,
      job: jobData
    };

    const successAction = {
      type: actions.JOB_SUCCESS,
      result_uuid: '1234567',
      customer_uuid: '1234567',
      miner_id: '123456745678',
      job_uuid: '1234567jhsdagjhadg',
      geo: 'Testing',
      asn: 12345,
      ip_range: '12.12.12.12',
      received_on: 1234567890,
      status: 'ok',
      response_time: 12345
    };

    expect(reducers({}, successAction)).toEqual(expectedState);
  });

  it('should handle JOB_FAILURE', () => {
    const jobData = {
      errorMessage: 'job test fail message',
      jobPending: false,
      jobSuccess: {}
    };

    const expectedState = {
      ...defaultState,
      job: jobData
    };

    const failAction = {
      type: actions.JOB_FAILURE,
      message: 'job test fail message'
    };
    expect(reducers({}, failAction)).toEqual(expectedState);
  });
});
