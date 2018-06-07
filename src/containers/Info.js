// @flow
import * as React from 'react';
import { View } from 'react-native';

import InfoBox from '../components/InfoBox';

type Props = {};

type State = {
  jobs: number,
  tokens: number,
  status: string
};

export default class Info extends React.Component<Props, State> {
  state = {
    jobs: 0,
    tokens: 0,
    status: 'Idle'
  };

  render() {
    const { jobs, tokens, status } = this.state;

    return (
      <View>
        <InfoBox prefix="Jobs completed: " data={jobs} />
        <InfoBox prefix="Path mined: " data={tokens} />
        <InfoBox prefix="Status: " data={status} />
      </View>
    );
  }
}
