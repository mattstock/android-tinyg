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
	private PrefsListener mPreferencesListener;
	private BroadcastReceiver mIntentReceiver;
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NO_USB = 1;
	private static final int DIALOG_NO_SERVICE = 2;
	
	@Override
	public void onResume() {
		// I think we could register these in the manifest, but this seems to be the
		// standard for BroadcastRecievers.
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
		
		// Force landscape for now, since we don't really handle the loss of the binding
		// (and subsequent destruction of the service) very well.  Revisit later.
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		mConnection = new DriverServiceConnection();
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));

		mPreferencesListener = new PrefsListener();
		settings.registerOnSharedPreferenceChangeListener(mPreferencesListener);
		
		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}
		
		// Do the initial service binding
		if (bindDriver(mConnection) == false) { 
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	// Allows us to switch binding methods Network vs USB.
	// Could add additional options like bluetooth in the future.
	private boolean bindDriver(ServiceConnection s) {
		switch (bindType) {
		case 0:
			return bindService(new Intent(this, TinyGNetwork.class),
					s, Context.BIND_AUTO_CREATE);
		case 1:
			// Check to see if the platform supports USB
			// Pop up a dialog if it doesn't.
			// If the binding fails, pop up a dialog with link
			// to service apk.
			if (android.os.Build.VERSION.SDK_INT < 12) {
				Toast.makeText(this, R.string.no_usb, Toast.LENGTH_SHORT)
					.show();
				return false;
			}
			if (bindService(new Intent(TinyGDriver.USB_SERVICE),
					s, Context.BIND_AUTO_CREATE))
				return true;
			Toast.makeText(this, R.string.no_service, Toast.LENGTH_LONG)
				.show();
			return false;
		default:
			return false;
		}
	}

	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	// This is how we get messages from the TinyG service.  Two different message types - a STATUS giving us
	// updates from an SR statement, and a CONNECTION_STATUS signal so that we know if the service is connected
	// to the USB or network port.
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
			if (connected) {
				tinyg.send_command(TinyGDriver.REFRESH);
			} else {
				Toast.makeText(this, "Not connected!", Toast.LENGTH_SHORT)
				.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public Dialog onCreateDialog(int arg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (arg) {
		case DIALOG_ABOUT:
			builder.setMessage(R.string.about)
				.setTitle(R.string.app_name);
			return builder.create();
		case DIALOG_NO_SERVICE:
			builder.setMessage(R.string.no_service)
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
			case R.id.filepick:
				Log.d(TAG, "ugly!");
				break;
			}
		}
	}

	// We get a driver binding, and so we create a helper class that interacts with the Messenger.
	// We can probably redo this as a subclass.
	private class DriverServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = new TinyGMessenger(new Messenger(service));
		}

		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}

	// Make sure we rebind services if we change the preference.
	private class PrefsListener implements SharedPreferences.OnSharedPreferenceChangeListener {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("tgfx_driver")) {
				Log.d(TAG, "Changing binding");
				bindType = Integer.parseInt(sharedPreferences.getString("tgfx_driver", "0"));
				unbindService(mConnection);
				if (bindDriver(mConnection) == false) { 
					Toast.makeText(TinyGActivity.this, "Binding service failed", Toast.LENGTH_SHORT)
						.show();
				}
			}
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