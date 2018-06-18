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
    socketConnected: false,
    socketInit: {
      id: ''
    },
    socketInitErrorMessage: '',
    socketReceiveJob: {},
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
