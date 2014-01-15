package com.example.tema2si;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

public class listRow {
	
	String package_name, app_uid, app_name;
	Drawable icon;
	public listRow(Drawable icon, String package_name, String app_uid,
			String app_name) {
		super();
		this.icon = icon;
		this.package_name = package_name;
		this.app_uid = app_uid;
		this.app_name = app_name;
	}

	public listRow() {
		// TODO Auto-generated constructor stub
	}
	
	

}
