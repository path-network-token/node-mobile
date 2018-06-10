// @flow

export type PersistantStorage = {
  // settings
  walletAddress: string,
  wifiOnly: boolean,
  // statistics
  jobCompleteCount: number,
  pathMinedCount: number
};
