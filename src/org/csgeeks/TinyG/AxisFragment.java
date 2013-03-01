package org.csgeeks.TinyG;

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

public class AxisFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private AxisFragmentListener mListener;
	private View fragView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (AxisFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement AxisFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		fragView = inflater.inflate(R.layout.axis, container, false);

		Spinner s = (Spinner) fragView.findViewById(R.id.axispick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new AxisItemSelectedListener());

		return fragView;
	}

	public interface AxisFragmentListener {
		public void onAxisSelected(int a);
		public void onAxisSaved(int a, Bundle b);
	}

	public class AxisItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mListener.onAxisSelected(pos);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void myClickHandler(View view) {
		if (view.getId() == R.id.save) {
			Spinner s = (Spinner) fragView.findViewById(R.id.axispick);
			mListener.onAxisSaved((int) s.getSelectedItemId(), getValues());
		}
	}

	// Pull all of the field values into a Bundle
	private Bundle getValues() {
		Bundle b = new Bundle();
		String scratch;

		scratch = ((EditText)fragView.findViewById(R.id.feed_rate)).getText().toString();
		b.putFloat("feed_rate", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.search_velocity)).getText().toString();
		b.putFloat("search_velocity", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.latch_velocity)).getText().toString();
		b.putFloat("latch_velocity", Float.parseFloat(scratch));
		b.putBoolean("axis_mode", ((ToggleButton)fragView.findViewById(R.id.axis_mode)).isChecked());
		scratch = ((EditText)fragView.findViewById(R.id.switch_min)).getText().toString();
		b.putInt("switch_min", Integer.parseInt(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.switch_max)).getText().toString();
		b.putInt("switch_max", Integer.parseInt(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.latch_backoff)).getText().toString();
		b.putFloat("latch_backoff", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.zero_backoff)).getText().toString();
		b.putFloat("zero_backoff", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.velocity_max)).getText().toString();
		b.putFloat("velocity_max", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.travel_max)).getText().toString();
		b.putFloat("travel_max", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.jerk_max)).getText().toString();
		b.putFloat("jerk_max", Float.parseFloat(scratch));
		scratch = ((EditText)fragView.findViewById(R.id.junction_deviation)).getText().toString();
		b.putFloat("junction_deviation", Float.parseFloat(scratch));

		return b;
	}

	public void updateState(Bundle b) {
		Spinner s = (Spinner) fragView.findViewById(R.id.axispick);
		if (s.getSelectedItemId() == b.getInt("axis")) {
			((EditText) fragView.findViewById(R.id.feed_rate)).setText(Float
					.toString(b.getFloat("feed_rate")));
			((EditText) fragView.findViewById(R.id.search_velocity))
					.setText(Float.toString(b.getFloat("search_velocity")));
			((EditText) fragView.findViewById(R.id.latch_velocity))
					.setText(Float.toString(b.getFloat("latch_velocity")));
			((ToggleButton) fragView.findViewById(R.id.axis_mode))
					.setChecked(b.getBoolean("axis_mode"));
			((EditText) fragView.findViewById(R.id.switch_min))
					.setText(Integer.toString(b.getInt("switch_min")));
			((EditText) fragView.findViewById(R.id.switch_max))
					.setText(Integer.toString(b.getInt("switch_max")));
			((EditText) fragView.findViewById(R.id.latch_backoff))
					.setText(Float.toString(b.getFloat("latch_backoff")));
			((EditText) fragView.findViewById(R.id.zero_backoff))
					.setText(Float.toString(b.getFloat("zero_backoff")));
			((EditText) fragView.findViewById(R.id.velocity_max))
					.setText(Float.toString(b.getFloat("velocity_max")));
			((EditText) fragView.findViewById(R.id.travel_max)).setText(Float
					.toString(b.getFloat("travel_max")));
			((EditText) fragView.findViewById(R.id.jerk_max)).setText(Float
					.toString(b.getFloat("jerk_max")));
			((EditText) fragView.findViewById(R.id.junction_deviation))
					.setText(Float.toString(b.getFloat("junction_deviation")));
		}
	}
}
