package org.csgeeks.TinyG;

import org.csgeeks.TinyG.system.Motor;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MotorActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
	private TinyGDriver tinyg;
	private ServiceConnection mConnection;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motor);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mConnection = new NetworkServiceConnection();

		if (bindService(new Intent(this, TinyGDriver.class),
				mConnection, Context.BIND_AUTO_CREATE)) {
		} else {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
		
		Spinner s = (Spinner) findViewById(R.id.motorpick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.axisArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}

	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	      Toast.makeText(parent.getContext(), "The planet is " +
	          parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
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
			break;
		}
	}
	
	private class NetworkServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = ((TinyGDriver.NetworkBinder) service).getService();
			Log.i(TAG,"got binder");
			if (tinyg.isReady()) {
				Log.i(TAG,"setting values in motor activity");
				Motor one = tinyg.getMachine().getMotorByNumber(1);
				((EditText) findViewById(R.id.step_angle)).setText(Float.toString(one.getStep_angle()));
			}
		}
		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}
}
