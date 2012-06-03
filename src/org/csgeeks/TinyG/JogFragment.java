package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class JogFragment extends Fragment {
	private static final String TAG = "TinyG";
	View v;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	Log.d(TAG, "Inflating JogFragment");
        v = inflater.inflate(R.layout.jog, container, false);
        return v;
    }

    public void updateState(Bundle b) {
		((Button) v.findViewById(R.id.units)).setText(b.getString("units"));
    }
}
