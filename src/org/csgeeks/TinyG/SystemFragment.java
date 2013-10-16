package org.csgeeks.TinyG;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class SystemFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private SystemFragmentListener parent;
	private View fragView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			parent = (SystemFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SystemFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Spinner s;

		Log.d(TAG, "Creating MachineFragment");

		// Inflate the layout for this fragment
		fragView = inflater.inflate(R.layout.system, container, false);

		// Configure switch type picker
		s = (Spinner) fragView.findViewById(R.id.switch_type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.stArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		// Configure hardware version picker
		s = (Spinner) fragView.findViewById(R.id.hardware_version);
		adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.hvArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		// Allow for saves
		((Button) fragView.findViewById(R.id.save))
				.setOnClickListener(clickListener);

		// Ask for state update
		parent.onSystemSelected();

		return fragView;
	}

	// Pull all of the field values into a Bundle
	private Bundle getValues() {
		Bundle b = new Bundle();

		switch (((Spinner) fragView.findViewById(R.id.hardware_version))
				.getSelectedItemPosition()) {
		case 0:
			b.putInt("hv", 6);
			break;
		case 1:
			b.putInt("hv", 7);
			break;
		case 2:
			b.putInt("hv", 8);
			break;
		}

		b.putInt("st", ((Spinner) fragView.findViewById(R.id.switch_type)).getSelectedItemPosition());

		return b;
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.save:
				Log.d(TAG, "SystemFragment save button pushed");
				parent.onSystemSaved(getValues());
				break;
			}
		}
	};

	public interface SystemFragmentListener {
		public void onSystemSelected();

		public void onSystemSaved(Bundle b);
	}

	public void updateState(Bundle b) {
		int hv_idx;

		switch (b.getInt("hv")) {
		case 6:
			hv_idx = 0;
			break;
		case 7:
			hv_idx = 1;
			break;
		case 8:
			hv_idx = 2;
			break;
		default:
			hv_idx = -1;
			break;
		}
		((Spinner) fragView.findViewById(R.id.hardware_version))
				.setSelection(hv_idx);
		((Spinner) fragView.findViewById(R.id.switch_type)).setSelection(b
				.getInt("st"));
		((TextView) fragView.findViewById(R.id.firmware_build)).setText(Float
				.toString(b.getFloat("fb")));
		((TextView) fragView.findViewById(R.id.firmware_version)).setText(Float
				.toString(b.getFloat("fv")));
		((TextView) fragView.findViewById(R.id.id)).setText(b.getString("id"));
		((TextView) fragView.findViewById(R.id.system_interval))
				.setText(Integer.toString(b.getInt("si")));
	}

}
