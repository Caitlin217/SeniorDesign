package com.example.jeff.wifififififififfi;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

	Context context;
	LayoutInflater inflater;
	List<ScanResult> wifiList;

	public ListAdapter(Context context, List<ScanResult> wifiList) {
		this.context = context;
		this.wifiList = wifiList;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return wifiList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		System.out.println("viewpos" + position);
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.dataset, null);
			holder = new Holder();
			holder.tvDetails = (TextView) view.findViewById(R.id.tvDetails);

			view.setTag(holder);
		} else {
			holder = (Holder) view.getTag();
		}
		holder.tvDetails.setText("SSID :: " + wifiList.get(position).SSID
				+ "\nStrength :: " + wifiList.get(position).level
				+ "\nBSSID :: " + wifiList.get(position).BSSID
				+ "\nChannel :: "
				+ convertFrequencyToChannel(wifiList.get(position).frequency)
				+ "\nFrequency :: " + wifiList.get(position).frequency
				+ "\nCapability :: " + wifiList.get(position).capabilities);

		return view;
	}

	public static int convertFrequencyToChannel(int freq) {
		if (freq >= 2412 && freq <= 2484) {
			return (freq - 2412) / 5 + 1;
		} else if (freq >= 5170 && freq <= 5825) {
			return (freq - 5170) / 5 + 34;
		} else {
			return -1;
		}
	}

	class Holder {
		TextView tvDetails;

	}
}
