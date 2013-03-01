package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class JogFragment extends SherlockFragment {
	private JogFragmentListener parent;
	private static final String TAG = "TinyG";
	private static float jogFastRate = 400;
	private static float jogSlowRate = 100;
	private static final String CMD_STEP_FORMAT = "g1f%f%s%f\n";
	private static int[] jogcontrols = { R.id.xpos, R.id.xneg, R.id.ypos,
			R.id.yneg, R.id.zpos, R.id.zneg };
	private float jogStep = 10;
	private float jogRate = jogFastRate;
	private TextView jogStepView;
	boolean enabled;

	View mView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			parent = (JogFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement JogFragmentListener");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.jog, container, false);
		View v;

		for (int id : jogcontrols) {
			v = mView.findViewById(id);
			v.setOnLongClickListener(jogHoldListener);
			v.setOnClickListener(clickListener);
			v.setOnTouchListener(jogReleaseListener);
		}

		// Rapid movement
		((ToggleButton)mView.findViewById(R.id.jogRate)).setChecked(true);
		
		// Jog step changes
		SeekBar s = (SeekBar)mView.findViewById(R.id.jog_step);
		s.setOnSeekBarChangeListener(jogStepListener);
		s.setProgress((int)jogStep);
		
		jogStepView = (TextView) mView.findViewById(R.id.jog_step_value);
		jogStepView.setText(Integer.toString((int)jogStep));
		return mView;
	}

	public interface JogFragmentListener {
		void jogChange(float rate);
		void sendGcode(String cmd);
	}

	private SeekBar.OnSeekBarChangeListener jogStepListener = new SeekBar.OnSeekBarChangeListener() {
		
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			jogStep = progress;
			jogStepView.setText(Integer.toString((int)jogStep));
		}
	};
	
	private View.OnClickListener clickListener = new View.OnClickListener() {

		public void onClick(View v) {

			if (!enabled)
				return;

			switch (v.getId()) {
			case R.id.xpos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "x",
						jogStep));
				break;
			case R.id.xneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "x",
						-jogStep));
				break;
			case R.id.ypos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "y",
						jogStep));
				break;
			case R.id.yneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "y",
						-jogStep));
				break;
			case R.id.zpos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "z",
						jogStep));
				break;
			case R.id.zneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "z",
						-jogStep));
				break;
			case R.id.jogRate:
				if (((ToggleButton)v).isChecked())
					jogRate = jogFastRate;
				else
					jogRate = jogSlowRate;
				break;
			// case R.id.zero:
			// mListener.sendGcode(JSONParser.CMD_ZERO_ALL_AXIS);
			// break;
			}
		}
	};

	private View.OnLongClickListener jogHoldListener = new View.OnLongClickListener() {
		public boolean onLongClick(View v) {
			switch (v.getId()) {
			case R.id.xpos:
				// TODO Initiate jog on this axis
				Log.d(TAG, "start jog on x+");
				return true;
			case R.id.xneg:
				return true;
			}
			return false;
		}
	};

	private View.OnTouchListener jogReleaseListener = new View.OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.xpos:
					// TODO Stop jog on this axis
					Log.d(TAG, "stop jog on x+");
					return true;
				case R.id.xneg:
					return true;
				}
			}
			return false;
		}
	};

	public void updateState(Bundle b) {
		if (b.containsKey("status"))
			enabled = b.getString("status").equals("stop")
					|| b.getString("status").equals("end")
					|| b.getString("status").equals("init")
					|| b.getString("status").equals("reset");
	}
}
