// @flow
import * as React from 'react';
import { View } from 'react-native';
import { connect } from 'react-redux';

import InfoBox from '../components/InfoBox';

type Props = {
  jobCompleteCount: number,
  pathMinedCount: number,
  jobStatus: string
};

class Info extends React.Component<Props> {
  render() {
    const { jobCompleteCount, pathMinedCount, jobStatus } = this.props;

    return (
      <View>
        <InfoBox prefix="Jobs completed: " data={jobCompleteCount} />
        <InfoBox prefix="Path mined: " data={pathMinedCount} />
        <InfoBox prefix="Status: " data={jobStatus} />
      </View>
    );
  }
}

const mapStateToProps = state => {
  return {
    jobCompleteCount: state.stats.jobCompleteCount,
    pathMinedCount: state.stats.pathMinedCount,
    jobStatus: state.job.status
  };
};

export default connect(mapStateToProps)(Info);
