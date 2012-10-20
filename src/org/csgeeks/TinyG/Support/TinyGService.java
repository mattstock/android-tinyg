package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

abstract public class TinyGService extends Service {
	public static final String STATUS = "org.csgeeks.TinyG.STATUS";
	public static final String CONNECTION_STATUS = "org.csgeeks.TinyG.CONNECTION_STATUS";
	private static final Object synctoken = new Object();
	private static boolean throttle = false;
	protected static final String TAG = "TinyG";
	protected Machine machine;
	private final IBinder mBinder = new TinyGBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		machine = new Machine();
	}

	@Override
	public void onDestroy() {
		disconnect();
	}

	public Machine getMachine() {
		return machine;
	}

	abstract public void connect();

	public void disconnect() {
		// Let everyone know we are disconnected
		Bundle b = new Bundle();
		b.putBoolean("connection", false);
		Intent i = new Intent(CONNECTION_STATUS);
		i.putExtras(b);
		sendBroadcast(i, null);

		Log.d(TAG, "disconnect done");
		}
	
	abstract public void write(String s);

	public class TinyGBinder extends Binder {
		public TinyGService getService() {
			return TinyGService.this;
		}
	}

	public static String short_jog (String axis, double step) {
		return String.format("g91g0%s%f", axis, step);
	}
	
	public void send_gcode(String gcode) {
		send_raw_gcode("{\"gc\": \"" + gcode + "\"}\n");
	}
	
	// Sends a command to the service
	public void send_raw_gcode(String gcode) {
		try {
			synchronized (synctoken) {
				while (throttle)
					synctoken.wait();
			}
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// This is probably ok, just proceed.
		}
		write(gcode);
	}

	public static void setThrottle(boolean t) {
		synchronized (synctoken) {
			throttle = t;
			synctoken.notify();
		}
	}
	
	public Bundle getMotor(int m) {
		return machine.getMotorBundle(m);
	}
	
	public Bundle getAxis(int a) {
		return machine.getAxisBundle(a);
	}

	public Bundle getMachineStatus() {
		return machine.getStatusBundle();
	}
		
	// Asks for the service to send a full update of all state.
	public void refresh() {
		write(JSONParser.CMD_DISABLE_LOCAL_ECHO);
		write(JSONParser.CMD_SET_STATUS_UPDATE_INTERVAL);
		write(JSONParser.CMD_GET_STATUS_REPORT);
		write(JSONParser.CMD_GET_X_AXIS);
		write(JSONParser.CMD_GET_Y_AXIS);
		write(JSONParser.CMD_GET_Z_AXIS);
		write(JSONParser.CMD_GET_A_AXIS);
		write(JSONParser.CMD_GET_B_AXIS);
		write(JSONParser.CMD_GET_C_AXIS);
		write(JSONParser.CMD_GET_MOTOR_1_SETTINGS);
		write(JSONParser.CMD_GET_MOTOR_2_SETTINGS);
		write(JSONParser.CMD_GET_MOTOR_3_SETTINGS);
		write(JSONParser.CMD_GET_MOTOR_4_SETTINGS);
		write(JSONParser.CMD_GET_MACHINE_SETTINGS);
	}
}
