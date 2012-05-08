package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.csgeeks.TinyG.R;
import org.csgeeks.TinyG.system.Axis;
import org.csgeeks.TinyG.system.Machine;
import org.csgeeks.TinyG.system.Motor;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TinyGDriver extends Service {
	private Machine machine;
	private ListenerTask mListener;
	private String tgfx_hostname;
	private int tgfx_port;
	private boolean ready;
	private InputStream is;
	private OutputStream os;
	private Socket socket;

	private static final String TAG = "TinyG";
	public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
	public static final String CMD_GET_STATUS_REPORT = "{\"sr\":null}\n";
	public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
	public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
	public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":150}\n";
	public static final String CMD_GET_MACHINE_SETTINGS = "{\"sys\":null}\n";
	public static final String CMD_SET_UNIT_MM = "{\"gc\":\"g21\"}\n";
	public static final String CMD_SET_UNIT_INCHES = "{\"gc\":\"g20\"}\n";
	public static final String CMD_GET_X_AXIS = "{\"x\":null}\n";
	public static final String CMD_GET_Y_AXIS = "{\"y\":null}\n";
	public static final String CMD_GET_Z_AXIS = "{\"z\":null}\n";
	public static final String CMD_GET_A_AXIS = "{\"a\":null}\n";
	public static final String CMD_GET_B_AXIS = "{\"b\":null}\n";
	public static final String CMD_GET_C_AXIS = "{\"c\":null}\n";
	public static final String CMD_GET_MOTOR_1_SETTINGS = "{\"1\":null}\n";
	public static final String CMD_GET_MOTOR_2_SETTINGS = "{\"2\":null}\n";
	public static final String CMD_GET_MOTOR_3_SETTINGS = "{\"3\":null}\n";
	public static final String CMD_GET_MOTOR_4_SETTINGS = "{\"4\":null}\n";
	public static final String TINYG_UPDATE = "org.csgeeks.TinyG.UPDATE";
	private static final String STATUS_REPORT = "{\"sr\":{";
	private static final String CMD_PAUSE = "!\n";
	private static final String CMD_RESUME = "~\n";

    public class NetworkBinder extends Binder {
        public TinyGDriver getService() {
            return TinyGDriver.this;
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
		machine = new Machine();
		ready = false;
    }

    @Override
    public void onDestroy() {
        disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new NetworkBinder();

	public Machine getMachine() {
		return machine;
	}

	public boolean processJSON(String string) {
		try {
			JSONObject json = new JSONObject(string);
			if (json.has("sr")) {
				Log.i(TAG,"Got sr JSON");
				JSONObject sr = json.getJSONObject("sr");
				// We should do this based on the machine configuration.
				// Hardcoded for now.
				machine.getAxisByName("X").setWork_position(
						(float) sr.getDouble("posx"));
				machine.getAxisByName("Y").setWork_position(
						(float) sr.getDouble("posy"));
				machine.getAxisByName("Z").setWork_position(
						(float) sr.getDouble("posz"));
				machine.getAxisByName("A").setWork_position(
						(float) sr.getDouble("posa"));
				machine.setLine_number(sr.getInt("line"));
				machine.setVelocity((float) sr.getDouble("vel"));
				machine.setUnits(sr.getInt("unit"));
				machine.setMotionMode(sr.getInt("momo"));
				machine.setMachineState(sr.getInt("stat"));
				return true;
			}
			if (json.has("gc")) {
				JSONObject gc = json.getJSONObject("gc");
				String gcode = gc.getString("gc");
				if (gc.getString("msg").equals("OK")) {
					if (gcode.regionMatches(0, "G20", 0, 3)) {
						// Hack
						Log.i(TAG, "Got confirmation of unit change to inches");

					}
					if (gcode.regionMatches(0, "G21", 0, 3)) {
						// Hack
						Log.i(TAG, "Got confirmation of unit change to mm");

					}
					if (gcode.regionMatches(0, "G92", 0, 3)) {
						// Should set the local screen value based on this.
						// Instead, I'm going to cheat
						// and set it when we send the zero command...
						Log.i(TAG, "Got confirmation of zeroing");
					}
				}
			}
			if (json.has("sys")) {
				Log.i(TAG,"Got sys JSON");
				JSONObject sys = json.getJSONObject("sys");
				machine.setFirmware_build((float) sys.getDouble("fb"));
				machine.setFirmware_version((float) sys.getDouble("fv"));
				machine.setStatus_report_interval(sys.getInt("si"));
				// More later if we want them.
			}
			if (json.has("1")) {
				Log.i(TAG,"Got motor 1 JSON");
				JSONObject motor = json.getJSONObject("1");
				parseMotor(1, motor);
			}
			if (json.has("2")) {
				Log.i(TAG,"Got motor 2 JSON");
				JSONObject motor = json.getJSONObject("2");
				parseMotor(2, motor);
			}
			if (json.has("3")) {
				Log.i(TAG,"Got motor 3 JSON");
				JSONObject motor = json.getJSONObject("3");
				parseMotor(3, motor);
			}
			if (json.has("4")) {
				Log.i(TAG,"Got motor 4 JSON");
				JSONObject motor = json.getJSONObject("4");
				parseMotor(4, motor);
			}
			if (json.has("a")) {
				Log.i(TAG,"Got axis A JSON");
				JSONObject axis = json.getJSONObject("a");
				parseAxis("A", axis);
			}
			if (json.has("b")) {
				Log.i(TAG,"Got axis B JSON");
				JSONObject axis = json.getJSONObject("b");
				parseAxis("B", axis);
			}
			if (json.has("c")) {
				Log.i(TAG,"Got axis C JSON");
				JSONObject axis = json.getJSONObject("c");
				parseAxis("C", axis);
			}
			if (json.has("x")) {
				Log.i(TAG,"Got axis X JSON");
				JSONObject axis = json.getJSONObject("x");
				parseAxis("X", axis);
			}
			if (json.has("y")) {
				Log.i(TAG,"Got axis Y JSON");
				JSONObject axis = json.getJSONObject("y");
				parseAxis("Y", axis);
			}
			if (json.has("z")) {
				Log.i(TAG,"Got axis Z JSON");
				JSONObject axis = json.getJSONObject("z");
				parseAxis("Z", axis);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Received malformed JSON: " + string + ": " + e.getMessage());
		}
		return false;
	}

	private void parseAxis(String string, JSONObject axis) {
		try {
			Axis a = machine.getAxisByName(string);
			a.setTravel_maximum((float) axis.getDouble("tm"));
			a.setVelocity_maximum((float) axis.getDouble("vm"));
			a.setJerk_maximum((float) axis.getDouble("jm"));
			a.setJunction_devation((float) axis.getDouble("jd"));
			a.setSwitch_mode(axis.getInt("sm"));
		} catch (JSONException e) {
			Log.e(TAG, "Received malformed JSON in parseAxis: " + e.getMessage());
		}
	}

	private void parseMotor(int i, JSONObject motor) {
		try {
			Motor m = machine.getMotorByNumber(i);
			m.setMapToAxis(motor.getInt("ma"));
			m.setStep_angle((float) motor.getDouble("sa"));
			m.setTravel_per_revolution((float) motor.getDouble("tr"));
			m.setMicrosteps(motor.getInt("mi"));
			m.setPolarity(motor.getInt("po") == 1);
			m.setPower_management(motor.getInt("pm") == 1);
		} catch (JSONException e) {
			Log.e(TAG, "Received malformed JSON in parseMotor: " + e.getMessage());
		}
	}

	public boolean isReady() {
		return ready;
	}

	public void disconnect() {
		if (mListener != null) {
			mListener.cancel(true);	
		}
		if (socket != null) {
			try {
				Log.d(TAG,"closing socket");
				socket.close();
				is.close();
				os.close();
			} catch (IOException e) {
				Log.e(TAG, "Close: " + e.getMessage());
			}
			is = null;
			os = null;
			socket = null;
			ready = false;
		}
		Log.d(TAG,"disconnect done");
	}

	public void write(String message) {
		try {
			os.write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "write: " + e.getMessage());
		}
	}

	public void connect() {
		if (ready) {
			return;
		}
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));
		
		new ConnectTask().execute(0);
		Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
	}
	
	private class ConnectTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect to " + tgfx_hostname + " in background");
			try {
				socket = new Socket(tgfx_hostname, tgfx_port);
				os = socket.getOutputStream();
				is = socket.getInputStream();
			} catch (UnknownHostException e) {
				socket = null;
				Log.e(TAG,"Socket: " + e.getMessage());
				Toast.makeText(TinyGDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				return false;
			} catch (IOException e) {
				socket = null;
				Log.e(TAG,"Socket: " + e.getMessage());
				Toast.makeText(TinyGDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean res) {
			if (res) {
				Toast.makeText(TinyGDriver.this, "Connected", Toast.LENGTH_SHORT)
						.show();
				mListener = new ListenerTask();
				mListener.execute(new InputStream[] { is });
				ready = true;
				Log.i(TAG, "Listener started");
				refresh();
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
				Log.i(TAG, "onProgressUpdate: " + values[0]);
				if (processJSON(values[0])) {
					Intent intent = new Intent(TINYG_UPDATE);
					sendBroadcast(intent);
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
		}
	}

	public void refresh() {
		write(TinyGDriver.CMD_DISABLE_LOCAL_ECHO);
		write(TinyGDriver.CMD_SET_STATUS_UPDATE_INTERVAL);
		write(TinyGDriver.CMD_GET_STATUS_REPORT);
		write(TinyGDriver.CMD_GET_X_AXIS);
		write(TinyGDriver.CMD_GET_Y_AXIS);
		write(TinyGDriver.CMD_GET_Z_AXIS);
		write(TinyGDriver.CMD_GET_A_AXIS);
		write(TinyGDriver.CMD_GET_B_AXIS);
		write(TinyGDriver.CMD_GET_C_AXIS);
		write(TinyGDriver.CMD_GET_MOTOR_1_SETTINGS);
		write(TinyGDriver.CMD_GET_MOTOR_2_SETTINGS);
		write(TinyGDriver.CMD_GET_MOTOR_3_SETTINGS);
		write(TinyGDriver.CMD_GET_MOTOR_4_SETTINGS);
		write(TinyGDriver.CMD_GET_MACHINE_SETTINGS);
	}
}

