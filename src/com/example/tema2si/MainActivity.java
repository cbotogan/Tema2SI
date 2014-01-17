package com.example.tema2si;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    public static PowerUsage powerUsageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        powerUsageService = null;
        powerUsageService = new PowerUsage(this);
        
        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                        }),
                this);
        
        powerUsageService.start();
        //pu.start();
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";
        Context context;
        
        public DummySectionFragment() {        	
        }    
        

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView DeviceInfoTextView;
            context = getActivity();
            
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1){
            	
            	DeviceInfoTextView = (TextView) rootView.findViewById(R.id.section_label);
            	DeviceInfoTextView.setText(
            			"Model device: " + Build.MODEL + "\n" +
            			"Versiune Android: " + Build.VERSION.RELEASE + "\n" +
            			getRAM() + 
            			getDiskSpace() +
            			"CPU: " + Build.CPU_ABI + "\n" +
            			"Battery: " + getBatteryLevel()            			
            			);
            }
            
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 2){
            	
            	Toast.makeText(context, "Refresh by entering this tab again", Toast.LENGTH_LONG).show();
            	ArrayList<PowerEvent> rows = MainActivity.powerUsageService.getHighestDrainers();         	
            	
            	ArrayAdapterAppInfo ad = new ArrayAdapterAppInfo(context, rows);
            	ListView lw = (ListView) rootView.findViewById(R.id.listview_apps);
            	rootView.findViewById(R.id.section_label).setVisibility(View.GONE);
            	lw.setAdapter(ad);      	            	
                
            }

            return rootView;
        }
        
        public String getRAM(){
        	ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        	MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        	actManager.getMemoryInfo(memInfo);
        	
        	long totalRAM = memInfo.totalMem / 1024;
        	long freeRAM = memInfo.availMem / 1024;
        	
        	return "Total RAM: " + Long.toString(totalRAM) + " kB\n" 
        		+ "Free RAM: " + Long.toString(freeRAM) + " kB\n";
        }
        
        public String getDiskSpace(){
        	// get total-free disk space
        	StatFs fs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        	long totalSpace = (long) (fs.getBlockCount() * fs.getBlockSize()) / 1048576;
        	long freeSpace = (long) (fs.getAvailableBlocks() * fs.getBlockSize()) / 1048576;
        	return "Total Disk Space: " + Long.toString(totalSpace) + " MB\n" +
				"Free Disk Space: " + Long.toString(freeSpace) + " MB\n";
        }
        
        public String getBatteryLevel(){
        	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        	Intent batteryStatus = context.registerReceiver(null, ifilter);
        	
        	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        	return Integer.toString(level) + "%";       	
        	
        }
        
    }

}
