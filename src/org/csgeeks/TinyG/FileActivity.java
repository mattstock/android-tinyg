package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import java.io.File;

import org.csgeeks.TinyG.Support.*;

import com.google.android.apps.analytics.easytracking.TrackedFragmentActivity;

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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FileActivity extends TrackedFragmentActivity {
	private static final String TAG = "TinyG";
	private BroadcastReceiver mIntentReceiver;
	private int bindType = 0;
	private boolean connected;
	private ServiceConnection mConnection;
	private TinyGMessenger tinyg;
	private Download mDownload;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_activity);

		// Force landscape for now, since we don't really handle the loss of the
		// binding
		// (and subsequent destruction of the service) very well. Revisit later.
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		connected = getIntent().getBooleanExtra("connection", false);
		
		mConnection = new DriverServiceConnection();
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));
		
		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}

		// Do the initial service binding
		if (bindDriver(mConnection) == false) {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("bindType", bindType);
	}

	private void restoreState(Bundle inState) {
		bindType = inState.getInt("bindType");
	}

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
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
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

	public void myClickHandler(View view) {
		if (tinyg == null)
			return;
		switch (view.getId()) {
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
					EditText mFilename = (EditText) findViewById(R.id.filename);
					mDownload = new Download(this, tinyg);
					mDownload.openFile(mFilename.getText().toString());
				}
				break;
			}
		}
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
			}
			if (action.equals(TinyGDriver.CONNECTION_STATUS))
				connected = b.getBoolean("connection");
			if (action.equals(TinyGDriver.THROTTLE) && mDownload != null) {
				synchronized (mDownload.getSyncToken()) {
					mDownload.setThrottle(b.getBoolean("state"));
					Log.d(TAG, "Got [un]throttle signal");
					mDownload.getSyncToken().notify();
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
}
