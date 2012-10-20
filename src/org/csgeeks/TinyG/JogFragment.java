package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class JogFragment extends SherlockFragment {
	private JogFragmentListener mListener;
	private static final String TAG = "TinyG";
	private float jogRate = 10;
	boolean mEnabled;
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
        mView = inflater.inflate(R.layout.jog, container, false);
        
        ((EditText) mView.findViewById(R.id.x_position)).setText("0");
        ((EditText) mView.findViewById(R.id.y_position)).setText("0");
        ((EditText) mView.findViewById(R.id.z_position)).setText("0");
        ((EditText) mView.findViewById(R.id.i_position)).setText("0");
        ((EditText) mView.findViewById(R.id.j_position)).setText("0");
        ((EditText) mView.findViewById(R.id.k_position)).setText("0");
        ((EditText) mView.findViewById(R.id.r_position)).setText("0");
        return mView;
    }

	public void myClickHandler(View view) {
		TextView tv;
		float tmp;
		
		switch (view.getId()) {
		case R.id.arc_mode:
			enable_radius(((ToggleButton) view).isChecked());
			break;
		}
		
		if (!mEnabled)
			return;
		
		switch (view.getId()) {
		case R.id.xpos:
			tv = (TextView) mView.findViewById(R.id.x_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.xneg:
			tv = (TextView) mView.findViewById(R.id.x_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.ypos:
			tv = (TextView) mView.findViewById(R.id.y_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.yneg:
			tv = (TextView) mView.findViewById(R.id.y_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.zpos:
			tv = (TextView) mView.findViewById(R.id.z_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.zneg:
			tv = (TextView) mView.findViewById(R.id.z_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.ipos:
			tv = (TextView) mView.findViewById(R.id.i_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.ineg:
			tv = (TextView) mView.findViewById(R.id.i_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.jpos:
			tv = (TextView) mView.findViewById(R.id.j_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.jneg:
			tv = (TextView) mView.findViewById(R.id.j_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.kpos:
			tv = (TextView) mView.findViewById(R.id.k_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.kneg:
			tv = (TextView) mView.findViewById(R.id.k_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.rpos:
			tv = (TextView) mView.findViewById(R.id.r_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp += jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.rneg:
			tv = (TextView) mView.findViewById(R.id.r_position);
			tmp = Float.parseFloat(tv.getText().toString());
			tmp -= jogRate;
			tv.setText(Float.toString(tmp));			
			break;
		case R.id.jog:
			EditText xt = (EditText) mView.findViewById(R.id.x_position);
			EditText yt = (EditText) mView.findViewById(R.id.y_position);
			EditText zt = (EditText) mView.findViewById(R.id.z_position);
			mListener.sendGcode("g0x" + xt.getText() + "y" + yt.getText() + "z" + zt.getText());
			break;
//		case R.id.zero:
//			mListener.sendGcode(JSONParser.CMD_ZERO_ALL_AXIS);
//			break;
		}
	}

	private void enable_radius(boolean status) {
		int radius[] = {R.id.r_label, R.id.r_position, R.id.rpos, R.id.rneg};
		int ijk[] = { R.id.i_label, R.id.i_position, R.id.ipos, R.id.ineg,
					R.id.j_label, R.id.j_position, R.id.jpos, R.id.jneg,
					R.id.k_label, R.id.k_position, R.id.kpos, R.id.kneg
					};
		
		for (int i=0; i < 4; i++)
			mView.findViewById(radius[i]).setVisibility((status ? View.VISIBLE : View.INVISIBLE));
		for (int i=0; i < 12; i++)
			mView.findViewById(ijk[i]).setVisibility((status ? View.INVISIBLE : View.VISIBLE));
	}

	public interface JogFragmentListener {
		void jogChange(float rate);
		void sendGcode(String cmd);
	}
	
    public void updateState(Bundle b) {
    	EditText v;
   
		if (b.containsKey("posx")) {
			v = (EditText) mView.findViewById(R.id.x_position);
			v.setText(Float.toString(b.getFloat("posx")));
		}
		if (b.containsKey("posy")) {
			v = (EditText) mView.findViewById(R.id.y_position);
			v.setText(Float.toString(b.getFloat("posy")));
		}
		if (b.containsKey("posz")) {
			v = (EditText) mView.findViewById(R.id.z_position);
			v.setText(Float.toString(b.getFloat("posz")));
		}
		if (b.containsKey("status"))
			mEnabled = b.getString("status").equals("stop") ||
				b.getString("status").equals("end") ||
				b.getString("status").equals("init") ||
				b.getString("status").equals("reset");

//		((Button) mView.findViewById(R.id.units)).setText(b.getString("units"));
    }
}
