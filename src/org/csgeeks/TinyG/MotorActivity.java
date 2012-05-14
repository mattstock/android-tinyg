package org.csgeeks.TinyG;

import org.csgeeks.TinyG.Support.*;
import org.csgeeks.TinyG.TinyGActivity.TinyGServiceReceiver;

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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

public class MotorActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
	private TinyGMessenger tinyg;
	private ServiceConnection mConnection;
	private BroadcastReceiver mIntentReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motor);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mConnection = new DriverServiceConnection();

		if (bindService(new Intent(this, TinyGDriver.class), mConnection,
				Context.BIND_AUTO_CREATE)) {
		} else {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}

		// configure motor picker
		Spinner s = (Spinner) findViewById(R.id.motorpick);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.motorArray, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new MotorItemSelectedListener());

		// Configure axis picker
		s = (Spinner) findViewById(R.id.map_axis);
		adapter = ArrayAdapter.createFromResource(this, R.array.axisArray,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new AxisItemSelectedListener());
	}

	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter();
		updateFilter.addAction(TinyGDriver.MOTOR_1_STATUS);
		updateFilter.addAction(TinyGDriver.MOTOR_2_STATUS);
		updateFilter.addAction(TinyGDriver.MOTOR_3_STATUS);
		updateFilter.addAction(TinyGDriver.MOTOR_4_STATUS);
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

	private class TinyGServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TinyGDriver.MOTOR_1_STATUS)) {
				// TODO pull data out, update fields
			}
			if (action.equals(TinyGDriver.MOTOR_2_STATUS)) {
				// TODO pull data out, update fields
			}
			if (action.equals(TinyGDriver.MOTOR_3_STATUS)) {
				// TODO pull data out, update fields
			}
			if (action.equals(TinyGDriver.MOTOR_4_STATUS)) {
				// TODO pull data out, update fields
			}
		}
	}

	// TODO
	private class MotorItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			if (tinyg != null) {
				Log.i(TAG, "setting values in motor activity");
				Motor m = tinyg.getMachine().getMotorByNumber(pos+1);
				((Spinner) findViewById(R.id.map_axis)).setSelection(m
						.getMapToAxis());
				((EditText) findViewById(R.id.step_angle)).setText(Float
						.toString(m.getStep_angle()));
				((EditText) findViewById(R.id.travel_rev)).setText(Float
						.toString(m.getTravel_per_revolution()));
				((EditText) findViewById(R.id.microsteps)).setText(Integer
						.toString(m.getMicrosteps()));
				((ToggleButton) findViewById(R.id.polarity)).setChecked(m
						.isPolarity());
				((ToggleButton) findViewById(R.id.power_management))
						.setChecked(m.isPower_management());
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	// TODO
	private class AxisItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			Toast.makeText(
					parent.getContext(),
					"The planet is " + parent.getItemAtPosition(pos).toString(),
					Toast.LENGTH_LONG).show();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void myClickHandler(View view) {
		// Just in case something happened, though it seems like this shouldn't
		// be possible.
		if (tinyg == null) {
			if (bindService(new Intent(this, TinyGNetwork.class), mConnection,
					Context.BIND_AUTO_CREATE)) {
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

	private class DriverServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = new TinyGMessenger(new Messenger(service));
		}

		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}
}
