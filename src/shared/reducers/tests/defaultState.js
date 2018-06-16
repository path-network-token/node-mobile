const defaultState = {
  device: {
    errorMessage: '',
    info: {
      asn: '',
      id: '',
      ip: '',
      lat: '',
      lng: ''
    }
  },
  job: {
    errorMessage: '',
    jobPending: false,
    jobSuccess: {}
  },
  options: {
    errorMessage: '',
    userSettings: {
      walletAddress: '',
      wifiOnly: false
    }
  },
  socketClient: {
    socketInit: {},
    socketInitErrorMessage: '',
    socketReceiveJob: {},
    socketReceiveJobErrorMessage: '',
    socketSubmitJob: {},
    socketSubmitJobErrorMessage: '',
    socketUpdateLoc: {},
    socketUpdateLocErrorMessage: ''
  },
  stats: {
    jobCompleteCount: 0
  }
};

export default defaultState;
