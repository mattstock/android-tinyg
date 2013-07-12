package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Support.Machine;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class JogFragment extends SherlockFragment {
	private JogFragmentListener parent;
	private static final String TAG = "TinyG";
	private static float jogSlowRate[] = { 400f, 300f, 100f, 10f, 10f, 10f };
	private static final String CMD_ZERO_AXIS = "g28.3%s0";
	private static final String CMD_JOG_SLOW = "g91 f%f g1%s%f";
	private static final String CMD_JOG_FULL = "g91 g0%s%f";
	private static final String CMD_MOVE_ORIGIN = "g90g0x0y0z0a0";
	private static int[] allButtons = { R.id.xpos, R.id.xneg, R.id.ypos,
			R.id.yneg, R.id.zpos, R.id.zneg, R.id.jogRate, R.id.home,
			R.id.xzero, R.id.yzero, R.id.zzero, R.id.spindle, R.id.coolant,
			R.id.step_001, R.id.step_01, R.id.step_1, R.id.step_1_0, R.id.step_10,
			R.id.g28, R.id.reset, R.id.go };
	private static int[] jogControls = { R.id.xpos, R.id.xneg, R.id.ypos, R.id.yneg, R.id.zpos, R.id.zneg }; 
	private float jogStep = 10;
	boolean jogActive = false;
	private ToggleButton jogRateButton;
	
	View view;

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
		view = inflater.inflate(R.layout.jog, container, false);
		View v;

		for (int id : allButtons) {
			v = view.findViewById(id);
			v.setOnClickListener(clickListener);
		}

		
		 for (int id : jogControls) { 
			 v = view.findViewById(id);
			 v.setOnLongClickListener(jogHoldListener);
			 v.setOnTouchListener(jogReleaseListener);
		 }
		 
		 // Rapid movement
		 jogRateButton = (ToggleButton) view.findViewById(R.id.jogRate);
		 jogRateButton.setChecked(true);
		
		((RadioButton) view.findViewById(R.id.step_10)).setChecked(true);
		return view;
	}

	public interface JogFragmentListener {
		void sendGcode(String cmd);

		void sendReset();

		void stopMove();
		
		int queueSize();
		
		void goHome();
	}

	private String getRateByAxis(int axis, float step) {
		if (jogRateButton.isChecked())
			return String.format(CMD_JOG_FULL, Machine.axisIndexToName[axis], step);
		else
			return String.format(CMD_JOG_SLOW, jogSlowRate[axis], Machine.axisIndexToName[axis], step);
	}
	
	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View v) {

			// These buttons should work all the time
			switch (v.getId()) {
			case R.id.step_001:
				jogStep = 0.001f;
				break;
			case R.id.step_01:
				jogStep = 0.01f;
				break;
			case R.id.step_1:
				jogStep = 0.1f;
				break;
			case R.id.step_1_0:
				jogStep = 1.0f;
				break;
			case R.id.step_10:
				jogStep = 10.0f;
				break;				
			case R.id.xpos:
				parent.sendGcode(getRateByAxis(0, jogStep));
				break;
			case R.id.xneg:
				parent.sendGcode(getRateByAxis(0, -jogStep));
				break;
			case R.id.ypos:
				parent.sendGcode(getRateByAxis(1, jogStep));
				break;
			case R.id.yneg:
				parent.sendGcode(getRateByAxis(1, -jogStep));
				break;
			case R.id.zpos:
				parent.sendGcode(getRateByAxis(2, jogStep));
				break;
			case R.id.zneg:
				parent.sendGcode(getRateByAxis(2, -jogStep));
				break;
			case R.id.zzero:
				parent.sendGcode(String.format(CMD_ZERO_AXIS, "z"));
				break;
			case R.id.yzero:
				parent.sendGcode(String.format(CMD_ZERO_AXIS, "y"));
				break;
			case R.id.xzero:
				parent.sendGcode(String.format(CMD_ZERO_AXIS, "x"));
				break;
			case R.id.coolant:
				if (((ToggleButton) v).isChecked())
					parent.sendGcode("M7");
				else
					parent.sendGcode("M9");
				break;
			case R.id.spindle:
				if (((ToggleButton) v).isChecked())
					parent.sendGcode("M3 200");
				else
					parent.sendGcode("M5");
				break;
			case R.id.reset:
				parent.sendReset();
				break;
			case R.id.g28:
				parent.goHome();
				break;
			case R.id.go:
				String text = ((EditText)view.findViewById(R.id.gcode)).getText().toString();
				parent.sendGcode(text);
				break;
			case R.id.home:
				parent.sendGcode(CMD_MOVE_ORIGIN);
				break;
			}
		}
	};

	private View.OnLongClickListener jogHoldListener = new View.OnLongClickListener() {
		public boolean onLongClick(View v) {
			switch (v.getId()) {
			case R.id.xpos:
				parent.sendGcode(getRateByAxis(0, 100f));
				jogActive = true;
				return true;
			case R.id.xneg:
				parent.sendGcode(getRateByAxis(0, -100f));
				jogActive = true;
				return true;
			case R.id.ypos:
				parent.sendGcode(getRateByAxis(1, 100f));
				jogActive = true;
				return true;
			case R.id.yneg:
				parent.sendGcode(getRateByAxis(1, -100f));
				jogActive = true;
				return true;
			case R.id.zpos:
				parent.sendGcode(getRateByAxis(2, 100f));
				jogActive = true;
				return true;
			case R.id.zneg:
				parent.sendGcode(getRateByAxis(2, -100f));
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
					return false;
				}
			}
			return false;
		}
	};

	// Used to use this for enable/disable of buttons, but maybe it's ok to
	// queue things up.
	public void updateState(Machine m) {
		for (int i=0; i < 6; i++)
			jogSlowRate[i] = (m.getAxisBundle(i).getFloat("vm"))/2f;

	}
}
