// @flow
import * as IpSubnetCalculator from 'ip-subnet-calculator';
import type { Store } from 'redux';

const setDeviceNetworkInfo = async dispatch => {
  // TODO get IP from multiple sources?
  const ipResponse = await fetch('https://api.ipify.org');
  const deviceIP = await ipResponse.text();
  console.log('DEVICE IP:', deviceIP);

  const asnFetchURL = 'https://api.iptoasn.com/v1/as/ip/' + deviceIP;
  const asnResponse = await fetch(asnFetchURL);
  const asnData = await asnResponse.json();
  console.log('ASN DATA:', asnData);

  const subnet = IpSubnetCalculator.calculate(
    asnData.first_ip,
    asnData.last_ip
  );
  console.log('SUBNET:', subnet);

  // TODO determine cidr from subnet info above

  // TODO dispatch action to set values in store
};

const setDeviceLocationInfo = async dispatch => {
  // navigator.geolocation;
};

const setInfo: (store: Store) => Promise<void> = async store => {
  const { dispatch } = store;

  setDeviceNetworkInfo(dispatch);
  setDeviceLocationInfo(dispatch);
};

const deviceHandle = {
  setInfo
};

export default deviceHandle;
