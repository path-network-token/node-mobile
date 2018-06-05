// @flow
import React from 'react';
import { Text, View, Image } from 'react-native';

class Banner extends React.Component {
  render() {
    return (
      <View>
        <Image
          source={require('../assets/images/pathtoplogo.png')}
          style={{ width: 253, height: 220 }}
        />
      </View>
    );
  }
}

export default Banner;
