package org.csgeeks.TinyG;

import org.csgeeks.TinyG.system.Machine;
import org.csgeeks.TinyG.system.Machine.unit_modes;

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
	private TinyGDriver tinyg;
	private float jogRate = 10;
	private ServiceConnection mConnection;
	private BroadcastReceiver mIntentReceiver;
	private static final int DIALOG_ABOUT = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mConnection = new NetworkServiceConnection();

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}
		if (bindService(new Intent(this, TinyGDriver.class),
				mConnection, Context.BIND_AUTO_CREATE)) {
		} else {
			Toast.makeText(this, "Binding service failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onResume() {
		IntentFilter updateFilter;
		updateFilter = new IntentFilter(TinyGDriver.TINYG_UPDATE);
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

	public class TinyGServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateState(tinyg.getMachine());
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
			tinyg.refresh();
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
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
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
		case R.id.connect:
			if (tinyg.isReady()) {
				tinyg.disconnect();
				((Button) view).setText(R.string.connect);
			} else {
				tinyg.connect();
				((Button) view).setText(R.string.disconnect);
			}
			break;
		}
		// If we're ready, handle buttons that will send messages to TinyG
		if (tinyg != null && tinyg.isReady()) {
			switch (view.getId()) {
			case R.id.xpos:
				tinyg.write("{\"gc\": \"g91g0x" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.xneg:
				tinyg.write("{\"gc\": \"g91g0x" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.ypos:
				tinyg.write("{\"gc\": \"g91g0y" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.yneg:
				tinyg.write("{\"gc\": \"g91g0y" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.zpos:
				tinyg.write("{\"gc\": \"g91g0z" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.zneg:
				tinyg.write("{\"gc\": \"g91g0z" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.apos:
				tinyg.write("{\"gc\": \"g91g0a" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.aneg:
				tinyg.write("{\"gc\": \"g91g0a" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.rpos:
				jogRate += 1;
				break;
			case R.id.rneg:
				jogRate -= 1;
				break;
			case R.id.units:
				switch (tinyg.getMachine().getUnitMode()) {
				case MM:
					tinyg.write(TinyGDriver.CMD_SET_UNIT_INCHES);
					// A hack
					tinyg.getMachine().setUnits(unit_modes.INCHES);
					break;
				case INCHES:
					tinyg.write(TinyGDriver.CMD_SET_UNIT_MM);
					// A hack
					tinyg.getMachine().setUnits(unit_modes.MM);
					break;
				}
				break;
			case R.id.zero:
				tinyg.write(TinyGDriver.CMD_ZERO_ALL_AXIS);
				// This is a bit of a hack.
				Machine machine = tinyg.getMachine();
				machine.getAxisByName("X").setWork_position(0);
				machine.getAxisByName("Y").setWork_position(0);
				machine.getAxisByName("Z").setWork_position(0);
				machine.getAxisByName("A").setWork_position(0);
				break;
			}
			updateState(tinyg.getMachine());
		}
	}

	private class NetworkServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			tinyg = ((TinyGDriver.NetworkBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			tinyg = null;
		}
	}

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