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
		Log.d(TAG, "Creating MachineFragment");

		// Asked for state update
		mListener.onSystemSelected();
		
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.system, container, false);
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
	}
	
	public void updateState(Bundle b) {
		Activity a = getActivity();
		((TextView) a.findViewById(R.id.firmware_build)).setText(Float.toString(b.getFloat("firmware_build")));
		((TextView) a.findViewById(R.id.firmware_version)).setText(Float.toString(b.getFloat("firmware_version")));
		((EditText) a.findViewById(R.id.system_interval)).setText(Integer.toString(b.getInt("system_interval")));
	}

}
