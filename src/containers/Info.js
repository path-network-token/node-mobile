// @flow
import * as React from 'react';
import { View } from 'react-native';
import { connect } from 'react-redux';

import reducers from '../shared/reducers';

import { incrementJobCount } from '../shared/actions';

import InfoBox from '../components/InfoBox';

type Props = {
  jobCompleteCount: number,
  jobStatus: boolean
};

class Info extends React.Component<Props> {
  render() {
    const { jobCompleteCount, jobStatus } = this.props;

    return (
      <View>
        <InfoBox prefix="Jobs completed: " data={jobCompleteCount} />
        <InfoBox prefix="Status: " data={jobStatus} />
      </View>
    );
  }
}

const mapStateToProps = state => {
  return {
    jobCompleteCount: state.stats.jobCompleteCount,
    jobStatus: state.job.jobPending
  };
};

export default connect(mapStateToProps)(Info);
