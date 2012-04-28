package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;

import org.csgeeks.TinyG.driver.NetworkDriver;
import org.csgeeks.TinyG.driver.RetCode;
import org.csgeeks.TinyG.driver.TinygDriver;
import org.csgeeks.TinyG.driver.UsbDriver;

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
import android.widget.TextView;
import android.widget.Toast;

public class TinyGActivity extends Activity {
	private static final String TAG = "TinyG";

	private enum conntype {
		NET, USB
	};

	private conntype connectionType = conntype.NET;
	private TinygDriver tinyg;
	private Context mContext;
	private ListenerTask mListener;
	private View mDisconnect, mConnect;
	private Machine machine;
	private Double r = 10.0;
	private Double x, y, z = 0.0;
	private String netIP;
	private int netPort;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();
		mDisconnect = findViewById(R.id.disconnect);
		mConnect = findViewById(R.id.connect);
		machine = new Machine();
	    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
	    netIP = settings.getString("tgfx_hostname", "");
	    netPort = Integer.parseInt(settings.getString("tgfx_port","4444"));
	}

	@Override
    protected void onStop(){
       super.onStop();

	   SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
       SharedPreferences.Editor editor = settings.edit();
       editor.putString("tgfx_hostname", netIP);
       editor.putString("tgfx_port", Integer.toString(netPort));
       // Commit the edits!
       editor.commit();
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Settings");
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
        case 0:
            startActivity(new Intent(this, ShowSettingsActivity.class));
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.xpos:
			if (tinyg != null) {
				x += r;
				tinyg.write("{\"gc\": \"g0x" + x.toString() + "\"}\n");
			}
			break;
		case R.id.xneg:
			if (tinyg != null) {
				x -= r;
				tinyg.write("{\"gc\": \"g0x" + x.toString() + "\"}\n");
			}
			break;
		case R.id.ypos:
			if (tinyg != null) {
				y += r;
				tinyg.write("{\"gc\": \"g0y" + y.toString() + "\"}\n");
			}
			break;
		case R.id.yneg:
			if (tinyg != null) {
				y -= r;
				tinyg.write("{\"gc\": \"g0y" + y.toString() + "\"}\n");
			}
			break;
		case R.id.zpos:
			if (tinyg != null) {
				z += r;
				tinyg.write("{\"gc\": \"g0z" + z.toString() + "\"}\n");
			}
			break;
		case R.id.zneg:
			if (tinyg != null) {
				z -= r;
				tinyg.write("{\"gc\": \"g0z" + z.toString() + "\"}\n");
			}
			break;
		case R.id.rpos:
			r += 1;
			break;
		case R.id.rneg:
			r -= 1;
			break;
		case R.id.zero:
			tinyg.write(Machine.CMD_ZERO_ALL_AXIS);
			break;
		case R.id.connect:
			switch (connectionType) {
			case NET:
				if (tinyg == null) {
					tinyg = new NetworkDriver(netIP, netPort);
				} else {
					tinyg.disconnect();
				}
				new ConnectTask().execute(0);
				Toast.makeText(mContext, "Connecting...", Toast.LENGTH_SHORT)
						.show();
				view.setVisibility(View.INVISIBLE);
				break;
			case USB:
				if (tinyg == null) {
					tinyg = new UsbDriver("path");
				} else {
					tinyg.disconnect();
				}
				tinyg.connect();
				view.setVisibility(View.INVISIBLE);
				break;
			}
			break;
		case R.id.disconnect:
			if (tinyg != null) {
				Log.d(TAG, "telling listener to cancel");
				mListener.cancel(true);
				Log.d(TAG, "sending disconnect command to tinyg");
				tinyg.disconnect();
				Log.d(TAG, "done with disconnect");
			}
			view.setVisibility(View.INVISIBLE);
		}
	}

	private class ConnectTask extends AsyncTask<Integer, Integer, RetCode> {
		@Override
		protected RetCode doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect to " + netIP + " in background");
			RetCode res = tinyg.connect();
			Log.d(TAG, "Returned from connect");
			return res;
		}

		protected void onPostExecute(RetCode res) {
			if (res.result) {
				Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT)
						.show();

				mDisconnect.setVisibility(View.VISIBLE);
				mListener = new ListenerTask();
				mListener.execute(new InputStream[] { tinyg.getInputStream() });
				Log.i(TAG, "Listener started");
				tinyg.write(Machine.CMD_DISABLE_LOCAL_ECHO);
				tinyg.write(Machine.CMD_GET_OK_PROMPT);
				tinyg.write(Machine.CMD_SET_STATUS_UPDATE_INTERVAL);
				tinyg.write(Machine.CMD_GET_OK_PROMPT);
				tinyg.write(Machine.CMD_GET_STATUS_REPORT);
				tinyg.write(Machine.CMD_GET_OK_PROMPT);
			} else {
				CharSequence c = res.message;
				Toast.makeText(mContext, c, Toast.LENGTH_SHORT).show();
				mConnect.setVisibility(View.VISIBLE);
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
				if (machine.processJSON(values[0])) {
					x = machine.getX();
					y = machine.getY();
					z = machine.getZ();
					((TextView) findViewById(R.id.xloc)).setText(Double
							.toString(x));
					((TextView) findViewById(R.id.yloc)).setText(Double
							.toString(y));
					((TextView) findViewById(R.id.zloc)).setText(Double
							.toString(z));
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
			mConnect.setVisibility(View.VISIBLE);
		}
	}
}