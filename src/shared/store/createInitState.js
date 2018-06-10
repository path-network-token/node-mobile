// @flow
import type { PersistantStorage } from '../types/storage.types.js';

export default function createInitState(storage: PersistantStorage) {
  return {
    jobStatus: 'Inactive',
    activeJob: null,
    jobQueue: [],
    count: 50,
    ...storage
  };
}
