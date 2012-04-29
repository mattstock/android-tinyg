package org.csgeeks.TinyG.driver;

import java.io.InputStream;

import org.csgeeks.TinyG.system.Machine;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public abstract class TinyGDriver {
	private Machine machine;
	protected boolean ready;
	
	private static final String TAG = "TinyG";
	public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
	public static final String CMD_GET_STATUS_REPORT = "{\"sr\":null}\n";
	public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
	public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
	public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":150}\n";
	public static final String CMD_GET_MACHINE_SETTINGS = "{\"sys\":null}\n";
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
	private static final String STATUS_REPORT = "{\"sr\":{";
	private static final String CMD_PAUSE = "!\n";
	private static final String CMD_RESUME = "~\n";

	public TinyGDriver() {
		machine = new Machine();
		ready = false;
	}

	public Machine getMachine() {
		return machine;
	}
	
	public boolean processJSON(String string) {
		try {
			JSONObject json = new JSONObject(string);
			if (json.has("sr")) {
				JSONObject sr = json.getJSONObject("sr");
				// We should do this based on the machine configuration.  Hardcoded for now.
				machine.getAxisByName("X").setWork_position((float) sr.getDouble("posx"));
				machine.getAxisByName("Y").setWork_position((float) sr.getDouble("posy"));
				machine.getAxisByName("Z").setWork_position((float) sr.getDouble("posz"));
				machine.getAxisByName("A").setWork_position((float) sr.getDouble("posa"));
				machine.setLine_number(sr.getInt("line"));
				machine.setVelocity((float) sr.getDouble("vel"));
				machine.setUnits(sr.getInt("unit"));
				machine.setMotionMode(sr.getInt("momo"));
				machine.setMachineState(sr.getInt("stat"));
				ready = true;
				return true;
			}
			if (json.has("gc")) {
				JSONObject gc = json.getJSONObject("gc");
				String gcode = gc.getString("gc");
				if (gc.getString("msg").equals("OK")) {
					if (gcode.regionMatches(0,"G92",0,3)) {
						// Should set the local screen value based on this.  Instead, I'm going to cheat
						// and set it when we send the zero command...
						Log.i(TAG, "Got confirmation of zeroing");
					}
				}
			}
			if (json.has("sys")) {
				
			}
		} catch (JSONException e) {
			Log.e(TAG,
					"Received malformed JSON: " + string + ": "
							+ e.getMessage());
		}
		return false;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public abstract RetCode disconnect();
	public abstract void write(String string);
	public abstract InputStream getInputStream();
	public abstract RetCode connect();
}
