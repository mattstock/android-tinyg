package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class Parser {
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

	public static Bundle processJSON(String string, Machine machine) {
		try {
			JSONObject json = new JSONObject(string);
			Bundle b = new Bundle();
			
			if (json.has("sr")) {
				JSONObject sr = json.getJSONObject("sr");
				machine.setStatus(sr);
				b = machine.getStatusBundle();
				b.putString("json", "sr");

				return b;
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
				
				b.putString("json", "gc");
				return b;
			}
			if (json.has("sys")) {
				JSONObject sys = json.getJSONObject("sys");
				machine.putSys(sys);
				b = machine.getStatusBundle();
				b.putString("json", "sys");
				return b;
			}
			if (json.has("1")) {
				JSONObject motor = json.getJSONObject("1");
				machine.putMotor(motor, 1);
				b = machine.getMotorBundle(1);
				b.putString("json", "1");
				return b;
			}
			if (json.has("2")) {
				JSONObject motor = json.getJSONObject("2");
				machine.putMotor(motor, 2);
				b = machine.getMotorBundle(2);
				b.putString("json", "2");
				return b;
			}
			if (json.has("3")) {
				JSONObject motor = json.getJSONObject("3");
				machine.putMotor(motor, 3);
				b = machine.getMotorBundle(3);
				b.putString("json", "3");
				return b;
			}
			if (json.has("4")) {
				JSONObject motor = json.getJSONObject("4");
				machine.putMotor(motor, 4);
				b = machine.getMotorBundle(4);
				b.putString("json", "4");
				return b;
			}
			if (json.has("a")) {
				JSONObject axis = json.getJSONObject("a");
				machine.putAxis(axis, "a");
				b = machine.getAxisBundle("a");
				b.putString("json", "a");				
				return b;
			}
			if (json.has("b")) {
				JSONObject axis = json.getJSONObject("b");
				machine.putAxis(axis, "b");
				b = machine.getAxisBundle("b");
				b.putString("json", "b");				
				return b;
			}
			if (json.has("c")) {
				JSONObject axis = json.getJSONObject("c");
				machine.putAxis(axis, "c");
				b = machine.getAxisBundle("c");
				b.putString("json", "c");				
				return b;
			}
			if (json.has("x")) {
				JSONObject axis = json.getJSONObject("x");
				machine.putAxis(axis, "x");
				b = machine.getAxisBundle("x");
				b.putString("json", "x");				
				return b;
			}
			if (json.has("y")) {
				JSONObject axis = json.getJSONObject("y");
				machine.putAxis(axis, "y");
				b = machine.getAxisBundle("y");
				b.putString("json", "y");				
				return b;
			}
			if (json.has("z")) {
				JSONObject axis = json.getJSONObject("z");
				machine.putAxis(axis, "z");
				b = machine.getAxisBundle("z");
				b.putString("json", "z");				
				return b;
			}
		} catch (JSONException e) {
			Log.e(TAG, "Received malformed JSON: " + string + ": " + e.getMessage());
		}
		return null;
	}

}
