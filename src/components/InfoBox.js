// @flow
import React from 'react';

type Props = {
  prefix: string,
  data: number | string,
  style: Object
};

class InfoBox extends React.Component<Props> {
  render() {
    return (
      <div className="info-box" style={this.props.style}>{`${
        this.props.prefix
      }${this.props.data}`}</div>
    );
  }
}

export default InfoBox;
