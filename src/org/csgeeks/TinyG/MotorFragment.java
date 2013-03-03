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
		b.putFloat("sa", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.travel_rev)).getText().toString();
		b.putFloat("tr", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.microsteps)).getText().toString();
		b.putInt("mi", Integer.parseInt(scratch));
		b.putBoolean("po", ((ToggleButton)fragView.findViewById(R.id.polarity)).isChecked());
		b.putBoolean("pm", ((ToggleButton)fragView.findViewById(R.id.power_management)).isChecked());
		b.putInt("ma", (int) ((Spinner)fragView.findViewById(R.id.map_axis)).getSelectedItemPosition());

		return b;
	}

	public void myClickHandler(View view) {
		
		if (view.getId() == R.id.save) {
			Spinner s = (Spinner) fragView.findViewById(R.id.motorpick);
			mListener.onMotorSaved((int) s.getSelectedItemPosition()+1, getValues());
		}
	}
	
	public void updateState(Bundle b) {
		Spinner s = (Spinner) fragView.findViewById(R.id.motorpick);
		if (s.getSelectedItemId() == b.getInt("motor")-1) {
			
			((EditText) fragView.findViewById(R.id.step_angle)).setText(Float
					.toString(b.getFloat("sa")));
			((EditText) fragView.findViewById(R.id.travel_rev)).setText(Float
					.toString(b.getFloat("tr")));
			((EditText) fragView.findViewById(R.id.microsteps))
					.setText(Integer.toString(b.getInt("mi")));
			((ToggleButton) fragView.findViewById(R.id.polarity)).setChecked(b
					.getBoolean("po"));
			((ToggleButton) fragView.findViewById(R.id.power_management))
					.setChecked(b.getBoolean("pm"));
			((Spinner) fragView.findViewById(R.id.map_axis)).setSelection(b
					.getInt("ma"));
		}
	}
}
