// @flow
import { STATS_INCREMENT_JOB_COUNT, STATS_SET_JOB_COUNT } from './constants';

export const incrementJobCount: () => any = () => ({
  type: STATS_INCREMENT_JOB_COUNT
});

export const setJobCount: (jobCount: number) => any = jobCount => ({
  type: STATS_SET_JOB_COUNT,
  jobCompleteCount: jobCount
});
