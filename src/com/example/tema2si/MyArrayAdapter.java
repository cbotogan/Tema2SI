package com.example.tema2si;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<listRow> {
	Context context;
	ArrayList<listRow> rows;

	public MyArrayAdapter(Context context, ArrayList<listRow> rows) {
		super(context, R.layout.list_row, rows);
		this.context = context;
		this.rows = rows;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_row, parent, false);
		
		TextView textView1 = (TextView) rowView.findViewById(R.id.first_packageName);
		textView1.setText(rows.get(position).package_name);
		
		TextView textView2 = (TextView) rowView.findViewById(R.id.second_appName);
		textView2.setText(rows.get(position).app_name);
		
		TextView textView3 = (TextView) rowView.findViewById(R.id.third_uid);
		textView3.setText(rows.get(position).app_uid);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		imageView.setImageDrawable(rows.get(position).icon);
		
		return rowView;
	}

}
