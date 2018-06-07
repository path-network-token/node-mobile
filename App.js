// @flow
import React, { Component } from 'react';
import { AppLoading, Font } from 'expo';
import { StyleSheet, Text, ImageBackground } from 'react-native';

import Banner from './src/components/Banner';
import Info from './src/containers/Info';

type Props = {};
type State = {
  loaded: boolean
};

export default class App extends Component<Props, State> {
  state = {
    loaded: false
  };

  componentWillMount() {
    this._loadAssetsAsync();
  }

  _loadAssetsAsync = async () => {
    await Font.loadAsync({
      'exo-light': require('./assets/fonts/Exo-Light.ttf'),
      'exo-medium': require('./assets/fonts/Exo-Medium.ttf'),
      'exo-regular': require('./assets/fonts/Exo-Regular.ttf'),
      'exo-semibold': require('./assets/fonts/Exo-SemiBold.ttf')
    });
    this.setState({ loaded: true });
  };

  render() {
    if (!this.state.loaded) {
      return <AppLoading />;
    }

    return (
      <ImageBackground
        source={require('./assets/images/3_bg.jpg')}
        style={styles.container}
      >
        <Banner />
        <Info />
      </ImageBackground>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 50,
    paddingBottom: 20,
    paddingHorizontal: 10
  }
});
