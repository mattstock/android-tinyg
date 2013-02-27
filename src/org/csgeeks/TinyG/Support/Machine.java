package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class Machine {
	private static final String TAG = "TinyG";

	// Machine state variables
	private Bundle state;
	private Bundle axis[] = new Bundle[6];
	private Bundle motor[] = new Bundle[4];

	public Machine() {
		for (int i = 0; i < 4; i++) {
			motor[i] = new Bundle();
		}
		for (int i = 0; i < 6; i++) {
			axis[i] = new Bundle();
		}
		state = new Bundle();
	}

	public void setQueue(int qr) {
		state.putInt("qr", qr);
	}

	public void setStatus(JSONObject sr) throws JSONException {
		if (sr.has("posx"))
			state.putFloat("posx", (float) sr.getDouble("posx"));
		if (sr.has("posy"))
			state.putFloat("posy", (float) sr.getDouble("posy"));
		if (sr.has("posz"))
			state.putFloat("posz", (float) sr.getDouble("posz"));
		if (sr.has("posa"))
			state.putFloat("posa", (float) sr.getDouble("posa"));
		if (sr.has("vel"))
			state.putFloat("velocity", (float) sr.getDouble("vel"));
		if (sr.has("line"))
			state.putInt("line", sr.getInt("line"));
		if (sr.has("momo"))
			switch (sr.getInt("momo")) {
			case 0:
				state.putString("momo", "traverse");
				break;
			case 1:
				state.putString("momo", "straight");
				break;
			case 2:
				state.putString("momo", "cw_arc");
				break;
			case 3:
				state.putString("momo", "ccw_arc");
				break;
			case 4:
				state.putString("momo", "invalid");
				break;
			}

		if (sr.has("stat"))
			switch (sr.getInt("stat")) {
			case 0:
				state.putString("status", "init");
				break;
			case 1:
				state.putString("status", "reset");
				break;
			case 2:
				state.putString("status", "stop");
				break;
			case 3:
				state.putString("status", "end");
				break;
			case 4:
				state.putString("status", "run");
				break;
			case 5:
				state.putString("status", "hold");
				break;
			case 6:
				state.putString("status", "probe");
				break;
			case 7:
				state.putString("status", "cycle");
				break;
			case 8:
				state.putString("status", "homing");
				break;
			case 9:
				state.putString("status", "jog");
				break;
			}

		if (sr.has("unit")) {
			switch (sr.getInt("unit")) {
			case 0:
				state.putString("units", "inches");
				break;
			case 2:
				state.putString("units", "mm");
				break;
			}
		}
	}

	public Bundle getStatusBundle() {
		return state;
	}

	public void putSys(JSONObject sysjson) throws JSONException {
		state.putFloat("firmware_build", (float) sysjson.getDouble("fb"));
		state.putFloat("firmware_version", (float) sysjson.getDouble("fv"));
		state.putFloat("hardware_version", (float) sysjson.getDouble("hv"));
		state.putFloat("json_verbosity", (float) sysjson.getDouble("jv"));
		state.putInt("system_interval", sysjson.getInt("si"));
		state.putString("board_id", sysjson.getString("id"));
		state.putFloat("junction_acceleration", (float) sysjson.getDouble("ja"));
		state.putFloat("chordal_tolerance", (float) sysjson.getDouble("ct"));
		state.putBoolean("switch_type", sysjson.getInt("st") == 1);
		state.putBoolean("enable_json", sysjson.getInt("ej") == 1);
		state.putInt("json_verbosity", sysjson.getInt("jv"));
		state.putInt("text_verbosity", sysjson.getInt("tv"));
		state.putInt("queue_verbosity", sysjson.getInt("qv"));
		state.putInt("status_verbosity", sysjson.getInt("sv"));
		state.putInt("status_interval", sysjson.getInt("si"));
		state.putInt("gcode_plane", sysjson.getInt("gpl"));
	}

	public void putAxis(JSONObject axisjson, String name) throws JSONException {
		Bundle a = axis[axisNameToIndex(name)];

		a.putFloat("travel_max", (float) axisjson.getDouble("tm"));
		a.putFloat("velocity_max", (float) axisjson.getDouble("vm"));
		a.putFloat("jerk_max", (float) axisjson.getDouble("jm"));
		a.putFloat("junction_deviation", (float) axisjson.getDouble("jd"));
		a.putFloat("feed_rate", (float) axisjson.getDouble("fr"));
		a.putBoolean("axis_mode", axisjson.getInt("am") == 1);
		a.putInt("axis", axisNameToIndex(name));
		if (axisjson.has("sv"))
			a.putFloat("search_velocity", (float) axisjson.getDouble("sv"));
		if (axisjson.has("lv"))
			a.putFloat("latch_velocity", (float) axisjson.getDouble("lv"));
		if (axisjson.has("sn"))
			a.putInt("switch_min", axisjson.getInt("sn"));
		if (axisjson.has("sx"))
			a.putInt("switch_max", axisjson.getInt("sx"));
		if (axisjson.has("lb"))
			a.putFloat("latch_backoff", (float) axisjson.getDouble("lb"));
		if (axisjson.has("zb"))
			a.putFloat("zero_backoff", (float) axisjson.getDouble("zb"));
	}

	public Bundle getAxisBundle(int idx) {
		if (idx < 0 || idx > 5)
			return axis[0];
		else
			return axis[idx];
	}

	public Bundle getAxisBundle(String string) {
		return axis[axisNameToIndex(string)];
	}

	public void putMotor(JSONObject motorjson, int name) throws JSONException {
		Bundle m;

		if (name < 1 || name > 4)
			m = motor[0];
		else
			m = motor[name - 1];

		m.putFloat("travel_rev", (float) motorjson.getDouble("tr"));
		m.putFloat("step_angle", (float) motorjson.getDouble("sa"));
		m.putInt("microsteps", motorjson.getInt("mi"));
		m.putBoolean("polarity", motorjson.getInt("po") == 1);
		m.putBoolean("power_management", motorjson.getInt("pm") == 1);
		m.putInt("map_to_axis", motorjson.getInt("ma"));
		m.putInt("motor", name);
	}

	public Bundle getMotorBundle(int m) {
		if (m < 1 || m > 4)
			return motor[0];
		else
			return motor[m - 1];
	}

	private int axisNameToIndex(String string) {
		if (string.equals("x")) {
			return 0;
		}
		if (string.equals("y")) {
			return 1;
		}
		if (string.equals("z")) {
			return 2;
		}
		if (string.equals("a")) {
			return 3;
		}
		if (string.equals("b")) {
			return 4;
		}
		if (string.equals("c")) {
			return 5;
		}
		return 0;
	}

	public Bundle processJSON(String string) {
		Bundle bResult;
		try {
			JSONObject json = new JSONObject(string);

			if (json.has("r")) {
				bResult = processBody(string, json.getJSONObject("r"));
				return bResult;
			}
			if (json.has("sr")) {
				bResult = processStatusReport(json.getJSONObject("sr"));
				return bResult;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage() + " : " + string);
		}
		return null;
	}

	// [<protocol_version>, <status_code>, <input_available>, <checksum>]
	private Bundle processFooter(JSONArray json) throws JSONException,
			NumberFormatException {
		Bundle b = new Bundle();
		b.putInt("protocol", json.getInt(0));
		b.putInt("status", json.getInt(1));
		b.putInt("buffer", json.getInt(2));
		b.putInt("checksum", Integer.parseInt(json.getString(3)));
		return b;
	}

	private Bundle processBody(String json_string, JSONObject json)
			throws JSONException {
		Bundle fResult = processFooter(json.getJSONArray("f"));

		// Check checksum
		int pos = json_string.lastIndexOf(",");
		if (pos == -1) // Shouldn't be possible!
			return null;
		String subval = json_string.substring(0, pos);
		int check = fResult.getInt("checksum");
		long y = (subval.hashCode() & 0x00000000ffffffffL) % 9999;
		if (y != check) {
			Log.e(TAG, "Checksum error for: " + json_string + " (" + y + "," + check + ")");
			return null;
		}
		switch (fResult.getInt("status")) {
		case 0: // OK
		case 3: // NOOP
		case 60: // NULL move
			break;
		default:
			Log.e(TAG, "Status code error: " + json_string);
			return null;
		}

		if (json.has("sr")) 
			fResult.putAll(processStatusReport(json.getJSONObject("sr")));
		if (json.has("qr"))
			fResult.putAll(processQueueReport(json.getInt("qr")));
		if (json.has("sys"))
			fResult.putAll(processSys(json.getJSONObject("sys")));
		if (json.has("1"))
			fResult.putAll(processMotor(1, json.getJSONObject("1")));
		if (json.has("2"))
			fResult.putAll(processMotor(2, json.getJSONObject("2")));
		if (json.has("3"))
			fResult.putAll(processMotor(3, json.getJSONObject("3")));
		if (json.has("4"))
			fResult.putAll(processMotor(4, json.getJSONObject("4")));
		if (json.has("a"))
			fResult.putAll(processAxis("a", json.getJSONObject("a")));
		if (json.has("b"))
			fResult.putAll(processAxis("b", json.getJSONObject("b")));
		if (json.has("c"))
			fResult.putAll(processAxis("c", json.getJSONObject("c")));
		if (json.has("x"))
			fResult.putAll(processAxis("x", json.getJSONObject("x")));
		if (json.has("y"))
			fResult.putAll(processAxis("y", json.getJSONObject("y")));
		if (json.has("z"))
			fResult.putAll(processAxis("z", json.getJSONObject("z")));
		return fResult;
	}

	private Bundle processStatusReport(JSONObject sr) throws JSONException {
		setStatus(sr);
		Bundle b = getStatusBundle();
		b.putString("json", "sr");
		return b;
	}

	private Bundle processQueueReport(int qr) {
		setQueue(qr);
		Bundle b = getStatusBundle();
		b.putString("json", "qr");
		return b;
	}

	private Bundle processSys(JSONObject sys) throws JSONException {
		putSys(sys);
		Bundle b = getStatusBundle();
		b.putString("json", "sys");
		return b;
	}

	private Bundle processMotor(int num, JSONObject motor) throws JSONException {
		putMotor(motor, num);
		Bundle b = getMotorBundle(num);
		b.putString("json", Integer.toString(num));
		return b;
	}

	private Bundle processAxis(String axisName, JSONObject axis)
			throws JSONException {
		putAxis(axis, axisName);
		Bundle b = getAxisBundle(axisName);
		b.putString("json", axisName);
		return b;
	}
}
