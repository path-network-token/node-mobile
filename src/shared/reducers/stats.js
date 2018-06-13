// @flow
import { combineReducers } from 'redux';

import {
  STATS_PENDING,
  STATS_SUCCESS,
  STATS_FAILURE
} from '../actions/constants';

const initialStats = {
  jobCompleteCount: 0,
  pathMinedCount: 0
};

const statsInit = (state: any = initialStats, action: any) => {
  switch (action.type) {
    case STATS_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const statsPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case STATS_PENDING:
      return true;
    case STATS_SUCCESS:
    case STATS_FAILURE:
      return false;
    default:
      return state;
  }
};

const errorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case STATS_FAILURE:
      return action.message;
    case STATS_PENDING:
    case STATS_SUCCESS:
      return '';
    default:
      return state;
  }
};

const stats = combineReducers({
  statsInit,
  statsPending,
  errorMessage
});

export default stats;

export const getStats = (state: any) => state.stats;
export const getStatsPending = (state: any) => state.statsPending;
export const getErrorMessage = (state: any) => state.errorMessage;
