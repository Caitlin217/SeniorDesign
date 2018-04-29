package com.example.android.wifidirect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;
import com.example.android.wifidirect.AppNetService.NetServiceBinder;


public class WiFiDirectActivity extends Activity implements DeviceActionListener {
    public static final String TAG = "wifi direct";

    private static boolean isSendingFile = false;
    AppNetService appNetService;
    public AppNetService getNetService() {
        return appNetService;
    }


    private ServiceConnection serviceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d("ServiceConnection", "bind service success");
            NetServiceBinder binder = (NetServiceBinder) service;
            appNetService = binder.getService();
            appNetService.bindAcitivity(WiFiDirectActivity.this);
            appNetService.bindListener(new AppNetServiceListener(getDetailFrag(), getListFrag()));
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection", "unbind service success");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        android.net.wifi.WifiManager wifi_manager = (android.net.wifi.WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        String networkSSID = "Kyle";
        String networkPass = "Kpizzo94";
        android.net.wifi.WifiConfiguration conf = new android.net.wifi.WifiConfiguration();

        if (!wifi_manager.isWifiEnabled()){

            conf.status = android.net.wifi.WifiConfiguration.Status.ENABLED;
            conf.allowedKeyManagement.set(android.net.wifi.WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedProtocols.set(android.net.wifi.WifiConfiguration.Protocol.WPA);
            conf.allowedGroupCiphers.set(android.net.wifi.WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(android.net.wifi.WifiConfiguration.GroupCipher.CCMP);
            conf.allowedPairwiseCiphers.set(android.net.wifi.WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(android.net.wifi.WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedProtocols.set(android.net.wifi.WifiConfiguration.Protocol.RSN);
            conf.SSID = "\"" + networkSSID + "\"";
            conf.preSharedKey = "\""+ networkPass +"\"";
            wifi_manager.setWifiEnabled(true);

            //wifi_manager.startScan();
            wifi_manager.addNetwork(conf);
            wifi_manager.saveConfiguration();
            List<android.net.wifi.WifiConfiguration> list = wifi_manager.getConfiguredNetworks();
            for( android.net.wifi.WifiConfiguration i : list ) {

            wifi_manager.disconnect();

            wifi_manager.enableNetwork(i.networkId, true);

            wifi_manager.reconnect();

            break;

            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        bindService(new Intent(this, AppNetService.class), serviceConn,
                BIND_AUTO_CREATE);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ...");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ...");
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy   unbindService service.");
        if (serviceConn != null) {
            unbindService(serviceConn);
            serviceConn = null;
        }

        super.onDestroy();
    }

    DeviceDetailFragment fragDetail = null;
    DeviceListFragment fragList = null;

    private DeviceDetailFragment getDetailFrag() {
        if (fragDetail == null) {
            fragDetail = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_detail);
        }
        return fragDetail;
    }

    private DeviceListFragment getListFrag() {
        if (fragList == null) {
            fragList = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
        }
        return fragList;
    }

    public List<WifiP2pDevice> getPeersList() {
        return this.getNetService().getP2pDeviceList();
    }

    public void updateThisDevice(WifiP2pDevice device) {
        getListFrag().updateThisDevice(device);
    }


    private long recvFileSize = 0;
    private long sendFileSize = 0;
    private String recvFileName = "";
    private String sendFileName = "";
    private long recvBytes = 0;
    private long sendBytes = 0;
    private String selectHost = "";

    public void resetRecvBytes() {
        recvBytes = 0;
    }

    public void resetSendBytes() {
        sendBytes = 0;
    }

    public void resetRecvFileInfo() {
        resetRecvBytes();
        recvFileName = "";
        recvFileSize = 0;
    }

    public void resetSendFileInfo() {
        resetSendBytes();
        sendFileName = "";
        sendFileSize = 0;
    }

    public void setRecvFileSize(long size) {
        recvFileSize = size;
    }

    public void setSendFileSize(long size) {
        sendFileSize = size;
    }

    public String recvFileName() {
        return recvFileName;
    }

    public String sendFileName() {
        return sendFileName;
    }

    public void setRecvFileName(String name) {
        recvFileName = name;
    }

    public void setSendFileName(String name) {
        sendFileName = name;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User has picked an image. Transfer it to group owner i.e peer using
        if (resultCode != RESULT_OK) {
            return;
        }

        Log.d(this.getClass().getName(), "onActivityResult requestCode:"
                + requestCode + " resultCode:" + resultCode);
        if (requestCode == ConfigInfo.REQUEST_CODE_SELECT_IMAGE) {
            if (data == null) {
                Log.d(this.getClass().getName(),
                        "onActivityResult data == null, no choice.");
                return;
            }
            Uri uri = data.getData();
            getDetailFrag().showStatus("Sending: " + uri);
            sendFile(uri);
        }
    }


    public void startSelectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, ConfigInfo.REQUEST_CODE_SELECT_IMAGE);
    }


    public void startSelectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, ConfigInfo.REQUEST_CODE_SELECT_IMAGE);
    }


    public void startSelectAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, ConfigInfo.REQUEST_CODE_SELECT_IMAGE);
    }


    public void startSelectTakeImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // "android.media.action.IMAGE_CAPTURE";
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Environment.getExternalStorageDirectory() + "/wifi-direct/");
        startActivityForResult(intent,
                ConfigInfo.REQUEST_CODE_SELECT_TAKE_IMAGE);
    }


    public void startSelectTakeVedio() {
        int durationLimit = 60;
        int sizeLimit = 100 * 1024 * 1024;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);
        startActivityForResult(intent,
                ConfigInfo.REQUEST_CODE_SELECT_TAKE_VIDEO);
    }


    public void startSelectAudioAmr() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/amr"); // String AUDIO_AMR = "audio/amr";
        intent.setClassName("com.android.soundrecorder",
                "com.android.soundrecorder.SoundRecorder");
        startActivityForResult(intent, ConfigInfo.REQUEST_CODE_SELECT_AUDIO_ARM);
    }

    public void showSelectMediaDialog() {
        AlertDialog.Builder selectDialog = new AlertDialog.Builder(this);
        selectDialog.setTitle("选取文件");
        selectDialog.setIcon(android.R.drawable.ic_dialog_info);
        selectDialog.setSingleChoiceItems(new String[] { "图片", "视频", "音频",
                "拍照", "录像", "录音" }, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        startSelectImage();
                        break;
                    case 1:
                        startSelectVideo();
                        break;
                    case 3:
                        startSelectAudio();
                        break;
                    case 4:
                        startSelectTakeImage();
                        break;
                    case 5:
                        startSelectTakeVedio();
                        break;
                    case 6:
                        startSelectAudioAmr();
                        break;
                }
            }
        });
        selectDialog.setNegativeButton("", null);
        selectDialog.show();
    }

    public void showSelectPeerDialog() {
        selectHost = null;
        AlertDialog.Builder selectDialog = new AlertDialog.Builder(this);
        selectDialog.setTitle("SELECT PEER");
        selectDialog.setIcon(android.R.drawable.ic_dialog_info);

        final ArrayList<String> items = new ArrayList<String>();
        Iterator<PeerInfo> it = getNetService().getPeerInfoList().iterator();
        while (it.hasNext())
            items.add(it.next().host);
        items.add("null");
        String[] strHosts = new String[items.size()];
        items.toArray(strHosts);
        selectDialog.setSingleChoiceItems(strHosts, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < items.size()-1) {
                            selectHost = items.get(which);
                            Log.d(TAG, "showSelectPeerDialog selectHost:" + selectHost);
                            dialog.dismiss();
                            startSelectImage();
                        }
                    }
                });
        selectDialog.setNegativeButton("CANCEL", null);
        selectDialog.show();
    }

    private void verifyRecvFile() {
        AlertDialog.Builder normalDia = new AlertDialog.Builder(this);
        normalDia.setIcon(R.drawable.ic_launcher);
        normalDia.setTitle("Verify Receive File");
        normalDia.setMessage("NAME:" + recvFileName + "\nSIZE:"
                + ((double) recvFileSize) / 1024 + "KB\nFROM:"
                + getNetService().getRemoteSockAddress());

        normalDia.setPositiveButton("",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppNetService netService = getNetService();
                        netService.setbVerifyRecvFile(true);
                        netService.verifyRecvFile();
                    }
                });
        normalDia.setNegativeButton("",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppNetService netService = getNetService();
                        netService.setbVerifyRecvFile(false);
                        netService.verifyRecvFile();
                    }
                });
        normalDia.create().show();
    }

    public void sendFile(Uri uri) {

        if (!isSendingFile) {
            isSendingFile = true;
            resetSendFileInfo();
            String host = "";
            if (appNetService.isPeer()) {
                host = appNetService.hostAddress();
            } else {
                assert (!selectHost.isEmpty());
                host = selectHost;
            }
            int port = ConfigInfo.LISTEN_PORT;

            appNetService.handleSendFile(host, port, uri);
            Log.d(TAG, "send host:" + host + "port:"+ port + "uri:" + uri);
        }
        else
            showToastTips("Just allowe only a sending file concurrently.");
    }

    public void onSendFileEnd() {

        isSendingFile = false;
    }

    public void reportPeerInfo() {
        Log.d(TAG, "reportPeerInfo");
        if (appNetService.isGroupOwner())
            this.broadcastPeerList();
        else
            appNetService.handleSendPeerInfo();
    }

    public void showDiscoverPeers() {
        getListFrag().onInitiateDiscovery();
    }

    public void showToastTips(String tips) {
        Toast toast = Toast.makeText(this, tips, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    public boolean broadcastPeerList() {
        Log.d(TAG, "broadcastPeerList ...");
        appNetService.handleBroadcastPeerList();
        return true;
    }

    static private class ActivityHandler extends Handler {
        private static final String TAG = "ActivityHandler";
        private WiFiDirectActivity activity;

        ActivityHandler(WiFiDirectActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage()  msg.what:" + msg.what);
            switch (msg.what) {
                case ConfigInfo.MSG_RECV_PEER_INFO:
                    activity.showToastTips("receive peer's address.");
                    activity.getDetailFrag().showSendFileVeiw();
                    break;
                case ConfigInfo.MSG_SEND_RECV_FILE_BYTES:
                    activity.sendBytes = activity.sendBytes + msg.arg1;
                    activity.recvBytes = activity.recvBytes + msg.arg2;
                    int progress1 = 0;
                    int progress2 = 0;
                    if (activity.sendFileSize != 0)
                        progress1 = (int) (activity.sendBytes * 100 / (activity.sendFileSize));
                    if (activity.recvFileSize != 0)
                        progress2 = (int) (activity.recvBytes * 100 / (activity.recvFileSize));

                    String tips = "\n send:" + progress1 + "(%) data(kb):"
                            + activity.sendBytes / 1024 + "\n recv:" + progress2
                            + "(%) data(kb):" + activity.recvBytes / 1024;

                    activity.getDetailFrag().showStatus(tips);
                    break;

                case ConfigInfo.MSG_VERIFY_RECV_FILE_DIALOG:
                    activity.verifyRecvFile();
                    break;

                case ConfigInfo.MSG_REPORT_RECV_FILE_RESULT:
                    if (msg.arg1 == 0)
                        activity.showToastTips("receive file successed.");
                    else
                        activity.showToastTips("receive file failed.");
                    break;

                case ConfigInfo.MSG_REPORT_SEND_FILE_RESULT:
                    if (msg.arg1 == 0)
                        activity.showToastTips("send file successed.");
                    else
                        activity.showToastTips("send file failed.");
                    activity.onSendFileEnd();
                    break;
                case ConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT:
                    if (msg.arg1 == 0)
                        activity.showToastTips("send peer's info successed.");
                    else
                        activity.showToastTips("send peer's info failed.");
                    break;
                case ConfigInfo.MSG_SEND_STRING:
                    if (msg.arg1 == -1)
                        activity.showToastTips("send string failed.");
                    else
                        activity.showToastTips("send string successed, length " + msg.arg1 + ".");
                    break;
                case ConfigInfo.MSG_REPORT_RECV_PEER_LIST:
                    activity.showToastTips("receive peer list.");
                case ConfigInfo.MSG_REPORT_SEND_STREAM_RESULT:
                    if (msg.arg1 == 0)
                        activity.showToastTips("send stream successed.");
                    else
                        activity.showToastTips("send stream failed.");
                    break;
                // ...
                default:
                    activity.showToastTips("error msg id.");
            }
            super.handleMessage(msg);
        }
    }

    private Handler handler = new ActivityHandler(this);

    public Handler getHandler() {
        return handler;
    }


    public void resetPeers() {
        getListFrag().clearPeers();
        getDetailFrag().resetViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:

                if (appNetService.isWifiP2pManager()
                        && this.appNetService.isWifiP2pChannel()) {



                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                return appNetService.discoverPeers();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        getDetailFrag().showDetails(device);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        appNetService.connect(config);
    }

    @Override
    public void disconnect() {
        getDetailFrag().resetViews();
        resetPeers();
        appNetService.removeGroup();

        appNetService.discoverPeers();
    }

    public void onDisconnect() {
        getDetailFrag().getView().setVisibility(View.GONE);
    }

    @Override
    public void cancelDisconnect() {

        Log.e(TAG, "cancelDisconnect.");
        if (appNetService.isWifiP2pManager()) {
            final DeviceListFragment fragment = getListFrag();
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                appNetService.cancelDisconnect();
            }
        }

    }
}

