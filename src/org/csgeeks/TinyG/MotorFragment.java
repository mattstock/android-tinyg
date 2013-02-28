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
	private View fragView;
	
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "Creating MotorFragment");
		// Inflate the layout for this fragment
		fragView = inflater.inflate(R.layout.motor, container, false);

		Spinner s = (Spinner) fragView.findViewById(R.id.motorpick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.motorArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new MotorItemSelectedListener());

		
		// Configure axis picker
		s = (Spinner) fragView.findViewById(R.id.map_axis);
		adapter = ArrayAdapter.createFromResource(getActivity(), R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		return fragView;
	}

	public interface MotorFragmentListener {
		public void onMotorSelected(int m);
		public void onMotorSaved(int m, Bundle b);
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
	
	// Pull all of the field values into a Bundle
	private Bundle getValues() {
		Bundle b = new Bundle();
		String scratch;

		scratch = ((EditText)fragView.findViewById(R.id.step_angle)).getText().toString();
		b.putFloat("step_angle", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.travel_rev)).getText().toString();
		b.putFloat("travel_rev", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.microsteps)).getText().toString();
		b.putInt("microsteps", Integer.parseInt(scratch));
		b.putBoolean("polarity", ((ToggleButton)fragView.findViewById(R.id.polarity)).isChecked());
		b.putBoolean("power_management", ((ToggleButton)fragView.findViewById(R.id.power_management)).isChecked());
		b.putInt("map_to_axis", (int) ((Spinner)fragView.findViewById(R.id.map_axis)).getSelectedItemId()+1);

		return b;
	}

	public void myClickHandler(View view) {
		
		if (view.getId() == R.id.save) {
			Spinner s = (Spinner) fragView.findViewById(R.id.motorpick);
			mListener.onMotorSaved((int) s.getSelectedItemId()+1, getValues());
		}
	}
	
	public void updateState(Bundle b) {
		Spinner s = (Spinner) fragView.findViewById(R.id.motorpick);
		if (s.getSelectedItemId() == b.getInt("motor")-1) {
			((EditText) fragView.findViewById(R.id.step_angle)).setText(Float
					.toString(b.getFloat("step_angle")));
			((EditText) fragView.findViewById(R.id.travel_rev)).setText(Float
					.toString(b.getFloat("travel_rev")));
			((EditText) fragView.findViewById(R.id.microsteps))
					.setText(Integer.toString(b.getInt("microsteps")));
			((ToggleButton) fragView.findViewById(R.id.polarity)).setChecked(b
					.getBoolean("polarity"));
			((ToggleButton) fragView.findViewById(R.id.power_management))
					.setChecked(b.getBoolean("power_management"));
			((Spinner) fragView.findViewById(R.id.map_axis)).setSelection(b
					.getInt("map_to_axis"));
		}
	}
}
