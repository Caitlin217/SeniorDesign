package com.example.android.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;



public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private AppNetService netService;
    WifiP2pNetServiceListener serviceListener;

    private static final String TAG = "WiFiDirectBroadcastReceiver";

    public WiFiDirectBroadcastReceiver(AppNetService service, WifiP2pNetServiceListener listener) {
        super();
        this.netService = service;
        this.serviceListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                netService.setIsWifiP2pEnabled(true);

                netService.discoverPeers();
            } else {
                netService.setIsWifiP2pEnabled(false);
                netService.resetPeers();

            }
            Log.d(TAG, "P2P state changed - state:" + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {


            if (netService.isWifiP2pAviliable()) {
                netService.requestPeers(serviceListener);

            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (!netService.isWifiP2pAviliable()) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {


                netService.requestConnectionInfo(serviceListener);
            } else {

                netService.resetPeers();

                netService.discoverPeers();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            netService.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            Log.d(TAG, "P2P this device changed - wifiP2pDevice:" + wifiP2pDevice.toString());

        } else {

            Log.d(TAG, "Other P2P change action - " + action);
        }
    }
}