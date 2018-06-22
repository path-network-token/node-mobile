// @flow
import type { DynamicStorage } from '../types/storage.types.js';

export default function createInitState(storage: any) {
  return {
    job: {},
    device: {},
    socketClient: {},
    ...storage
  };
}
