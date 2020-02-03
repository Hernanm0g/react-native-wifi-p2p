import { DeviceEventEmitter, NativeModules } from 'react-native';
import { getError } from './reason-code';

const WiFiP2PManager = NativeModules.WiFiP2PManagerModule;

// ACTIONS
const PEERS_UPDATED_ACTION = 'PEERS_UPDATED';
const CONNECTION_INFO_UPDATED_ACTION = 'CONNECTION_INFO_UPDATED';
const GROUP_INFO_UPDATED_ACTION = 'GROUP_INFO_UPDATED';

// CONSTS
const MODULE_NAME = 'WIFI_P2P';

// Set Device name,
// Random numbers at the end of the string
const ran = Math.floor((Math.random() * 10000) + 1);
const DEFAULT_DEVICE_NAME = "[Vidapp] Anonymous_"+ran;
const initialize = (devname = DEFAULT_DEVICE_NAME) => WiFiP2PManager.init( );

// Set device name
const setDeviceName = (devname = DEFAULT_DEVICE_NAME) => WiFiP2PManager.setDeviceName( devname );

const startDiscoveringPeers = () => new Promise((resolve, reject) => {
    WiFiP2PManager.discoverPeers((reasonCode) => {
        reasonCode === undefined ? resolve() : reject(getError(reasonCode));
    })
});

const subscribeOnEvent = (event, callback) => {
    DeviceEventEmitter.addListener(`${MODULE_NAME}:${event}`, callback);
};

const unsubscribeFromEvent = (event, callback) => {
    DeviceEventEmitter.removeListener(`${MODULE_NAME}:${event}`, callback);
};

const subscribeOnPeersUpdates = (callback) => subscribeOnEvent(PEERS_UPDATED_ACTION, callback);

const unsubscribeFromPeersUpdates = (callback) => unsubscribeFromEvent(PEERS_UPDATED_ACTION, callback);

const subscribeOnConnectionInfoUpdates = (callback) => subscribeOnEvent(CONNECTION_INFO_UPDATED_ACTION, callback);

const unsubscribeFromConnectionInfoUpdates = (callback) => unsubscribeFromEvent(CONNECTION_INFO_UPDATED_ACTION, callback);

const subscribeOnGroupInfoUpdates = (callback) => subscribeOnEvent(GROUP_INFO_UPDATED_ACTION, callback);

const unsubscribeFromGroupInfoUpdates = (callback) => unsubscribeFromEvent(GROUP_INFO_UPDATED_ACTION, callback);

const connect = (deviceAddress) => new Promise((resolve, reject) => {
    WiFiP2PManager.connect(deviceAddress, status => {
        status === undefined ? resolve() : reject(getError(status));
    })
});

const disconnect = () => new Promise((resolve, reject) => {
    WiFiP2PManager.disconnect(status => {
        status === undefined ? resolve() : reject(getError(status));
    })
});

const createGroup = () => new Promise((resolve, reject) => {
    WiFiP2PManager.createGroup(reasonCode => {
        reasonCode === undefined ? resolve() : reject(getError(reasonCode));
    })
});

const removeGroup = () => new Promise((resolve, reject) => {
    WiFiP2PManager.removeGroup(reasonCode => {
        reasonCode === undefined ? resolve() : reject(getError(reasonCode));
    })
});

const getAvailablePeers = () => WiFiP2PManager.getAvailablePeersList();

const isSuccessfulInitialize = () => WiFiP2PManager.isSuccessfulInitialize();

const stopDiscoveringPeers = () => new Promise((resolve, reject) => {
    WiFiP2PManager.stopPeerDiscovery(reasonCode => {
        reasonCode === undefined ? resolve() : reject(getError(reasonCode));
    })
});

const sendFile = (pathToFile, receiverIp = null) => new Promise((resolve, reject) => {
  console.log("Send to:",receiverIp);
  console.log("file Path:", pathToFile);
    WiFiP2PManager.sendFile(pathToFile, receiverIp, (reasonCode) => {
        reasonCode === undefined ? resolve() : reject(getError(reasonCode));
    });
});

const receiveFile = () => new Promise((resolve, reject) => {
  console.log("receiveFile");
    WiFiP2PManager.receiveFile((pathToFile) => {
        resolve(pathToFile);
    });
});

const sendMessage = (message, ip) => new Promise((resolve, reject) => {
  console.log("sendMessage", message);
    WiFiP2PManager.sendMessage(message, ip, (reasonCode) => {
        reasonCode === "soon will be" ? resolve(true) : reject(getError(reasonCode));
    });
});

const receiveMessage = () => new Promise((resolve, reject) => {
  console.log("receiveMessage");
    WiFiP2PManager.receiveMessage((message) => {
        resolve(message);
    });
});

const getConnectionInfo = () => WiFiP2PManager.getConnectionInfo();

const getGroupPassphraseInfo = () => WiFiP2PManager.getGroupPassphraseInfo();

const getGroupClients = () => WiFiP2PManager.getGroupClients();

//////////////////////////////////////////////////////////////////

const isWiFiEnabled = () => true;

const setWiFiState = (isEnabled) => {};

export {
    // public methods
    initialize,
    setDeviceName,
    isSuccessfulInitialize,
    startDiscoveringPeers,
    stopDiscoveringPeers,
    subscribeOnPeersUpdates,
    unsubscribeFromPeersUpdates,
    subscribeOnConnectionInfoUpdates,
    unsubscribeFromConnectionInfoUpdates,
    subscribeOnGroupInfoUpdates,
    unsubscribeFromGroupInfoUpdates,
    getAvailablePeers,
    connect,
    disconnect,
    createGroup,
    removeGroup,
    getConnectionInfo,
    getGroupPassphraseInfo,
    getGroupClients,

    // experimental
    sendFile,
    receiveFile,
    sendMessage,
    receiveMessage,

    // system methods
    subscribeOnEvent,
    unsubscribeFromEvent,

    // const
    PEERS_UPDATED_ACTION,
    CONNECTION_INFO_UPDATED_ACTION,
    GROUP_INFO_UPDATED_ACTION,

    // future realization
    // isWiFiEnabled,
    // setWiFiState,
    // sendFile,
    // receiveFile
};
