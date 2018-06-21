// @flow
import { combineReducers } from 'redux';

import {
  STATS_SET_JOB_COUNT,
  STATS_INCREMENT_JOB_COUNT
} from '../actions/constants';

const initialStats = {
  jobCompleteCount: 0
};

const stats = (state: any = initialStats, action: any) => {
  switch (action.type) {
    case STATS_SET_JOB_COUNT:
      return {
        jobCompleteCount: action.jobCompleteCount
      };
    case STATS_INCREMENT_JOB_COUNT:
      return {
        jobCompleteCount: state.jobCompleteCount + 1
      };
    default:
      return state;
  }
};

export default stats;

export const getStats = (state: any) => state.stats;
