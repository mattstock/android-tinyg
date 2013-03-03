package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Support.Config.TinyGType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class Machine {
	private static final String TAG = "TinyG";
	private static final String UPDATE_BLOCK_FORMAT = "{\"%s\": {%s}}";
	private static final String UPDATE_VALUE_FORMAT = "\"%s\": %s";
	private static final String axisIndexToName[] = { "x", "y", "z", "a", "b",
			"c" };
	private static final TGVar axisVars[] = new TGVar[] {
			new TGVar("tm", "float"), new TGVar("vm", "float"),
			new TGVar("jm", "float"), new TGVar("jd", "float"),
			new TGVar("ra", "float"), new TGVar("fr", "float"),
			new TGVar("am", "int"), new TGVar("sv", "float"),
			new TGVar("lv", "float"), new TGVar("sn", "int"),
			new TGVar("sx", "int"), new TGVar("zb", "float") };
	private static final TGVar sysVars[] = new TGVar[] {
			new TGVar("fb", "float"), new TGVar("fv", "float"),
			new TGVar("hv", "float"), new TGVar("id", "string"),
			new TGVar("ja", "float"), new TGVar("ct", "float"),
			new TGVar("st", "boolean"), new TGVar("ej", "boolean"),
			new TGVar("jv", "int"), new TGVar("tv", "int"),
			new TGVar("qv", "int"), new TGVar("sv", "int"),
			new TGVar("si", "int"), new TGVar("ic", "boolean"),
			new TGVar("ec", "boolean"), new TGVar("ee", "boolean"),
			new TGVar("ex", "boolean") };

	// Machine state variables
	private Bundle state;
	private Bundle axis[] = new Bundle[6];
	private Bundle motor[] = new Bundle[4];
	private static Config machineVars;
	
	public Machine() {
		for (int i = 0; i < 4; i++) {
			motor[i] = new Bundle();
		}
		for (int i = 0; i < 6; i++) {
			axis[i] = new Bundle();
		}
		state = new Bundle();
		machineVars = new Config();
	}

	public Bundle getStatusBundle() {
		return state;
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

	public Bundle getMotorBundle(int m) {
		if (m < 1 || m > 4)
			return motor[0];
		else
			return motor[m - 1];
	}

	public String updateAxisBundle(int anum, Bundle b) {
		String scratch;
		String cmds = null;
		Bundle a = axis[anum];

		a.putAll(b);

		for (TGVar v : axisVars) {
			if (b.containsKey(v.name)) {
				scratch = "";
				if (v.type.equals("float"))
					scratch = Float.toString(b.getFloat(v.name));
				if (v.type.equals("boolean"))
					scratch = b.getBoolean(v.name) ? "1" : "0";
				if (v.type.equals("string"))
					scratch = b.getString(v.name);
				if (v.type.equals("int"))
					scratch = Integer.toString(b.getInt(v.name));
				scratch = String.format(UPDATE_VALUE_FORMAT, v.name, scratch);
				if (cmds == null)
					cmds = scratch;
				else
					cmds = ", " + scratch;
			}
		}

		return String.format(UPDATE_BLOCK_FORMAT, axisIndexToName[anum], cmds);
	}

	public String updateMotorBundle(int mnum, Bundle b) {
		String scratch;
		String cmds = null;
		Bundle m = motor[mnum];

		m.putAll(b);

		for (Config.TinyGType v : machineVars.getMotor()) {
			if (b.containsKey(v.name)) {
				scratch = "";
				if (v.type.equals("float"))
					scratch = Float.toString(b.getFloat(v.name));
				if (v.type.equals("boolean"))
					scratch = b.getBoolean(v.name) ? "1" : "0";
				if (v.type.equals("string"))
					scratch = b.getString(v.name);
				if (v.type.equals("int"))
					scratch = Integer.toString(b.getInt(v.name));
				scratch = String.format(UPDATE_VALUE_FORMAT, v.name, scratch);
				if (cmds == null)
					cmds = scratch;
				else
					cmds = ", " + scratch;
			}
		}


		return String.format(UPDATE_BLOCK_FORMAT, Integer.toString(mnum), cmds);
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

	private void setQueue(int qr) {
		state.putInt("qr", qr);
	}

	private void setStatus(JSONObject sr) throws JSONException {
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
				state.putString("momo", "seek");
				break;
			case 1:
				state.putString("momo", "feed");
				break;
			case 2:
				state.putString("momo", "cw_arc");
				break;
			case 3:
				state.putString("momo", "ccw_arc");
				break;
			case 4:
				state.putString("momo", "cancel");
				break;
			case 5:
				state.putString("momo", "probe");
				break;
			default:
				state.putString("momo", Integer.toString(sr.getInt("momo")));
				break;
			}

		if (sr.has("stat"))
			switch (sr.getInt("stat")) {
			case 0:
				state.putString("status", "init");
				break;
			case 1:
				state.putString("status", "ready");
				break;
			case 2:
				state.putString("status", "shutdown");
				break;
			case 3:
				state.putString("status", "stop");
				break;
			case 4:
				state.putString("status", "end");
				break;
			case 5:
				state.putString("status", "run");
				break;
			case 6:
				state.putString("status", "hold");
				break;
			case 7:
				state.putString("status", "probe");
				break;
			case 8:
				state.putString("status", "cycle");
				break;
			case 9:
				state.putString("status", "homing");
				break;
			case 10:
				state.putString("status", "jog");
				break;
			}

		if (sr.has("unit")) {
			switch (sr.getInt("unit")) {
			case 0:
				state.putString("units", "inches");
				break;
			case 1:
				state.putString("units", "mm");
				break;
			case 2:
				state.putString("units", "degrees");
				break;
			}
		}
	}

	private void putSys(JSONObject sysjson) throws JSONException {
		for (TGVar v : sysVars) {
			if (sysjson.has(v.name)) {
				if (v.type.equals("float"))
					state.putFloat(v.name, (float) sysjson.getDouble(v.name));
				if (v.type.equals("boolean"))
					state.putBoolean(v.name, sysjson.getInt(v.name) == 1);
				if (v.type.equals("int"))
					state.putInt(v.name, sysjson.getInt(v.name));
				if (v.type.equals("string"))
					state.putString(v.name, sysjson.getString(v.name));
			}
		}
	}

	private void putAxis(JSONObject axisjson, String name) throws JSONException {
		Bundle a = axis[axisNameToIndex(name)];

		a.putInt("axis", axisNameToIndex(name));

		for (TGVar v : axisVars) {
			if (axisjson.has(v.name)) {
				if (v.type.equals("float"))
					a.putFloat(v.name, (float) axisjson.getDouble(v.name));
				if (v.type.equals("boolean"))
					a.putBoolean(v.name, axisjson.getInt(v.name) == 1);
				if (v.type.equals("int"))
					a.putInt(v.name, axisjson.getInt(v.name));
				if (v.type.equals("string"))
					a.putString(v.name, axisjson.getString(v.name));
			}
		}
	}

	private void putMotor(JSONObject motorjson, int name) throws JSONException {
		Bundle m;

		if (name < 1 || name > 4)
			m = motor[0];
		else
			m = motor[name - 1];
		m.putInt("motor", name);

		for (TinyGType v : machineVars.getMotor()) {
			if (motorjson.has(v.name)) {
				if (v.type.equals("float"))
					m.putFloat(v.name, (float) motorjson.getDouble(v.name));
				if (v.type.equals("boolean"))
					m.putBoolean(v.name, motorjson.getInt(v.name) == 1);
				if (v.type.equals("int"))
					m.putInt(v.name, motorjson.getInt(v.name));
				if (v.type.equals("string"))
					m.putString(v.name, motorjson.getString(v.name));
			}
		}
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
			Log.e(TAG, "Checksum error for: " + json_string + " (" + y + ","
					+ check + ")");
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

	private static class TGVar {
		public String name;
		public String type;

		public TGVar(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}
}
