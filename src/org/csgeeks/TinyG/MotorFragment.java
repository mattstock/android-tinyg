package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class MotorFragment extends Fragment {
	private MotorFragmentListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.motor, container, false);
        
		// configure motor picker
        Activity mActivity = getActivity();
        
		Spinner s = (Spinner) v.findViewById(R.id.motorpick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				mActivity, R.array.motorArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new ItemSelectedListener());

		// Configure axis picker
		s = (Spinner) v.findViewById(R.id.map_axis);
		adapter = ArrayAdapter.createFromResource(mActivity, R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new ItemSelectedListener());
		
		return v;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	try {
    		mListener = (MotorFragmentListener) activity;
    	} catch (ClassCastException e) {
    		throw new ClassCastException(activity.toString() + " must implement MotorFragmentListener");
    	}
    }
    
    public interface MotorFragmentListener {
    	public void onMotorSelected(int m);
    	public void onAxisSelected(int a);
    }
    
	private class ItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			switch (view.getId()) {
			case R.id.motorpick:
				mListener.onMotorSelected(pos+1);
				break;
			case R.id.map_axis:
				mListener.onAxisSelected(pos);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
}
