package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import java.io.File;

import org.csgeeks.TinyG.Support.*;

import com.google.android.apps.analytics.easytracking.TrackedFragmentActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends TrackedFragmentActivity implements MotorFragment.MotorFragmentListener, 
							ActionFragment.ActionFragmentListener, AxisFragment.AxisFragmentListener {
	private static final String TAG = "TinyG";
	private TinyGMessenger tinyg;
	private float jogRate = 10;
	private String filename;
	private int bindType = 0;
	private int axis_pick = 0, motor_pick = 0;
	private boolean connected = false;
	private ServiceConnection mConnection = new DriverServiceConnection();
	private PrefsListener mPreferencesListener;
	private Download mDownload;
	private BroadcastReceiver mIntentReceiver;
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NO_USB = 1;
	private static final int DIALOG_NO_SERVICE = 2;
	
	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter();
		updateFilter.addAction(TinyGDriver.AXIS_CONFIG);
		updateFilter.addAction(TinyGDriver.MOTOR_CONFIG);
		updateFilter.addAction(TinyGDriver.STATUS);
		updateFilter.addAction(TinyGDriver.CONNECTION_STATUS);
		updateFilter.addAction(TinyGDriver.THROTTLE);
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

		// Force landscape for now, since we don't really handle the loss of the
		// binding
		// (and subsequent destruction of the service) very well. Revisit later.
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.main);

		mConnection = new DriverServiceConnection();
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
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
			return bindService(new Intent(getApplicationContext(), TinyGNetwork.class), s,
					Context.BIND_AUTO_CREATE);
		case 1:
			// Check to see if the platform supports USB
			// Pop up a dialog if it doesn't.
			// If the binding fails, pop up a dialog with link
			// to service apk.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
				Toast.makeText(this, R.string.no_usb, Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			if (bindService(new Intent(TinyGDriver.USB_SERVICE), s,
					Context.BIND_AUTO_CREATE))
				return true;
			// TODO make this smarter - send us to the store to download the
			// driver
			Toast.makeText(this, R.string.no_service, Toast.LENGTH_LONG).show();
			tinyg = null;
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

	// This is how we get messages from the TinyG service. Two different message
	// types - a STATUS giving us
	// updates from an SR statement, and a CONNECTION_STATUS signal so that we
	// know if the service is connected
	// to the USB or network port.
	public class TinyGServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			String action = intent.getAction();
			if (action.equals(TinyGDriver.STATUS)) {
				StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusF);
				b.putFloat("jogRate", jogRate);
				sf.updateState(b);
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.displayF);
				if (f != null && f.getClass() == JogFragment.class)
					((JogFragment) f).updateState(b);
			}
			if (action.equals(TinyGDriver.MOTOR_CONFIG)) {
				Log.d(TAG, "Got MOTOR_CONFIG broadcast");
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.displayF);
				if (f != null && f.getClass() == MotorFragment.class)
					((MotorFragment) f).updateState(b);				
			}
			if (action.equals(TinyGDriver.AXIS_CONFIG)) {
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.displayF);
				if (f != null && f.getClass() == AxisFragment.class)
					((AxisFragment) f).updateState(b);			
			}			
			if (action.equals(TinyGDriver.CONNECTION_STATUS)) {
				Button conn = ((Button) findViewById(R.id.connect));
				if (b.getBoolean("connection")) {
					conn.setText(R.string.disconnect);
					connected = true;
				} else {
					conn.setText(R.string.connect);
					connected = false;
				}
			}
			if (action.equals(TinyGDriver.THROTTLE) && mDownload != null) {
				synchronized (mDownload.getSyncToken()) {
					mDownload.setThrottle(b.getBoolean("state"));
					Log.d(TAG, "Got [un]throttle signal");
					mDownload.getSyncToken().notify();
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
			if (mDownload != null)
				return true;
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
			builder.setMessage(R.string.about).setTitle(R.string.app_name);
			return builder.create();
		case DIALOG_NO_SERVICE:
			builder.setMessage(R.string.no_service).setTitle(R.string.app_name);
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
		outState.putString("filename", filename);
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
		bindType = inState.getInt("bindType");
		connected = inState.getBoolean("connected");
		filename = inState.getString("filename");
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
		case R.id.filepick:
			pickFile();
			break;
		}
		// If we're ready, handle buttons that will send messages to TinyG
		if (connected) {
			switch (view.getId()) {
			case R.id.start:
				// stop downloading
				if (mDownload != null) {
					mDownload.cancel();
					((Button) findViewById(R.id.start))
							.setText(R.string.start);
					// TODO Send interrupt
					mDownload = null;
				} else {
					mDownload = new Download(this, tinyg);
					EditText mFilename = (EditText) findViewById(R.id.filename);
					mDownload.openFile(mFilename.getText().toString());
				}
				break;
			case R.id.pause:
				break;
			case R.id.xpos:
				tinyg.short_jog("x", jogRate);
				break;
			case R.id.xneg:
				tinyg.short_jog("x", -jogRate);
				break;
			case R.id.ypos:
				tinyg.short_jog("y", jogRate);
				break;
			case R.id.yneg:
				tinyg.short_jog("y", -jogRate);
				break;
			case R.id.zpos:
				tinyg.short_jog("z", jogRate);
				break;
			case R.id.zneg:
				tinyg.short_jog("z", -jogRate);
				break;
			case R.id.apos:
				tinyg.short_jog("a", jogRate);
				break;
			case R.id.aneg:
				tinyg.short_jog("a", -jogRate);
				break;
			case R.id.rpos:
				jogRate += 1;
				((TextView) findViewById(R.id.jogval)).setText(Float
						.toString(jogRate));
				break;
			case R.id.rneg:
				jogRate -= 1;
				((TextView) findViewById(R.id.jogval)).setText(Float
						.toString(jogRate));
				break;
			case R.id.units:
				break;
			case R.id.zero:
				tinyg.send_gcode(Parser.CMD_ZERO_ALL_AXIS);
				break;
			}
		}
	}

	// We get a driver binding, and so we create a helper class that interacts
	// with the Messenger.
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
	private class PrefsListener implements
			SharedPreferences.OnSharedPreferenceChangeListener {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("tgfx_driver")) {
				Log.d(TAG, "Changing binding");
				bindType = Integer.parseInt(sharedPreferences.getString(
						"tgfx_driver", "0"));
				if (tinyg != null)
					unbindService(mConnection);
				if (bindDriver(mConnection) == false) {
					Toast.makeText(BaseActivity.this,
							"Binding service failed", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 1)
			return;
		if (resultCode == android.app.Activity.RESULT_OK && data != null) {
			String fileName = data.getData().getPath();
			if (fileName != null) {
				EditText mFilename = (EditText) findViewById(R.id.filename);
				if (mFilename != null)
					mFilename.setText(fileName);
			}
		}
	}

	private void pickFile() {
		EditText mFilename = (EditText) findViewById(R.id.filename);
		String fileName = mFilename.getText().toString();

		// TODO write our own
		Intent intent = new Intent("org.openintents.action.PICK_FILE");

		// Construct URI from file name.
		File file = new File(fileName);
		intent.setData(Uri.fromFile(file));

		try {
			startActivityForResult(intent, 1);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onMotorSelected(int m) {
		motor_pick = m;
		if (tinyg == null)
			return;
		Log.d(TAG, String.format("Sending GET_MOTOR intent %d", motor_pick));
		tinyg.send_command(TinyGDriver.GET_MOTOR, motor_pick);
	}

	public void onAxisSelected(int a) {
		axis_pick = a;
		if (tinyg == null)
			return;
		tinyg.send_command(TinyGDriver.GET_AXIS, axis_pick);
	}

	public boolean connectionState() {
		return connected;
	}
}
