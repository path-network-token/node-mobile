import reducer from './getPostReducer';
import * as actions from '../actions/posts/getPost';
import { UPDATE_POST_SUCCESS } from '../actions/posts/updatePost';
import expect from 'expect';

describe('device reducer', () => {
  it('should return the initial state', () => {
    expect(reducer(undefined, {})).toEqual({});
  });

  it('should handle DEVICE_INFO_SUCCESS', () => {
    const successAction = {
      type: actions.DEVICE_INFO_SUCCESS,
      post: getPostMock.data
    };
    expect(reducer({}, successAction)).toEqual(getPostMock.data);
  });

  it('should handle DEVICE_INFO_FAILURE', () => {
    const failAction = {
      type: actions.DEVICE_INFO_FAILURE,
      error: { success: false }
    };
    expect(reducer({}, failAction)).toEqual({ error: { success: false } });
  });
});
