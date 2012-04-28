package org.csgeeks.TinyG;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ShowSettingsActivity extends PreferenceActivity {
	@Override
	 protected void onCreate(Bundle savedInstanceState) {

	  super.onCreate(savedInstanceState);
	  addPreferencesFromResource(R.xml.preferences);        
	 }    
}
