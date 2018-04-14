package com.example.jeff.wifififififififfi;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	private Button btnRefresh;
	ListAdapter adapter;
	ListView lvWifiDetails;
	List<ScanResult> wifiList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lvWifiDetails = (ListView) findViewById(R.id.lvWifiDetails);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		scanWifiList();

		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				scanWifiList();

			}
		});
	}

	private void setAdapter() {
		adapter = new ListAdapter(getApplicationContext(), wifiList);
		lvWifiDetails.setAdapter(adapter);
	}

	private void scanWifiList() {
		mainWifi.startScan();
		wifiList = mainWifi.getScanResults();

		setAdapter();

	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
		}
	}
}
