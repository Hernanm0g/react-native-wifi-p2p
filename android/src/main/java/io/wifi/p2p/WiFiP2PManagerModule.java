package io.wifi.p2p;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

import java.lang.reflect.Method;
import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import static android.os.Looper.getMainLooper;

/**
 * Created by zyusk on 01.05.2018.
 * Forked and Updated by Hernnanm0g
 */
public class WiFiP2PManagerModule extends ReactContextBaseJavaModule implements WifiP2pManager.ConnectionInfoListener {
    private WifiP2pInfo wifiP2pInfo;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ReactApplicationContext reactContext;
    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiP2PDeviceMapper mapper = new WiFiP2PDeviceMapper();

    public WiFiP2PManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }
    /** Logging tag, attached to all Android log messages. */
    private static final String TAG = "WifiDirectSpeaker";
    @Override
    public String getName() {
        return "WiFiP2PManagerModule";
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.wifiP2pInfo = info;
    }

    @ReactMethod
    public void getConnectionInfo(final Promise promise) {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInformation) {
                System.out.println(wifiP2pInformation);

                wifiP2pInfo = wifiP2pInformation;

                promise.resolve(mapper.mapWiFiP2PInfoToReactEntity(wifiP2pInformation));
            }
        });
    }

    @ReactMethod
    public void getGroupPassphraseInfo(final Promise promise) {
        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    String groupPassword = group.getPassphrase();
                    promise.resolve(groupPassword);
                }
                else {
                    promise.resolve(null);
                }
            }
        });
    }

    @ReactMethod
    public void getGroupClients(final Promise promise) {
        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                  Collection<WifiP2pDevice> collection = group.getClientList();
                  Iterator<WifiP2pDevice> iterator = collection.iterator();
                  WritableArray array = Arguments.createArray();
                  while (iterator.hasNext()) {
                    WritableMap parm = mapper.mapDeviceInfoToReactEntity(iterator.next());
                    array.pushMap(parm);
                  }
                  promise.resolve(array);
                }
                else {
                    promise.resolve(null);
                }
            }
        });
    }

    @ReactMethod
    public void setDeviceName(String devname) {
        try {
          Activity activity = getCurrentActivity();
          if (activity != null) {
              manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
              channel = manager.initialize(activity, getMainLooper(), null);

              Class[] paramTypes = new Class[3];
              paramTypes[0] = WifiP2pManager.Channel.class;
              paramTypes[1] = String.class;
              paramTypes[2] = WifiP2pManager.ActionListener.class;
              Method setDeviceName = manager.getClass().getMethod(
                      "setDeviceName", paramTypes);
              setDeviceName.setAccessible(true);

              Object arglist[] = new Object[3];
              arglist[0] = channel;
              arglist[1] = devname;
              arglist[2] = new WifiP2pManager.ActionListener() {

                  @Override
                  public void onSuccess() {
                      Log.d("setDeviceName succeeded", "true");
                  }

                  @Override
                  public void onFailure(int reason) {
                      Log.d("setDeviceName failed", "true");
                  }
              };
              setDeviceName.invoke(manager, arglist);
          }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void init() {
        if (manager != null) { // prevent reinitialization
            return;
        }

        try {

          intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
          intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
          intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
          intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

          Activity activity = getCurrentActivity();
          if (activity != null) {
              manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
              channel = manager.initialize(activity, getMainLooper(), null);

              WiFiP2PBroadcastReceiver receiver = new WiFiP2PBroadcastReceiver(manager, channel, reactContext);
              activity.registerReceiver(receiver, intentFilter);
          }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void isSuccessfulInitialize(Promise promise) {
        Boolean isSuccessfulInitialize = manager != null && channel != null;
        promise.resolve(isSuccessfulInitialize);
    }

    @ReactMethod
    public void createGroup(final Callback callback) {
        manager.createGroup(channel,  new WifiP2pManager.ActionListener()  {
            public void onSuccess() {
                callback.invoke();
                //Group creation successful
            }

            public void onFailure(int reason) {
                callback.invoke(Integer.valueOf(reason));
                //Group creation failed
            }
        });
    }

    @ReactMethod
    public void removeGroup(final Callback callback) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                callback.invoke();
            }

            @Override
            public void onFailure(int reason) {
                callback.invoke(Integer.valueOf(reason));
            }
        });
    }

    @ReactMethod
    public void getAvailablePeersList(final Promise promise) {
        manager.requestPeers(channel, new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList deviceList) {
                WritableMap params = mapper.mapDevicesInfoToReactEntity(deviceList);
                promise.resolve(params);
            }
        });
    }

    @ReactMethod
    public void discoverPeers(final Callback callback) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                callback.invoke();
            }

            @Override
            public void onFailure(int reasonCode) {
                callback.invoke(Integer.valueOf(reasonCode));
            }
        });
    }

    @ReactMethod
    public void stopPeerDiscovery(final Callback callback) {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                callback.invoke();
            }

            @Override
            public void onFailure(int reasonCode) {
                callback.invoke(Integer.valueOf(reasonCode));
            }
        });
    }

    @ReactMethod
    public void disconnect(final Callback callback) {
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                callback.invoke();
            }

            @Override
            public void onFailure(int reasonCode) {
                callback.invoke(Integer.valueOf(reasonCode));
            }
        });
    }

    @ReactMethod
    public void connect(String deviceAddress, final Callback callback) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                callback.invoke();
                // WiFiP2PBroadcastReceiver notifies us. Ignore for now.
            }

            @Override
            public void onFailure(int reasonCode) {
                callback.invoke(Integer.valueOf(reasonCode));
            }
        });
    }

    @ReactMethod
    public void sendFile(final String filePath, final String receiverIp, final Callback callback) {
        // FileTransferService.
        // Toast toast =
        //     Toast.makeText(reactContext,
        //             filePath+receiverIp, Toast.LENGTH_LONG);
        //
        // toast.show();
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupFormed && info.isGroupOwner) {
                  // Send file to clients
                } else if (info.groupFormed) {

                }
                Uri uri = Uri.fromFile(new File(filePath));
                String finalReceiver = "";
                if (receiverIp == null) {
                  finalReceiver = info.groupOwnerAddress.getHostAddress();
                } else {
                  finalReceiver = receiverIp;
                }
                System.out.println("Sending: " + uri);
                System.out.println("Intent----------- " + uri);
                Intent serviceIntent = new Intent(getCurrentActivity(), FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        finalReceiver);
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                getCurrentActivity().startService(serviceIntent);
                callback.invoke("soon will be");
            }
        });
    }

    @ReactMethod
    public void receiveFile(final Callback callback) {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupFormed && info.isGroupOwner) {
                    new FileServerAsyncTask(getCurrentActivity(), callback)
                            .execute();
                } else if (info.groupFormed) {
                  // Toast toast =
                  //     Toast.makeText(reactContext,
                  //             "Ready for receving", Toast.LENGTH_LONG);
                  //
                  // toast.show();
                  new FileServerAsyncTask(getCurrentActivity(), callback)
                          .execute();
                }
            }
        });
    }

    @ReactMethod
    public void sendMessage(final String message, final String ip, final Callback callback) {
      manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
          @Override
          public void onConnectionInfoAvailable(WifiP2pInfo info) {
              if (info.groupOwnerAddress==null){
                  callback.invoke("Group owner not accessible yet.");
                  return;
              }
              System.out.println("Sending message: " + message);
              Intent serviceIntent = new Intent(getCurrentActivity(), MessageTransferService.class);
              serviceIntent.setAction(MessageTransferService.ACTION_SEND_MESSAGE);
              serviceIntent.putExtra(MessageTransferService.EXTRAS_DATA, message);
              if (ip==null) {
                serviceIntent.putExtra(MessageTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
              } else {
                serviceIntent.putExtra(MessageTransferService.EXTRAS_GROUP_OWNER_ADDRESS, ip);
              }

              serviceIntent.putExtra(MessageTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
              getCurrentActivity().startService(serviceIntent);

              callback.invoke("soon will be");
          }
      });
    }

    @ReactMethod
    public void receiveMessage(final Callback callback) {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupFormed && info.isGroupOwner) {
                    new MessageServerAsyncTask(callback)
                            .execute();
                } else if (info.groupFormed) {
                  new MessageServerAsyncTask(callback)
                          .execute();
                }
                // hide the connect button
            }
        });
    }
}
