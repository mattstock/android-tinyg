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

	public void setQueue(JSONObject qr) throws JSONException {
		if (qr.has("lix"))
			state.putInt("lix", qr.getInt("lix"));
		if (qr.has("pba"))
			state.putInt("pba", qr.getInt("pba"));
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
		state.putFloat("velocity", (float) sr.getDouble("vel"));
		state.putInt("line", sr.getInt("line"));

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
		switch (sr.getInt("unit")) {
		case 0:
			state.putString("units", "inches");
			break;
		case 2:
			state.putString("units", "mm");
			break;
		}
	}

	public Bundle getStatusBundle() {
		return state;
	}

	public void putSys(JSONObject sysjson) throws JSONException {
		state.putFloat("firmware_build", (float) sysjson.getDouble("fb"));
		state.putFloat("firmware_version", (float) sysjson.getDouble("fv"));
		state.putInt("system_interval", sysjson.getInt("si"));
	}

	public void putAxis(JSONObject axisjson, String name) throws JSONException {
		Bundle a = axis[axisNameToIndex(name)];

		a.putFloat("travel_max", (float) axisjson.getDouble("tm"));
		a.putFloat("velocity_max", (float) axisjson.getDouble("vm"));
		a.putFloat("jerk_max", (float) axisjson.getDouble("jm"));
		a.putFloat("junction_deviation", (float) axisjson.getDouble("jd"));
		a.putFloat("feed_rate", (float) axisjson.getDouble("fr"));
		a.putFloat("search_velocity", (float) axisjson.getDouble("sv"));
		a.putFloat("latch_velocity", (float) axisjson.getDouble("lv"));
		a.putBoolean("axis_mode", axisjson.getInt("am") == 1);
		a.putInt("switch_mode", axisjson.getInt("sm"));
		a.putInt("axis", axisNameToIndex(name));
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
		Bundle bResult, fResult;
		try {
			JSONObject json = new JSONObject(string);

			if (json.has("b")) {
				bResult = processBody(json.getJSONObject("b"));
				fResult = processFooter(json.getJSONArray("f"));
				return bResult;
			} else {
				// To preserve compatibility with the older firmware
				return processBody(json);
			}
		} catch (JSONException e) {
			Log.e(TAG,
					"JSON: " + e.getMessage());
		}
		return null;
	}

	private Bundle processFooter(JSONArray json) throws JSONException {
		// TODO need to figure out how to handle failure, either of parsing or of 
		return null;
	}
	
	private Bundle processBody(JSONObject json) throws JSONException {
		if (json.has("sr"))
			return processStatusReport(json.getJSONObject("sr"));
		if (json.has("qr"))
			return processQueueReport(json.getJSONObject("qr"));
		if (json.has("sys"))
			return processSys(json.getJSONObject("sys"));
		if (json.has("1"))
			return processMotor(1, json.getJSONObject("1"));
		if (json.has("2"))
			return processMotor(2, json.getJSONObject("2"));
		if (json.has("3"))
			return processMotor(3, json.getJSONObject("3"));
		if (json.has("4"))
			return processMotor(4, json.getJSONObject("4"));
		if (json.has("a"))
			return processAxis("a", json.getJSONObject("a"));
		if (json.has("b"))
			return processAxis("b", json.getJSONObject("b"));
		if (json.has("c"))
			return processAxis("c", json.getJSONObject("c"));
		if (json.has("x"))
			return processAxis("x", json.getJSONObject("x"));
		if (json.has("y"))
			return processAxis("y", json.getJSONObject("y"));
		if (json.has("z"))
			return processAxis("z", json.getJSONObject("z"));
		// Anything else doesn't change the state of the machine.
		// Just get the block type so that we can check ACK status later.
		Bundle b = new Bundle();
		for (Iterator<String> it = json.keys(); it.hasNext();) {
			String reply = it.next();
			Log.d(TAG, "Storing misc response type " + reply);
			b.putString("json", reply);
		}
		Log.d(TAG, "Done with processing");
		return b;
	}

	private Bundle processStatusReport(JSONObject sr) throws JSONException {
		setStatus(sr);
		Bundle b = getStatusBundle();
		b.putString("json", "sr");
		return b;
	}

	private Bundle processQueueReport(JSONObject qr) throws JSONException {
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

	private Bundle processAxis(String axisName, JSONObject axis) throws JSONException {
		putAxis(axis, axisName);
		Bundle b = getAxisBundle(axisName);
		b.putString("json", axisName);
		return b;
	}
}
