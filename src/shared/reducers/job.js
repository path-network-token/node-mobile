// @flow
import { combineReducers } from 'redux';

import { JOB_PENDING, JOB_SUCCESS, JOB_FAILURE } from '../actions/constants';

const jobSuccess = (state: any = {}, action: any) => {
  switch (action.type) {
    case JOB_SUCCESS:
      return {
        result_uuid: action.result_uuid,
        customer_uuid: action.customer_uuid,
        miner_id: action.miner_id,
        job_uuid: action.job_uuid,
        geo: action.geo,
        asn: action.asn,
        ip_range: action.ip_range,
        received_on: action.received_on,
        status: action.status,
        response_time: action.response_time
      };
    default:
      return {};
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
  jobSuccess,
  jobPending,
  errorMessage
});

export default job;

export const getJobSuccess = (state: any) => state.jobSuccess;
export const getJobPending = (state: any) => state.jobPending;
export const getErrorMessage = (state: any) => state.errorMessage;
