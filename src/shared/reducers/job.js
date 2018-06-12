// @flow
import { combineReducers } from 'redux';

import { JOB_PENDING, JOB_SUCCESS, JOB_FAILURE } from '../actions/constants';

const initialJob = {};

const jobInit = (state: any = initialJob, action: any) => {
  switch (action.type) {
    case JOB_SUCCESS:
      return action.response.result;
    default:
      return state;
  }
};

const jobPending = (state: boolean = false, action: any) => {
  switch (action.type) {
    case JOB_PENDING:
      return true;
    case JOB_SUCCESS:
    case JOB_FAILURE:
      return false;
    default:
      return state;
  }
};

const errorMessage = (state: string = '', action: any) => {
  switch (action.type) {
    case JOB_FAILURE:
      return action.message;
    case JOB_PENDING:
    case JOB_SUCCESS:
      return '';
    default:
      return state;
  }
};

const job = combineReducers({
  jobInit,
  jobPending,
  errorMessage
});

export default job;

export const getJob = (state: any) => state.job;
export const getJobPending = (state: any) => state.jobPending;
export const getErrorMessage = (state: any) => state.errorMessage;
