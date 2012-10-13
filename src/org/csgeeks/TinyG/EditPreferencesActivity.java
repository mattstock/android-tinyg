package org.csgeeks.TinyG;

import java.util.List;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Build;
import android.os.Bundle;

public class EditPreferencesActivity extends SherlockPreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
		  addPreferencesFromResource(R.xml.preferences);
		  addPreferencesFromResource(R.xml.preferences_net);
	  }
	}
	
	 @Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.preference_headers, target);
	  }	
}
	

