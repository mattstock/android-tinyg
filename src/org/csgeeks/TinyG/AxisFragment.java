package org.csgeeks.TinyG;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
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
	private AxisFragmentListener mListener;
	private Activity mActivity;

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
		View v = inflater.inflate(R.layout.axis, container, false);

		// configure axis picker
		mActivity = getActivity();

		Spinner s = (Spinner) v.findViewById(R.id.axispick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				mActivity, R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new AxisItemSelectedListener());

		return v;
	}

	public interface AxisFragmentListener {
		public void onAxisSelected(int a);
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

	public void updateState(Bundle b) {
		Spinner s = (Spinner) mActivity.findViewById(R.id.axispick);
		if (s.getSelectedItemId() == b.getInt("axis")) {
			((EditText) mActivity.findViewById(R.id.feed_rate)).setText(Float
					.toString(b.getFloat("feed_rate")));
			((EditText) mActivity.findViewById(R.id.search_velocity))
					.setText(Float.toString(b.getFloat("search_velocity")));
			((EditText) mActivity.findViewById(R.id.latch_velocity))
					.setText(Float.toString(b.getFloat("latch_velocity")));
			((ToggleButton) mActivity.findViewById(R.id.axis_mode))
					.setChecked(b.getBoolean("axis_mode"));
			((EditText) mActivity.findViewById(R.id.switch_mode))
					.setText(Integer.toString(b.getInt("switch_mode")));
			((EditText) mActivity.findViewById(R.id.velocity_max))
					.setText(Float.toString(b.getFloat("velocity_max")));
			((EditText) mActivity.findViewById(R.id.travel_max)).setText(Float
					.toString(b.getFloat("travel_max")));
			((EditText) mActivity.findViewById(R.id.jerk_max)).setText(Float
					.toString(b.getFloat("jerk_max")));
			((EditText) mActivity.findViewById(R.id.junction_deviation))
					.setText(Float.toString(b.getFloat("junction_deviation")));
		}
	}
}
