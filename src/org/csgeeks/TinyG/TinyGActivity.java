package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.csgeeks.TinyG.Support.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
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

public class TinyGActivity extends FragmentActivity implements MotorFragment.MotorFragmentListener {
	private static final String TAG = "TinyG";
	private TinyGMessenger tinyg;
	private float jogRate = 10;
	private String filename;
	private int bindType = 0;
	private boolean connected = false, download_mode = false;
	private boolean throttle;
	private ServiceConnection mConnection;
	private PrefsListener mPreferencesListener;
	private EditText mFilename;
	private BroadcastReceiver mIntentReceiver;
	private ProgressDialog progressDialog;
	private FileWriteTask mFileTask;
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NO_USB = 1;
	private static final int DIALOG_NO_SERVICE = 2;
	private static final int DIALOG_DOWNLOAD = 3;
	private int numLines;
	final private Object synctoken = new Object();
	
	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter();
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
		setContentView(R.layout.main);

		// Force landscape for now, since we don't really handle the loss of the
		// binding
		// (and subsequent destruction of the service) very well. Revisit later.
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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

		((TextView) findViewById(R.id.jogval)).setText(Float
				.toString(jogRate));

		throttle = false;
		
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
			return bindService(new Intent(this, TinyGNetwork.class), s,
					Context.BIND_AUTO_CREATE);
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
			if (bindService(new Intent(TinyGDriver.USB_SERVICE), s,
					Context.BIND_AUTO_CREATE))
				return true;
			// TODO make this smarter - send us to the store to download the
			// driver
			Toast.makeText(this, R.string.no_service, Toast.LENGTH_LONG).show();
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
				sf.updateState(b);
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.displayF);
				if (f != null && f.getClass() == JogFragment.class)
					((JogFragment) f).updateState(b);
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
			if (action.equals(TinyGDriver.THROTTLE)) {
				synchronized (synctoken) {
					throttle = b.getBoolean("state");
					Log.d(TAG, "Got [un]throttle signal");
					synctoken.notify();
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
			if (download_mode)
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
		case DIALOG_DOWNLOAD:
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Transferring...");
			return progressDialog;
		}
		return null;
	}

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case DIALOG_DOWNLOAD:
            progressDialog.setProgress(0);
        }
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat("jogRate", jogRate);
		outState.putInt("bindType", bindType);
		outState.putBoolean("connected", connected);
		outState.putBoolean("download_mode", download_mode);
		outState.putString("filename", filename);
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
		bindType = inState.getInt("bindType");
		connected = inState.getBoolean("connected");
		download_mode = inState.getBoolean("download_mode");
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
				if (download_mode) {
					// stop downloading
					if (mFileTask != null) {
						mFileTask.cancel(true);
						((Button) findViewById(R.id.start))
								.setText(R.string.start);
						// TODO Send interrupt
						download_mode = false;
					}
				} else {
					showDialog(DIALOG_DOWNLOAD);
					openFile(mFilename.getText().toString());
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
				unbindService(mConnection);
				if (bindDriver(mConnection) == false) {
					Toast.makeText(TinyGActivity.this,
							"Binding service failed", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}


	// Given a filename, start up the file writer task and provide status on
	// progress dialog
	private void openFile(String string) {
		// Count lines
		try {
			BufferedReader in = new BufferedReader(new FileReader(string));
			numLines = 0;
			while (in.readLine() != null) {
				numLines++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Invalid filename", Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			Toast.makeText(this, "Gcode file read error", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.d(TAG, "lines = " + numLines);
		
		// Now send them!
		try {
			BufferedReader in = new BufferedReader(new FileReader(string));
			mFileTask = new FileWriteTask();
			mFileTask.execute(in);
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Invalid filename", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	protected class FileWriteTask extends AsyncTask<BufferedReader, String, Void> {
		@Override
		protected Void doInBackground(BufferedReader... params) {
			BufferedReader in = params[0];
			String line;
			int idx = 0;
			
			try {
				while (!isCancelled() && (line = in.readLine()) != null) {
					idx++;
					try {
						synchronized (synctoken) {
							if (throttle) 
								synctoken.wait();
						}
					} catch (InterruptedException e) {
						// This is probably ok, just proceed.
					}				
					publishProgress(line, Integer.toString(idx));
				}
				in.close();
			} catch (IOException e) {
				Log.e(TAG, "error writing file: " + e.getMessage());
			} 
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if (values.length > 1) {
				progressDialog.setProgress((int)(100*(Double.parseDouble(values[1])/numLines)));
				tinyg.send_gcode(values[0] + "\n");
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "FileWriteTask cancelled");
			// TODO add a hard stop instruction to TinyG
			dismissDialog(DIALOG_DOWNLOAD);
			Toast.makeText(TinyGActivity.this, "Download cancelled", Toast.LENGTH_SHORT).show();			
		}
		
	    protected void onPostExecute() {
	    	Log.i(TAG, "FileWriteTask complete");
	        dismissDialog(DIALOG_DOWNLOAD);
	    }

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 1)
			return;
		if (resultCode == android.app.Activity.RESULT_OK && data != null) {
			String fileName = data.getData().getPath();
			if (fileName != null)
				mFilename.setText(fileName);
		}
	}

	private void pickFile() {
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
		if (tinyg == null)
			return;
		tinyg.send_command(TinyGDriver.GET_MOTOR, m);
	}

	public void onAxisSelected(int a) {
		// TODO Auto-generated method stub	
	}
}