package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;

import org.csgeeks.TinyG.driver.NetworkDriver;
import org.csgeeks.TinyG.driver.RetCode;
import org.csgeeks.TinyG.driver.TinyGDriver;
import org.csgeeks.TinyG.system.Machine;
import org.csgeeks.TinyG.system.Machine.unit_modes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TinyGActivity extends Activity {
	private static final String TAG = "TinyG";

	private TinyGDriver tinyg;
	private Context mContext;
	private ListenerTask mListener;
	private View mConnect;
	private float jogRate = 10;
	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jog);
		mContext = getApplicationContext();
		mConnect = findViewById(R.id.connect);
		settings = PreferenceManager.getDefaultSharedPreferences(mContext);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.connect:
			if (mListener != null) {
				Log.d(TAG, "telling listener to cancel");
				mListener.cancel(true);
				mListener = null;
			}
			if (tinyg != null) {
				Log.d(TAG, "sending disconnect command to tinyg");
				tinyg.disconnect();
				tinyg = null;
				((Button) view).setText(R.string.connect);
			} else {
				tinyg = new NetworkDriver(settings.getString("tgfx_hostname",
						"127.0.0.1"), Integer.parseInt(settings.getString(
						"tgfx_port", "4444")));
				new ConnectTask().execute(0);
				Toast.makeText(mContext, "Connecting...", Toast.LENGTH_SHORT)
						.show();
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

	private class ConnectTask extends AsyncTask<Integer, Integer, RetCode> {
		@Override
		protected RetCode doInBackground(Integer... params) {
			Log.d(TAG,
					"Starting connect to "
							+ settings.getString("tgfx_hostname", "127.0.0.1")
							+ " in background");
			RetCode res = tinyg.connect();
			Log.d(TAG, "Returned from connect");
			return res;
		}

		protected void onPostExecute(RetCode res) {
			if (res.result) {
				Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT)
						.show();

				mListener = new ListenerTask();
				mListener.execute(new InputStream[] { tinyg.getInputStream() });
				Log.i(TAG, "Listener started");
				tinyg.write(TinyGDriver.CMD_DISABLE_LOCAL_ECHO);
				tinyg.write(TinyGDriver.CMD_GET_OK_PROMPT);
				tinyg.write(TinyGDriver.CMD_SET_STATUS_UPDATE_INTERVAL);
				tinyg.write(TinyGDriver.CMD_GET_OK_PROMPT);
				tinyg.write(TinyGDriver.CMD_GET_STATUS_REPORT);
				tinyg.write(TinyGDriver.CMD_GET_OK_PROMPT);
			} else {
				CharSequence c = res.message;
				Toast.makeText(mContext, c, Toast.LENGTH_SHORT).show();
				tinyg = null;
			}
		}
	}

	private class ListenerTask extends AsyncTask<InputStream, String, Void> {

		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] buffer = new byte[1024];
			InputStream is = params[0];
			int b;
			int idx = 0;
			try {
				while (!isCancelled()) {
					if ((b = is.read()) == -1) {
						break;
					}
					buffer[idx++] = (byte) b;
					if (b == '\n') {
						publishProgress(new String(buffer, 0, idx));
						idx = 0;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "listener read: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if (values.length > 0) {
				Log.i(TAG, "onProgressUpdate: " + values[0].length()
						+ " bytes received.");
				if (tinyg.processJSON(values[0])) {
					updateState(tinyg.getMachine());
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
			mConnect.setVisibility(View.VISIBLE);
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
		((TextView) findViewById(R.id.line)).setText(Integer.toString(machine.getLine_number()));
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
		((TextView) findViewById(R.id.velocity)).setText(Float.toString(machine.getVelocity()));
	}
}