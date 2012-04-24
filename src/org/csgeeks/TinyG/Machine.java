package org.csgeeks.TinyG;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Machine {
	private Double x,y,z;
	
	private static final String TAG = "TinyG";
	public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
	public static final String CMD_GET_STATUS_REPORT = "{\"sr\":\"\"}\n";
	public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
	public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
	public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":50}\n";
	private static final String CMD_GET_MACHINE_SETTINGS = "{\"sys\":null}\n";
	private static final String CMD_GET_X_AXIS = "{\"x\":null}\n";
	private static final String CMD_GET_Y_AXIS = "{\"y\":null}\n";
	private static final String CMD_GET_Z_AXIS = "{\"z\":null}\n";
	private static final String CMD_GET_A_AXIS = "{\"a\":null}\n";
	private static final String CMD_GET_B_AXIS = "{\"b\":null}\n";
	private static final String CMD_GET_C_AXIS = "{\"c\":null}\n";
	private static final String CMD_GET_MOTOR_1_SETTINGS = "{\"1\":null}\n";
	private static final String CMD_GET_MOTOR_2_SETTINGS = "{\"2\":null}\n";
	private static final String CMD_GET_MOTOR_3_SETTINGS = "{\"3\":null}\n";
	private static final String CMD_GET_MOTOR_4_SETTINGS = "{\"4\":null}\n";
	private static final String STATUS_REPORT = "{\"sr\":{";
	private static final String CMD_PAUSE = "!\n";
	private static final String CMD_RESUME = "~\n";

	public Machine() {
		x = y = z = 0.0;
	}

	public boolean processJSON(String string) {
		try {
			JSONObject json = new JSONObject(string);
			if (json.has("sr")) {
				x = json.getJSONObject("sr").getDouble("posx");
				y = json.getJSONObject("sr").getDouble("posy");
				z = json.getJSONObject("sr").getDouble("posz");
				Log.i(TAG, "x: " + x.toString() + " y: " + y.toString()
						+ " z: " + z.toString());
				return true;
			}
			if (json.has("gc")) {
				// Ignore?
			}
			if (json.has("ee")) {
				// Ignore?
			}
			if (json.has("si")) {
				// Ignore?
			}
		} catch (JSONException e) {
			Log.e(TAG,
					"Received malformed JSON: " + string + ": "
							+ e.getMessage());
		}
		return false;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
}
