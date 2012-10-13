package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

public class MotorFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private MotorFragmentListener mListener;
	private Activity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "Creating MotorFragment");
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.motor, container, false);

		// configure motor picker
		mActivity = getActivity();

		Spinner s = (Spinner) v.findViewById(R.id.motorpick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				mActivity, R.array.motorArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new MotorItemSelectedListener());

		
		// Configure axis picker
		s = (Spinner) v.findViewById(R.id.map_axis);
		adapter = ArrayAdapter.createFromResource(mActivity, R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new AxisItemSelectedListener());

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (MotorFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement MotorFragmentListener");
		}
	}

	public interface MotorFragmentListener {
		public void onMotorSelected(int m);
		public void onAxisSelected(int a);
	}

	private class MotorItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mListener.onMotorSelected(pos+1);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	private class AxisItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mListener.onAxisSelected(pos);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void updateState(Bundle b) {
		Spinner s = (Spinner) mActivity.findViewById(R.id.motorpick);
		if (s.getSelectedItemId() == b.getInt("motor")-1) {
			((EditText) mActivity.findViewById(R.id.step_angle)).setText(Float
					.toString(b.getFloat("step_angle")));
			((EditText) mActivity.findViewById(R.id.travel_rev)).setText(Float
					.toString(b.getFloat("travel_rev")));
			((EditText) mActivity.findViewById(R.id.microsteps))
					.setText(Integer.toString(b.getInt("microsteps")));
			((ToggleButton) mActivity.findViewById(R.id.polarity)).setChecked(b
					.getBoolean("polarity"));
			((ToggleButton) mActivity.findViewById(R.id.power_management))
					.setChecked(b.getBoolean("power_management"));
			((Spinner) mActivity.findViewById(R.id.map_axis)).setSelection(b
					.getInt("map_to_axis"));
		}
	}
}
