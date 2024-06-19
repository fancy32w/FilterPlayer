package cc.ralee.filterplayer;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.Socket;
import java.util.Collection;

//import android.net.wifi.p2p.WifiP2pWfdInfo;


/**
 * date：2018/2/24 on 11:10
 * description: 客户端监听连接服务端信息的变化，以回调的形式把信息传递给发送文件界面
 */

public class Wifip2pReceiver extends BroadcastReceiver {

    public static final String TAG = "Wifip2pReceiver";
    public static boolean connect=false;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private Wifip2pActionListener mListener;
    private WifiP2pDevice localDevice;
    private Socket mSocket;
    WifiP2pDevice device;

    //private WifiP2pManager.GroupInfoListener Listener;
    /***********Chris*************/
    public Wifip2pReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel,
                           Wifip2pActionListener listener) {
        mWifiP2pManager = wifiP2pManager;
        mChannel = channel;
        mListener = listener;
    }



    @SuppressLint("MissingPermission")
   // @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "接收到广播： " + intent.getAction());
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        switch (intent.getAction()) {
            //WiFi P2P是否可用
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:

                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    mListener.wifiP2pEnabled(true);

                } else {
                    mListener.wifiP2pEnabled(false);
                }
                break;

            // peers列表发生变化
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:

                mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mListener.onPeersInfo(peers.getDeviceList());

                    }
                });
                break;

            // WiFi P2P连接发生变化
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                 connect=true;
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                //boolean isConnected = (WifiP2pDevice.CONNECTED == device.status);
                if (networkInfo.isConnected()){
                    /***********查看组的基本信息**************/
/*                    mWifiP2pManager.requestGroupInfo(mChannel, wifiP2pGroup -> {
                        Log.d(TAG, "onGroupInfoAvailable detail:\n" + wifiP2pGroup.toString());
                        Collection<WifiP2pDevice> clientList = wifiP2pGroup.getClientList();
                        if (clientList != null) {
                            int size = clientList.size();
                            Log.d(TAG, "onGroupInfoAvailable - client count:" + size);
                            // Handle all p2p client devices
                        }
                    });
                    */

                    /******当前设备是否为GO，IP地址*******/
                    mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @SuppressLint("ServiceCast")
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            if (info.groupFormed && info.isGroupOwner) {
                                Log.d(TAG, "is GroupOwner: ");
                            } else if (info.groupFormed) {
                                Log.d(TAG, "is GroupClient: ");
                            }

                            String ownerIP = info.groupOwnerAddress.getHostAddress();
                            Log.e(TAG, "onConnectionInfoAvailable ownerIP = " + ownerIP);
                            mListener.onConnection(info);

                        }
                    });
                }else {
                    mListener.onDisconnection();
                }
                break;

            // WiFi P2P设备信息发生变化
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:

                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                mListener.onDeviceInfo(device);
                //BaseActivity.onDeviceInfo(device);

                break;
            default:
                break;
        }
    }


}
