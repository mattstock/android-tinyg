package org.csgeeks.TinyG;

import org.csgeeks.TinyG.Support.Parser;
import org.csgeeks.TinyG.Support.TinyGDriver;
import org.csgeeks.TinyG.Support.TinyGMessenger;
import org.csgeeks.TinyG.Support.TinyGNetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class JogActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
	private TinyGMessenger tinyg;
	private ServiceConnection mConnection;
	private BroadcastReceiver mIntentReceiver;
	private int bindType = 0;
	private float jogRate = 10;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jog_activity);
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		bindType = Integer.parseInt(settings.getString("tgfx_driver", "0"));

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}

		((TextView) findViewById(R.id.jogval)).setText(Float
				.toString(jogRate));

		if (bindDriver(mConnection) == false) {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private boolean bindDriver(ServiceConnection s) {
		switch (bindType) {
		case 0:
			return bindService(new Intent(this, TinyGNetwork.class), s,
					Context.BIND_AUTO_CREATE);
		case 1:
			return bindService(new Intent(TinyGDriver.USB_SERVICE), s,
					Context.BIND_AUTO_CREATE);
		default:
			return false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("bindType", bindType);
		outState.putFloat("jogRate", jogRate);
	}

	private void restoreState(Bundle inState) {
		bindType = inState.getInt("bindType");
		jogRate = inState.getFloat("jogRate");
		((TextView) findViewById(R.id.jogval)).setText(Float
				.toString(jogRate));
	}

	@Override
	public void onResume() {
		IntentFilter updateFilter = new IntentFilter(TinyGDriver.STATUS);
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
			Bundle b = intent.getExtras();
			if (action.equals(TinyGDriver.STATUS)) {
				StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusF);
				sf.updateState(b);
				JogFragment jf = (JogFragment) getSupportFragmentManager().findFragmentById(R.id.jogF);
				jf.updateState(b);
			}
		}
	}

	public void myClickHandler(View view) {
		if (tinyg == null)
			return;
		switch (view.getId()) {
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
