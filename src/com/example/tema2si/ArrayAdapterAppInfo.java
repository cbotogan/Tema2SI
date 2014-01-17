package com.example.tema2si;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArrayAdapterAppInfo extends ArrayAdapter<PowerEvent> {
	Context context;
	ArrayList<PowerEvent> rows;

	public ArrayAdapterAppInfo(Context context, ArrayList<PowerEvent> rows) {
		super(context, R.layout.app_info_row, rows);
		this.context = context;
		this.rows = rows;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.app_info_row, parent, false);
		
		DecimalFormat df = new DecimalFormat("##.##");
		
		TextView textView = (TextView) rowView.findViewById(R.id.cputime);
		textView.setText(Long.toString(rows.get(position).getCpuTime()));
		
		textView = (TextView) rowView.findViewById(R.id.energycpu);		
		textView.setText(df.format(rows.get(position).getPowerCPU()));
		
		textView = (TextView) rowView.findViewById(R.id.gpstime);
		textView.setText(Long.toString(rows.get(position).getGpsTime()));
		
		textView = (TextView) rowView.findViewById(R.id.energygps);
		textView.setText(df.format(rows.get(position).getPowerGPS()));

		
		textView = (TextView) rowView.findViewById(R.id.appName);
		textView.setText(rows.get(position).getAppName());
		
		textView = (TextView) rowView.findViewById(R.id.uid);
		textView.setText(rows.get(position).getUid());
		
		
		textView = (TextView) rowView.findViewById(R.id.packageName);
		textView.setText(rows.get(position).getDefaultPackageName());
		
		
		textView = (TextView) rowView.findViewById(R.id.bytessent);
		textView.setText(Long.toString(rows.get(position).getBytesSent()));
		
		textView = (TextView) rowView.findViewById(R.id.bytesreceived);
		textView.setText(Long.toString(rows.get(position).getBytesReceived()));
		
		textView = (TextView) rowView.findViewById(R.id.energywifi);
		textView.setText(df.format(rows.get(position).getPowerTCP()));
		
		return rowView;
	}

}
