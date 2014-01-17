/**************************************************************************
 * Copyright 2010 Chris Thompson                                           *
 *                                                                         *
 * Licensed under the Apache License, Version 2.0 (the "License");         *
 * you may not use this file except in compliance with the License.        *
 * You may obtain a copy of the License at                                 *
 *                                                                         *
 * http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                         *
 * Unless required by applicable law or agreed to in writing, software     *
 * distributed under the License is distributed on an "AS IS" BASIS,       *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*
 * See the License for the specific language governing permissions and     *
 * limitations under the License.                                          *
 **************************************************************************/
package com.example.tema2si;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

public class PowerUsage {

	private static final String TAG = "PowerInterfaceAdapter";
	private static final String BATTERY_STATS_IMPL_CLASS = "com.android.internal.os.BatteryStatsImpl";
	private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
	private static final String M_BATTERY_INFO_CLASS = "com.android.internal.app.IBatteryStats";
	private static final String BATTERY_STATS_CLASS = "android.os.BatteryStats";
	private static final String UID_CLASS = BATTERY_STATS_CLASS + "$Uid";
	private static final String PROC_CLASS = UID_CLASS + "$Proc";
	private static final String SENSOR_CLASS = UID_CLASS + "$Sensor";
	private static final String BATTER_STATS_TIMER_CLASS = BATTERY_STATS_CLASS
			+ "$Timer";
	private static final String TRAFFIC_CLASS = "android.net.TrafficStats";
	private static long WIFI_BPS = 1000000;
	private static long MOBILE_BPS = 200000;
	

	private Object mBatteryInfo_;
	private Object mStats_;
	private Object mPowerProfile_;
	private int mStatsType_ = BatteryStatsConstants.STATS_UNPLUGGED;
	private Activity parent_;
	public Context context;
	
	public ArrayList<PowerEvent> appsOneCycleUsageArray;	
	public ArrayList<PowerEvent> highestDrainers;
	public PowerEvent highestDrainCycle;
	
	int count = 0;
	Timer timer;

	public PowerUsage() {

	}

	public PowerUsage(Context context) {
		this.context = context;
		highestDrainers = new ArrayList<PowerEvent>();
	}

	// count = no of minutes
	public void start() {

		timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {				
				if (count != 15) {
					count++;
					try {
						processApplicationUsage("label");
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
					timer.cancel();
			}
		};

		// adding timer fire for every minute
		timer.schedule(task, 0, 60 * 1000);

	}

	private void load() {
		try {

			byte[] data = (byte[]) Class.forName(M_BATTERY_INFO_CLASS)
					.getMethod("getStatistics", null)
					.invoke(mBatteryInfo_, null);
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(data, 0, data.length);
			parcel.setDataPosition(0);

			/*
			 * Non-reflection call looks like mStats =
			 * com.android.internal.os.BatteryStatsImpl
			 * .CREATOR.createFromParcel(parcel);
			 */
			mStats_ = Class
					.forName(BATTERY_STATS_IMPL_CLASS)
					.getField("CREATOR")
					.getType()
					.getMethod("createFromParcel", Parcel.class)
					.invoke(Class.forName(BATTERY_STATS_IMPL_CLASS)
							.getField("CREATOR").get(null), parcel);

		} catch (InvocationTargetException e) {
			Log.e("BatteryTester", "Exception: " + e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}


	
	public void processApplicationUsage(String label) throws NameNotFoundException {

		parent_ = (Activity) context;
		try {

			mBatteryInfo_ = Class
					.forName("com.android.internal.app.IBatteryStats$Stub")
					.getDeclaredMethod("asInterface", IBinder.class)
					.invoke(null,
							Class.forName("android.os.ServiceManager")
									.getMethod("getService", String.class)
									.invoke(null, "batteryinfo"));
			mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
					.getConstructor(Context.class).newInstance(parent_);
			load();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		try {
			
			appsOneCycleUsageArray = new ArrayList<PowerEvent>();
			double powerCPU = 0;
			double powerGPS = 0;
			double powerTCP = 0;

			SensorManager sensorManager = (SensorManager) parent_
					.getSystemService(Context.SENSOR_SERVICE);
			Method getAvgPowerMeth = Class.forName(POWER_PROFILE_CLASS)
					.getMethod("getAveragePower", String.class);
			final double powerCpuNormal = .2;// (Double)
												// getAvgPowerMeth.invoke(mPowerProfile_,
												// PowerProfileConstants.POWER_CPU_NORMAL);
			final double averageCostPerByte = getAverageDataCost();
			final int which = mStatsType_;
			long uSecTime = (Long) Class
					.forName(BATTERY_STATS_IMPL_CLASS)
					.getMethod("computeBatteryRealtime", java.lang.Long.TYPE,
							java.lang.Integer.TYPE)
					.invoke(mStats_, SystemClock.elapsedRealtime() * 1000,
							which);
			updateStatsPeriod(uSecTime);
			SparseArray uidStats = (SparseArray) Class
					.forName(BATTERY_STATS_IMPL_CLASS)
					.getMethod("getUidStats", null).invoke(mStats_, null);
			final int NU = uidStats.size();
			Log.d("Apps", "Apps = " + NU);
			for (int iu = 0; iu < NU; iu++) {
				Object u = uidStats.valueAt(iu);
				
				powerCPU = 0;
				powerGPS = 0;
				powerTCP = 0;

				String packageWithHighestDrain = "";
				Map<String, Object> processStats = (Map) Class
						.forName(UID_CLASS)
						.getMethod("getProcessStats", (Class[]) null)
						.invoke(u, null);
				
				long cpuTime = 0;
				long cpuFgTime = 0;
				long gpsTime = 0;
				
				// Process cpu usage
				if (processStats.size() > 0) {
					for (Map.Entry<String, Object> ent : processStats
							.entrySet()) {
						packageWithHighestDrain = ent.getKey();

						if (packageWithHighestDrain == null)
							continue;

						Object ps = ent.getValue();

						final long userTime = (Long) Class
								.forName(PROC_CLASS)
								.getMethod("getUserTime",
										java.lang.Integer.TYPE)
								.invoke(ps, which);
						final long systemTime = (Long) Class
								.forName(PROC_CLASS)
								.getMethod("getSystemTime",
										java.lang.Integer.TYPE)
								.invoke(ps, which);
						final long foregroundTime = (Long) Class
								.forName(PROC_CLASS)
								.getMethod("getForegroundTime",
										java.lang.Integer.TYPE)
								.invoke(ps, which);
						cpuFgTime += foregroundTime * 10; // convert to millis
						final long tmpCpuTime = (userTime + systemTime) * 10; // convert
																				// to
																				// millis;
						final double processPower = tmpCpuTime * powerCpuNormal;

						cpuTime += tmpCpuTime;
						powerCPU += processPower;
						powerCPU /= 1000;

					}
				}
				
				// get list of all apps installed on system
				int uid = 0;
				String appName = null;
				List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
				for(PackageInfo p : packs){
					if (p.packageName.equals(packageWithHighestDrain)){
						uid = p.applicationInfo.uid;
						appName = (String) context.getPackageManager().getApplicationLabel(p.applicationInfo);
						break;
					}
				}
				
				// get mobile + wifi traffic for this app
				long totalBytesReceived = (Long)Class.forName(TRAFFIC_CLASS).getMethod("getUidRxBytes", java.lang.Integer.TYPE).invoke(u, uid);
				long totalBytesSent = (Long)Class.forName(TRAFFIC_CLASS).getMethod("getUidTxBytes", java.lang.Integer.TYPE).invoke(u, uid);
				powerTCP = (totalBytesReceived + totalBytesSent) * getAverageDataCost(); 
				
				
				// Process sensor usage
				Map<Integer, Object> sensorStats = (Map) Class
						.forName(UID_CLASS)
						.getMethod("getSensorStats", (Class[]) null).invoke(u);

				for (Map.Entry<Integer, Object> sensorEntry : sensorStats
						.entrySet()) {
					Object sensor = sensorEntry.getValue();
					int sensorType = (Integer) Class.forName(SENSOR_CLASS)
							.getMethod("getHandle", (Class[]) null)
							.invoke(sensor);
					Object timer = Class.forName(SENSOR_CLASS)
							.getMethod("getSensorTime", (Class[]) null)
							.invoke(sensor);
					long sensorTime = (Long) Class
							.forName(BATTER_STATS_TIMER_CLASS)
							.getMethod("getTotalTimeLocked",
									java.lang.Long.TYPE, java.lang.Integer.TYPE)
							.invoke(timer, uSecTime, which);
					double multiplier = 0;

					switch (sensorType) {

					// GPS
					case -10000:
						multiplier = (Double) Class
								.forName(POWER_PROFILE_CLASS)
								.getMethod("getAveragePower", String.class)
								.invoke(mPowerProfile_,
										PowerProfileConstants.POWER_GPS_ON);
						gpsTime = sensorTime / 1000;
						// Log.d(TAG, "GPS sensor time: " + sensorTime);
						powerGPS = (multiplier * gpsTime) /1000;
						break;
					default:
						android.hardware.Sensor sensorData = sensorManager
								.getDefaultSensor(1);// sensorType);
						if (sensorData != null) {
							multiplier = sensorData.getPower();
							// Log.d(TAG, "Other sensor time: " + sensorTime);

						}
					}
				}
				Log.d(TAG, "Power for package " + packageWithHighestDrain
						+ " equal to " + powerCPU);
				
				
				if (powerCPU != 0 && packageWithHighestDrain != null) {
					
					PowerEvent app = new PowerEvent();
					app.setCpuTime(cpuTime);
					app.setGpsTime(gpsTime);
					app.setCpuFgTime(cpuFgTime);					
					app.setPowerGPS(powerGPS);
					app.setPowerCPU(powerCPU);
					app.setDefaultPackageName(packageWithHighestDrain);	
					app.setUid(Integer.toString(uid));
					app.setAppName(appName);
					app.setUid(Integer.toString(uid));
					app.setBytesReceived(totalBytesReceived);
					app.setBytesSent(totalBytesSent);
					app.setPowerTCP(powerTCP);

					appsOneCycleUsageArray.add(app);		

		
				}

			}
			
		// get the most cpu-intensive app
		int pos = -1;
		double maxDrain = -1;
		
		for (PowerEvent buffEvent : appsOneCycleUsageArray)
			if (buffEvent.getPowerCPU() > maxDrain){
				maxDrain = buffEvent.getPowerCPU();
				pos = appsOneCycleUsageArray.indexOf(buffEvent);
			}			
		
		if (pos > -1){
			highestDrainCycle = appsOneCycleUsageArray.get(pos);
			
			//add app to list of highest drainers
			highestDrainers.add(highestDrainCycle);
		}
		
		Log.d("check", "dadadadaa");
		Log.d("check", highestDrainCycle.getDefaultPackageName());
		

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	public ArrayList<PowerEvent> getHighestDrainers(){		
		return highestDrainers;
	}
	
	// get average cost per byte between mobile and wifi
	private double getAverageDataCost() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {	
	
		double WIFI_POWER = (Double)Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", String.class)
		.invoke(mPowerProfile_, PowerProfileConstants.POWER_WIFI_ACTIVE) / 3600;
		double MOBILE_POWER = (Double)Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", String.class)
				.invoke(mPowerProfile_, PowerProfileConstants.POWER_RADIO_ACTIVE) / 3600;
		double mobileCostPerByte = MOBILE_POWER / (MOBILE_BPS / 8);
		double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);		
			
		return mobileCostPerByte + wifiCostPerByte;
		
	}

	private void updateStatsPeriod(long uSecTime) {

	}

	private class PowerProfileConstants {

		/**
		 * No power consumption, or accounted for elsewhere.
		 */
		public static final String POWER_NONE = "none";

		/**
		 * Power consumption when CPU is in power collapse mode.
		 */
		public static final String POWER_CPU_IDLE = "cpu.idle";

		/**
		 * Power consumption when CPU is running at normal speed.
		 */
		public static final String POWER_CPU_NORMAL = "cpu.normal";

		/**
		 * Power consumption when CPU is running at full speed.
		 */
		public static final String POWER_CPU_FULL = "cpu.full";

		/**
		 * Power consumption when WiFi driver is scanning for networks.
		 */
		public static final String POWER_WIFI_SCAN = "wifi.scan";

		/**
		 * Power consumption when WiFi driver is on.
		 */
		public static final String POWER_WIFI_ON = "wifi.on";

		/**
		 * Power consumption when WiFi driver is transmitting/receiving.
		 */
		public static final String POWER_WIFI_ACTIVE = "wifi.active";

		/**
		 * Power consumption when GPS is on.
		 */
		public static final String POWER_GPS_ON = "gps.on";

		/**
		 * Power consumption when Bluetooth driver is on.
		 */
		public static final String POWER_BLUETOOTH_ON = "bluetooth.on";

		/**
		 * Power consumption when Bluetooth driver is transmitting/receiving.
		 */
		public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";

		/**
		 * Power consumption when Bluetooth driver gets an AT command.
		 */
		public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";

		/**
		 * Power consumption when screen is on, not including the backlight
		 * power.
		 */
		public static final String POWER_SCREEN_ON = "screen.on";

		/**
		 * Power consumption when cell radio is on but not on a call.
		 */
		public static final String POWER_RADIO_ON = "radio.on";

		/**
		 * Power consumption when talking on the phone.
		 */
		public static final String POWER_RADIO_ACTIVE = "radio.active";

		/**
		 * Power consumption at full backlight brightness. If the backlight is
		 * at 50% brightness, then this should be multiplied by 0.5
		 */
		public static final String POWER_SCREEN_FULL = "screen.full";

		/**
		 * Power consumed by the audio hardware when playing back audio content.
		 * This is in addition to the CPU power, probably due to a DSP and / or
		 * amplifier.
		 */
		public static final String POWER_AUDIO = "dsp.audio";

		/**
		 * Power consumed by any media hardware when playing back video content.
		 * This is in addition to the CPU power, probably due to a DSP.
		 */
		public static final String POWER_VIDEO = "dsp.video";

	}

	private class BatteryStatsConstants {
		private static final boolean LOCAL_LOGV = false;

		/**
		 * A constant indicating a partial wake lock timer.
		 */
		public static final int WAKE_TYPE_PARTIAL = 0;

		/**
		 * A constant indicating a full wake lock timer.
		 */
		public static final int WAKE_TYPE_FULL = 1;

		/**
		 * A constant indicating a window wake lock timer.
		 */
		public static final int WAKE_TYPE_WINDOW = 2;

		/**
		 * A constant indicating a sensor timer.
		 * 
		 * {@hide}
		 */
		public static final int SENSOR = 3;

		/**
		 * A constant indicating a a wifi turn on timer
		 * 
		 * {@hide}
		 */
		public static final int WIFI_TURNED_ON = 4;

		/**
		 * A constant indicating a full wifi lock timer
		 * 
		 * {@hide}
		 */
		public static final int FULL_WIFI_LOCK = 5;

		/**
		 * A constant indicating a scan wifi lock timer
		 * 
		 * {@hide}
		 */
		public static final int SCAN_WIFI_LOCK = 6;

		/**
		 * A constant indicating a wifi multicast timer
		 * 
		 * {@hide}
		 */
		public static final int WIFI_MULTICAST_ENABLED = 7;

		/**
		 * A constant indicating an audio turn on timer
		 * 
		 * {@hide}
		 */
		public static final int AUDIO_TURNED_ON = 7;

		/**
		 * A constant indicating a video turn on timer
		 * 
		 * {@hide}
		 */
		public static final int VIDEO_TURNED_ON = 8;

		/**
		 * Include all of the data in the stats, including previously saved
		 * data.
		 */
		public static final int STATS_TOTAL = 0;

		/**
		 * Include only the last run in the stats.
		 */
		public static final int STATS_LAST = 1;

		/**
		 * Include only the current run in the stats.
		 */
		public static final int STATS_CURRENT = 2;

		/**
		 * Include only the run since the last time the device was unplugged in
		 * the stats.
		 */
		public static final int STATS_UNPLUGGED = 3;

	}

}