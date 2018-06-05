import React, { Component } from 'react';
import { StyleSheet, Text, ImageBackground } from 'react-native';

import Banner from './src/components/Banner';
import Info from './src/containers/Info';

export default class App extends Component {
  render() {
    return (
      <ImageBackground
        source={require('./src/assets/images/3_bg.jpg')}
        style={styles.container}
      >
        <Banner />
      </ImageBackground>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  }
});
