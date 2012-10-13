package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Support.JSONParser;
import org.csgeeks.TinyG.Support.TinyGMessenger;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class JogFragment extends SherlockFragment {
	private JogFragmentListener mListener;
	private static final String TAG = "TinyG";
	private float jogRate = 10;
	View mView;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (JogFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement JogFragmentListener");
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	Log.d(TAG, "Inflating JogFragment");
        mView = inflater.inflate(R.layout.jog, container, false);
        return mView;
    }

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.xpos:
			mListener.sendGcode(TinyGMessenger.short_jog("x", jogRate));
			break;
		case R.id.xneg:
			mListener.sendGcode(TinyGMessenger.short_jog("x", -jogRate));
			break;
		case R.id.ypos:
			mListener.sendGcode(TinyGMessenger.short_jog("y", jogRate));
			break;
		case R.id.yneg:
			mListener.sendGcode(TinyGMessenger.short_jog("y", -jogRate));
			break;
		case R.id.zpos:
			mListener.sendGcode(TinyGMessenger.short_jog("z", jogRate));
			break;
		case R.id.zneg:
			mListener.sendGcode(TinyGMessenger.short_jog("z", -jogRate));
			break;
		case R.id.apos:
			mListener.sendGcode(TinyGMessenger.short_jog("a", jogRate));
			break;
		case R.id.aneg:
			mListener.sendGcode(TinyGMessenger.short_jog("a", -jogRate));
			break;
		case R.id.rpos:
			jogRate += 1;
			mListener.jogChange(jogRate);
			break;
		case R.id.rneg:
			jogRate -= 1;
			mListener.jogChange(jogRate);
			break;
		case R.id.units:
			break;
		case R.id.zero:
			mListener.sendGcode(JSONParser.CMD_ZERO_ALL_AXIS);
			break;
		}
	}

	public interface JogFragmentListener {
		void jogChange(float rate);
		void sendGcode(String cmd);
	}
	
    public void updateState(Bundle b) {
		((Button) mView.findViewById(R.id.units)).setText(b.getString("units"));
    }
}
