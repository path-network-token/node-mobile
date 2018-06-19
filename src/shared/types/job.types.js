type Device = 'android' | 'ios' | 'chrome' | 'firefox' | 'desktop';

type CheckIn = {
  id: string,
  type: 'check-in',
  miner_id?: string,
  cidr?: string,
  asn?: string,
  lat?: number,
  lon?: number,
  wallet: string,
  device_type: Device
};

type Ack = {
  id: string,
  type: 'ack',
  miner_id?: string
};

type Error = {
  id: string,
  type: 'error',
  description: string
};

type Headers = {
  [string]: string
};

type CriticalResponses = {
  header_status: string,
  body_contains: string
};

export type MinerJobRequest = {
  id: string,
  type: 'job-request',
  job_type: string,
  protocol: string,
  headers: Headers,
  payload: string,
  endpoint_address: string,
  endpoint_port: number,
  endpoint_additional_params: string,
  polling_interval: number,
  degraded_after: number,
  critical_after: number,
  critical_responses: CriticalResponses,
  job_uuid: string
};

type Status = 'critical' | 'degraded' | 'ok' | 'unknown';

export type JobResult = {
  id: string,
  type: 'job-result',
  job_uuid: string,
  status: Status,
  response_time: number
};
