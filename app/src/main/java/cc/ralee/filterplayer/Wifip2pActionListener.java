package cc.ralee.filterplayer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.Collection;

/**
 * date：2018/2/24 on 16:41
 * description: 监听广播回调信息
 */

    public interface Wifip2pActionListener {

        void wifiP2pEnabled(boolean enabled);

        void onConnection(WifiP2pInfo wifiP2pInfo);

        void onDisconnection();

        void onDeviceInfo(WifiP2pDevice wifiP2pDevice);

        void onPeersInfo(Collection<WifiP2pDevice> wifiP2pDeviceList);
    }