package com.example.jeff.seniordesign25;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.ImageButton;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import java.util.List;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView=(TextView)findViewById(R.id.textView1);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        ((ImageButton)findViewById(R.id.imageButton_power)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("p2p", "discoverPeers() called");

                mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        textView.setText("You clicked the button bitch");
                        Log.d("p2p", "discoverPeers() Success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("p2p", "discoverPeers() Failure: " + reason);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public static class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private Channel mChannel;
        private MainActivity mActivity;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d("p2p", "Action: " + action);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.d("p2p", "Requesting for peers");

                if (mManager != null) {
                    mManager.requestPeers(mChannel, peerListListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
        private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
        private PeerListListener peerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                List<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                if(!refreshedPeers.equals(peers)){
                    peers.clear();
                    peers.addAll(refreshedPeers);
                }
            }
        };
    }
}

