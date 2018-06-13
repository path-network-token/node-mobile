// @flow
export type PersistantStorage = {
  // settings
  options: {
    walletAddress: string,
    wifiOnly: boolean
  },
  stats: {
    jobCompleteCount: number,
    pathMinedCount: number
  }
};

export type DynamicStorage = {
  job: {
    state: string,
    result: any // sort this out
  },
  device: {
    ip: string,
    asn: string
  },
  options: {
    walletAddress: string,
    wifiOnly: boolean
  },
  stats: {
    jobCompleteCount: number,
    pathMinedCount: number
  }
};
