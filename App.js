// @flow
import React, { Component } from 'react';
import { Provider } from 'react-redux';
import { StyleSheet, Text, ImageBackground, StatusBar } from 'react-native';
import { AppLoading, Font } from 'expo';
import io from 'socket.io-client';

import Banner from './src/components/Banner';
import Info from './src/containers/Info';

import jobRunner from './src/jobRunner';
import socketClient from './src/socketClient';
import deviceHandle from './src/device';
import reduxStore from './src/shared/store/reduxStore';

let store = {};
let socketHandle = {};

type Props = {};

type State = {
  assetsLoaded: boolean,
  storeLoaded: boolean
};

export default class App extends Component<Props, State> {
  state = {
    assetsLoaded: false,
    storeLoaded: false
  };

  componentWillMount() {
    this.loadAssets();
    this.initModules();
  }

  loadAssets = async () => {
    await Font.loadAsync({
      'exo-light': require('./assets/fonts/Exo-Light.ttf'),
      'exo-medium': require('./assets/fonts/Exo-Medium.ttf'),
      'exo-regular': require('./assets/fonts/Exo-Regular.ttf'),
      'exo-semibold': require('./assets/fonts/Exo-SemiBold.ttf')
    });
    this.setState({ assetsLoaded: true });
  };

  initModules = async () => {
    store = await reduxStore();
    this.setState({ storeLoaded: true });

    // get the device info
    await deviceHandle.setInfo(store);

    // setup socket client
    socketClient(store);

    //jobHandle = jobRunner(store);
  };

  render() {
    if (this.state.assetsLoaded && this.state.storeLoaded) {
      return (
        <Provider store={store}>
          <ImageBackground
            source={require('./assets/images/3_bg.jpg')}
            style={styles.container}
          >
            <StatusBar barStyle="light-content" />
            <Banner />
            <Info />
          </ImageBackground>
        </Provider>
      );
    } else {
      return <AppLoading />;
    }
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 70,
    paddingBottom: 20,
    paddingHorizontal: 10
  }
});
