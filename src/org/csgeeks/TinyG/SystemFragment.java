package org.csgeeks.TinyG;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class SystemFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private SystemFragmentListener mListener;
	private TextView fb,fv;
	private EditText si;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (SystemFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SystemFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v;
		
		Log.d(TAG, "Creating MachineFragment");

		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.system, container, false);
		fb = ((TextView) v.findViewById(R.id.firmware_build));
		fv = ((TextView) v.findViewById(R.id.firmware_version));
		si = ((EditText) v.findViewById(R.id.system_interval));

		// Ask for state update
		mListener.onSystemSelected();
		
		return v;
	}
	
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.save:
			Log.d(TAG, "SystemFragment save button pushed");
			break;
		}
	}
	
	public interface SystemFragmentListener {
		public void onSystemSelected();		
		public void onSystemSaved(Bundle b);
	}
	
	public void updateState(Bundle b) {
		fb.setText(Float.toString(b.getFloat("fb")));
		fv.setText(Float.toString(b.getFloat("fv")));
		si.setText(Integer.toString(b.getInt("si")));
	}

}
