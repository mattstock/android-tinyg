package org.csgeeks.TinyG;

import org.csgeeks.TinyG.system.Machine;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MachineActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
	private TinyGDriver tinyg;
	private ServiceConnection mConnection;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.machine);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mConnection = new NetworkServiceConnection();

		if (bindService(new Intent(this, TinyGDriver.class),
				mConnection, Context.BIND_AUTO_CREATE)) {
		} else {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	public void myClickHandler(View view) {
		// Just in case something happened, though it seems like this shouldn't
		// be possible.
		if (tinyg == null) {
			if (bindService(new Intent(this, TinyGDriver.class),
					mConnection, Context.BIND_AUTO_CREATE)) {
			} else {
				Toast.makeText(this, "Binding service failed",
						Toast.LENGTH_SHORT).show();
			}
			return;
		}
		switch (view.getId()) {
		case R.id.save:
			// TODO
			break;
		}
	}
	
	private class NetworkServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = ((TinyGDriver.NetworkBinder) service).getService();
			Log.i(TAG,"got binder");
			if (tinyg.isReady()) {
				Log.i(TAG,"setting values in machine activity");
				Machine m = tinyg.getMachine();
				((TextView) findViewById(R.id.firmware_build)).setText(Float.toString(m.getFirmware_version()));
				((TextView) findViewById(R.id.firmware_version)).setText(Float.toString(m.getFirmware_build()));
				((EditText) findViewById(R.id.system_interval)).setText(Integer.toString(m.getStatus_report_interval()));
			}
		}
		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}
}
