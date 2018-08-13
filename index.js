// @flow
import { AppRegistry } from 'react-native';
import App from './App';
import Background from './Background';

AppRegistry.registerComponent('mobileminer', () => App);
AppRegistry.registerHeadlessTask('PathBackgroundService', () => Background);
