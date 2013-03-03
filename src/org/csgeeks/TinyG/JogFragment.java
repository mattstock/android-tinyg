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
	private static final String CMD_SET_ORIGIN = "g92x0y0z0a0";
	private static final String CMD_STEP_FORMAT = "g91f%fg1%s%f";
	private static final String CMD_MOVE_ORIGIN = "g90g0x0y0z0a0";
	private static int[] jogControls = { R.id.xpos, R.id.xneg, R.id.ypos,
			R.id.yneg, R.id.zpos, R.id.zneg };
	private static int[] allButtons = { R.id.xpos, R.id.xneg, R.id.ypos,
			R.id.yneg, R.id.zpos, R.id.zneg, R.id.jogRate, R.id.home,
			R.id.origin };
	private float jogStep = 10;
	private float jogRate = jogFastRate;
	private TextView jogStepView;
	boolean enabled = true;
	boolean jogActive = false;

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

		for (int id : allButtons) {
			v = mView.findViewById(id);
			v.setOnClickListener(clickListener);
		}

		// Disable for now, until the queue flush support is ready.
/*		for (int id : jogControls) {
			v = mView.findViewById(id);
			v.setOnLongClickListener(jogHoldListener);
			v.setOnTouchListener(jogReleaseListener);
		}
*/
		// Rapid movement
		((ToggleButton) mView.findViewById(R.id.jogRate)).setChecked(true);

		// Jog step changes
		jogStepView = (TextView) mView.findViewById(R.id.jog_step_value);
		jogStepView.setText(Integer.toString((int) jogStep));

		SeekBar s = (SeekBar) mView.findViewById(R.id.jog_step);
		s.setOnSeekBarChangeListener(jogStepListener);
		s.setProgress((int) jogStep);

		return mView;
	}

	public interface JogFragmentListener {
		void jogChange(float rate);

		void sendGcode(String cmd);

		void stopMove();
	}

	private SeekBar.OnSeekBarChangeListener jogStepListener = new SeekBar.OnSeekBarChangeListener() {

		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			jogStep = progress;

			if (jogStepView != null)
				jogStepView.setText(Integer.toString((int) jogStep));
		}
	};

	private View.OnClickListener clickListener = new View.OnClickListener() {

		public void onClick(View v) {

			// These buttons should work all the time
			switch (v.getId()) {
			case R.id.jogRate:
				if (((ToggleButton) v).isChecked())
					jogRate = jogFastRate;
				else
					jogRate = jogSlowRate;
				break;
			}

			if (!enabled)
				return;

			// These only work if we're in a happy state
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
			case R.id.origin:
				parent.sendGcode(CMD_SET_ORIGIN);
				break;
			case R.id.home:
				parent.sendGcode(CMD_MOVE_ORIGIN);
				break;
			}
		}
	};

	private View.OnLongClickListener jogHoldListener = new View.OnLongClickListener() {
		public boolean onLongClick(View v) {
			if (!enabled)
				return false;

			switch (v.getId()) {
			case R.id.xpos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "x",
						400f));
				jogActive = true;
				return true;
			case R.id.xneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "x",
						-400f));
				jogActive = true;
				return true;
			case R.id.ypos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "y",
						400f));
				jogActive = true;
				return true;
			case R.id.yneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "y",
						-400f));
				jogActive = true;
				return true;
			case R.id.zpos:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "z",
						400f));
				jogActive = true;
				return true;
			case R.id.zneg:
				parent.sendGcode(String.format(CMD_STEP_FORMAT, jogRate, "z",
						-400f));
				jogActive = true;
				return true;
			}
			return false;
		}
	};

	private View.OnTouchListener jogReleaseListener = new View.OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (!jogActive)
					return false;
				jogActive = false;
				switch (v.getId()) {
				case R.id.xpos:
				case R.id.xneg:
				case R.id.ypos:
				case R.id.yneg:
				case R.id.zpos:
				case R.id.zneg:
					parent.stopMove();
					enabled = true;
					return false;
				}
			}
			return false;
		}
	};

	// Used to use this for enable/disable of buttons, but maybe it's ok to
	// queue things up.
	public void updateState(Bundle b) {
	}
}
