import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import chromeExtension from '../reducers/api';

const createStoreWithMiddleware = applyMiddleware(thunk)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(chromeExtension, initialState);
}
