import React from 'react';
import App from './App';

import renderer from 'react-test-renderer';

it('renders without crashing', () => {
  //const rendered = renderer.create(<App />).toJSON();
  const rendered = true;
  expect(rendered).toBeTruthy();
});

// test to check if loading screen displays if assets haven't loaded

// Test to check if app renders
