package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class JSONParser {
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

			if (json.has("r"))
				return processResponse(json.getJSONObject("r"), machine);

			// To preserve compatibility with the older firmware
			return processBodyObject(json, machine);
		} catch (JSONException e) {
			Log.e(TAG,
					"JSON: " + e.getMessage());
		}
		return null;
	}

	private static Bundle processResponse(JSONObject r, Machine machine)
			throws JSONException {
		// bd, sc, sm, buf, ln, cks
		Bundle b = processBodyObject(r.getJSONObject("bd"), machine);
		// int statusCode = r.getInt("sc");
		// String statusMsg = r.getString("sm");
		// int bufferRemaining = r.getInt("buf");
		// int lineProcessed = r.getInt("ln");
		// int checksum = r.getInt("cks");
		return b;
	}

	private static Bundle processBodyObject(JSONObject json, Machine machine)
			throws JSONException {
		if (json.has("sr"))
			return processStatusReport(json.getJSONObject("sr"), machine);
		if (json.has("sys"))
			return processSys(json.getJSONObject("sys"), machine);
		if (json.has("1"))
			return processMotor(1, json.getJSONObject("1"), machine);
		if (json.has("2"))
			return processMotor(2, json.getJSONObject("2"), machine);
		if (json.has("3"))
			return processMotor(3, json.getJSONObject("3"), machine);
		if (json.has("4"))
			return processMotor(4, json.getJSONObject("4"), machine);
		if (json.has("a"))
			return processAxis("a", json.getJSONObject("a"), machine);
		if (json.has("b"))
			return processAxis("b", json.getJSONObject("b"), machine);
		if (json.has("c"))
			return processAxis("c", json.getJSONObject("c"), machine);
		if (json.has("x"))
			return processAxis("x", json.getJSONObject("x"), machine);
		if (json.has("y"))
			return processAxis("y", json.getJSONObject("y"), machine);
		if (json.has("z"))
			return processAxis("z", json.getJSONObject("z"), machine);
		return null;
	}

	private static Bundle processStatusReport(JSONObject sr, Machine machine)
			throws JSONException {
		machine.setStatus(sr);
		Bundle b = machine.getStatusBundle();
		b.putString("json", "sr");
		return b;
	}

	private static Bundle processSys(JSONObject sys, Machine machine)
			throws JSONException {
		machine.putSys(sys);
		Bundle b = machine.getStatusBundle();
		b.putString("json", "sys");
		return b;
	}

	private static Bundle processMotor(int num, JSONObject motor,
			Machine machine) throws JSONException {
		machine.putMotor(motor, num);
		Bundle b = machine.getMotorBundle(num);
		b.putString("json", Integer.toString(num));
		return b;
	}

	private static Bundle processAxis(String axisName, JSONObject axis,
			Machine machine) throws JSONException {
		machine.putAxis(axis, axisName);
		Bundle b = machine.getAxisBundle(axisName);
		b.putString("json", axisName);
		return b;
	}
}
