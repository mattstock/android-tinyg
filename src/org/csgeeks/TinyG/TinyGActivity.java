package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Support.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TinyGActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
	private TinyGMessenger tinyg;
	private float jogRate = 10;
	private int bindType = 0;
	private ServiceConnection mConnection;
	private BroadcastReceiver mIntentReceiver;
	private static final int DIALOG_ABOUT = 0;
	
	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter();
		updateFilter.addAction(TinyGDriver.STATUS);
		updateFilter.addAction(TinyGDriver.CONNECTION_STATUS);
		mIntentReceiver = new TinyGServiceReceiver();
		registerReceiver(mIntentReceiver, updateFilter);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(mIntentReceiver);
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mConnection = new DriverServiceConnection();

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}
		
		if (bindDriver(mConnection) == false) { 
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private boolean bindDriver(ServiceConnection s) {
		switch (bindType) {
		case 0:
			return bindService(new Intent(this, TinyGNetwork.class),
					s, Context.BIND_AUTO_CREATE);
		case 1:
			return bindService(new Intent(TinyGDriver.USB_SERVICE),
					s, Context.BIND_AUTO_CREATE);
		default:
			return false;
		}
	}

	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	public class TinyGServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TinyGDriver.STATUS)) {
				// TODO pull data out, update fields
			}
			if (action.equals(TinyGDriver.CONNECTION_STATUS)) {
				// TODO pull data out, update button, etc.
			}
		}
	}
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, ShowSettingsActivity.class));
			return true;
		case R.id.about:
			showDialog(DIALOG_ABOUT);
			return true;
		case R.id.machine:
			startActivity(new Intent(this, MachineActivity.class));
			return true;
		case R.id.motors:
			startActivity(new Intent(this, MotorActivity.class));
			return true;
		case R.id.axis:
			startActivity(new Intent(this, AxisActivity.class));
			return true;
		case R.id.refresh:
			tinyg.send_command(TinyGDriver.REFRESH);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public Dialog onCreateDialog(int arg) {
		switch (arg) {
		case DIALOG_ABOUT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.about)
				.setTitle(R.string.app_name);
			return builder.create();
		}
		return null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat("jogRate", jogRate);
		outState.putInt("bindType", bindType);
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
		bindType = inState.getInt("bindType");
	}

	public void myClickHandler(View view) {
		if (tinyg == null)
			return;
		switch (view.getId()) {
		case R.id.connect:
			if (tinyg.isConnected()) {
				tinyg.send_command(TinyGDriver.DISCONNECT);
				((Button) view).setText(R.string.connect);
			} else {
				tinyg.send_command(TinyGDriver.CONNECT);
				((Button) view).setText(R.string.disconnect);
			}
			break;
		}
		// If we're ready, handle buttons that will send messages to TinyG
		if (tinyg != null && tinyg.isConnected()) {
			switch (view.getId()) {
			case R.id.xpos:
				tinyg.send_gcode("{\"gc\": \"g91g0x" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.xneg:
				tinyg.send_gcode("{\"gc\": \"g91g0x" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.ypos:
				tinyg.send_gcode("{\"gc\": \"g91g0y" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.yneg:
				tinyg.send_gcode("{\"gc\": \"g91g0y" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.zpos:
				tinyg.send_gcode("{\"gc\": \"g91g0z" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.zneg:
				tinyg.send_gcode("{\"gc\": \"g91g0z" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.apos:
				tinyg.send_gcode("{\"gc\": \"g91g0a" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.aneg:
				tinyg.send_gcode("{\"gc\": \"g91g0a" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.rpos:
				jogRate += 1;
				break;
			case R.id.rneg:
				jogRate -= 1;
				break;
			case R.id.units:
				break;
			case R.id.zero:
				tinyg.send_gcode(Parser.CMD_ZERO_ALL_AXIS);
				break;
			}
		}
	}

	private class DriverServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = new TinyGMessenger(new Messenger(service));
		}

		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}

	// TODO: need to fix this to be updated based on messages
	public void updateState(Machine machine) {
		((TextView) findViewById(R.id.xloc)).setText(Float.toString(machine
				.getAxisByName("X").getWork_position()));
		((TextView) findViewById(R.id.yloc)).setText(Float.toString(machine
				.getAxisByName("Y").getWork_position()));
		((TextView) findViewById(R.id.zloc)).setText(Float.toString(machine
				.getAxisByName("Z").getWork_position()));
		((TextView) findViewById(R.id.aloc)).setText(Float.toString(machine
				.getAxisByName("A").getWork_position()));
		((TextView) findViewById(R.id.jogval)).setText(Float.toString(jogRate));
		((TextView) findViewById(R.id.line)).setText(Integer.toString(machine
				.getLine_number()));
		switch (machine.getMotionMode()) {
		case traverse:
			((TextView) findViewById(R.id.momo)).setText(R.string.traverse);
			break;
		case straight:
			((TextView) findViewById(R.id.momo)).setText(R.string.straight);
			break;
		case cw_arc:
			((TextView) findViewById(R.id.momo)).setText(R.string.cw);
			break;
		case ccw_arc:
			((TextView) findViewById(R.id.momo)).setText(R.string.ccw);
			break;
		case invalid:
			((TextView) findViewById(R.id.momo)).setText(R.string.invalid);
			break;
		}
		switch (machine.getMachineState()) {
		case reset:
			((TextView) findViewById(R.id.status)).setText(R.string.reset);
			break;
		case nop:
			((TextView) findViewById(R.id.status)).setText(R.string.nop);
			break;
		case stop:
			((TextView) findViewById(R.id.status)).setText(R.string.stop);
			break;
		case end:
			((TextView) findViewById(R.id.status)).setText(R.string.end);
			break;
		case run:
			((TextView) findViewById(R.id.status)).setText(R.string.run);
			break;
		case hold:
			((TextView) findViewById(R.id.status)).setText(R.string.hold);
			break;
		case homing:
			((TextView) findViewById(R.id.status)).setText(R.string.homing);
			break;
		}
		switch (machine.getUnitMode()) {
		case INCHES:
			((Button) findViewById(R.id.units)).setText(R.string.inch);
		case MM:
			((Button) findViewById(R.id.units)).setText(R.string.mm);
		}
		((TextView) findViewById(R.id.velocity)).setText(Float.toString(machine
				.getVelocity()));
	}
}