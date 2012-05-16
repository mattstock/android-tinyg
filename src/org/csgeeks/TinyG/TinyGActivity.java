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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
	private boolean connected = false;
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
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));

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
			Bundle b = intent.getExtras();
			String action = intent.getAction();
			if (action.equals(TinyGDriver.STATUS)) {
				updateState(b);
			}
			if (action.equals(TinyGDriver.CONNECTION_STATUS)) {
				Log.d(TAG, "got connection_status intent");
				Button conn = ((Button) findViewById(R.id.connect));
				if (b.getBoolean("connection")) {
					conn.setText(R.string.disconnect);
					connected = true;
				} else {
					conn.setText(R.string.connect);
					connected = false;
				}
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
		outState.putBoolean("connected", connected);
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
		bindType = inState.getInt("bindType");
		connected = inState.getBoolean("connected");
	}

	public void myClickHandler(View view) {
		if (tinyg == null)
			return;
		switch (view.getId()) {
		case R.id.connect:
			if (connected) {
				tinyg.send_command(TinyGDriver.DISCONNECT);
			} else {
				tinyg.send_command(TinyGDriver.CONNECT);
			}
			break;
		}
		// If we're ready, handle buttons that will send messages to TinyG
		if (tinyg != null && connected) {
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
				((TextView) findViewById(R.id.jogval)).setText(Float.toString(jogRate));
				break;
			case R.id.rneg:
				jogRate -= 1;
				((TextView) findViewById(R.id.jogval)).setText(Float.toString(jogRate));
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

	public void updateState(Bundle b) {
		((TextView) findViewById(R.id.xloc)).setText(Float.toString(b.getFloat("posx")));
		((TextView) findViewById(R.id.yloc)).setText(Float.toString(b.getFloat("posy")));
		((TextView) findViewById(R.id.zloc)).setText(Float.toString(b.getFloat("posz")));
		((TextView) findViewById(R.id.aloc)).setText(Float.toString(b.getFloat("posa")));
		((TextView) findViewById(R.id.line)).setText(Integer.toString(b.getInt("line")));
		((TextView) findViewById(R.id.jogval)).setText(Float.toString(jogRate));
		((TextView) findViewById(R.id.momo)).setText(b.getString("momo"));
		((TextView) findViewById(R.id.status)).setText(b.getString("status"));
		((Button) findViewById(R.id.units)).setText(b.getString("units"));
		((TextView) findViewById(R.id.velocity)).setText(Float.toString(b.getFloat("velocity")));
	}
}