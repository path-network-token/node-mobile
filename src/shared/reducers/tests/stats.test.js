import reducers from '../../reducers';
import * as actions from '../../actions/constants';
import expect from 'expect';
import defaultState from './defaultState';

describe('stats reducer', () => {
  it('should return the initial state', () => {
    const initialState = {
      ...defaultState
    };

    expect(reducers(undefined, {})).toEqual(initialState);
  });

  it('should handle STATS_SET_JOB_COUNT', () => {
    const statsData = {
      jobCompleteCount: 12334
    };

    const expectedState = {
      ...defaultState,
      stats: statsData
    };

    const pendingAction = {
      type: actions.STATS_SET_JOB_COUNT,
      jobCompleteCount: 12334
    };
    expect(reducers({}, pendingAction)).toEqual(expectedState);
  });

  it('should handle STATS_INCREMENT_JOB_COUNT', () => {
    const expectedState = {
      ...defaultState,
      stats: {
        jobCompleteCount: 1
      }
    };

    const successAction = {
      type: actions.STATS_INCREMENT_JOB_COUNT
    };

    expect(reducers({}, successAction)).toEqual(expectedState);
  });
});
