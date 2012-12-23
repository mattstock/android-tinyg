package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

abstract public class TinyGService extends Service {
	public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
	public static final String CMD_GET_STATUS_REPORT = "{\"sr\":null}\n";
	public static final String CMD_GET_QUEUE_REPORT = "{\"qr\":null}\n";
	public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
	public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
	public static final String CMD_DISABLE_XON_XOFF = "{\"ex\":0}\n";
	public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":0}\n";
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
	public static final String STATUS = "org.csgeeks.TinyG.STATUS";
	public static final String CONNECTION_STATUS = "org.csgeeks.TinyG.CONNECTION_STATUS";
	protected static final String TAG = "TinyG";
	protected Machine machine;
	private final Semaphore available = new Semaphore(1, true);
	private static final BlockingQueue<String[]> queue = new LinkedBlockingQueue<String[]>();
	private final IBinder mBinder = new TinyGBinder();
	private final QueueProcessor procQ = new QueueProcessor();
	private Thread dequeueWorker;
	private String waitFor;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		machine = new Machine();
		dequeueWorker = new Thread(procQ);
		dequeueWorker.start();
	}

	@Override
	public void onDestroy() {
		disconnect();
	}

	public Machine getMachine() {
		return machine;
	}

	abstract public void connect();

	abstract protected void write(String cmd);

	public void disconnect() {
		// Let everyone know we are disconnected
		Bundle b = new Bundle();
		b.putBoolean("connection", false);
		Intent i = new Intent(CONNECTION_STATUS);
		i.putExtras(b);
		sendBroadcast(i, null);
		queue.clear();
		available.release();

		Log.d(TAG, "disconnect done");
	}

	public class TinyGBinder extends Binder {
		public TinyGService getService() {
			return TinyGService.this;
		}
	}

	public static String short_jog(String axis, double step) {
		return String.format("g91g0%s%f", axis, step);
	}

	public void send_gcode(String gcode) {
		send_message("gc", "{\"gc\": \"" + gcode + "\"}\n");
	}

	// Enqueue a command
	public void send_message(String cmd_type, String cmd) {
		try {
			String[] msg = new String[] {cmd_type, cmd};
			queue.put(msg);
		} catch (InterruptedException e) {
			// This really shouldn't happen
			e.printStackTrace();
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

	protected void updateInfo(String line, Bundle b) {
		String json = b.getString("json");
		if (json == null)
			return;
		if (json.equals("sr")) {
			Intent i = new Intent(STATUS);
			i.putExtras(b);
			sendBroadcast(i, null);
		}
		if (json.equals(waitFor))
			available.release();
	}

	// Asks for the service to send a full update of all state.
	public void refresh() {
		send_message("ee", CMD_DISABLE_LOCAL_ECHO);
		send_message("si", CMD_SET_STATUS_UPDATE_INTERVAL);
		send_message("sr", CMD_GET_STATUS_REPORT);
		send_message("x", CMD_GET_X_AXIS);
		send_message("y", CMD_GET_Y_AXIS);
		send_message("z", CMD_GET_Z_AXIS);
		send_message("a", CMD_GET_A_AXIS);
		send_message("b", CMD_GET_B_AXIS);
		send_message("c", CMD_GET_C_AXIS);
		send_message("1", CMD_GET_MOTOR_1_SETTINGS);
		send_message("2", CMD_GET_MOTOR_2_SETTINGS);
		send_message("3", CMD_GET_MOTOR_3_SETTINGS);
		send_message("4", CMD_GET_MOTOR_4_SETTINGS);
		send_message("sys", CMD_GET_MACHINE_SETTINGS);
	}

	private class QueueProcessor implements Runnable {
		public void run() {
			try {
				while (true) {
					available.acquire();
					String[] cmd = queue.take();
					Log.d(TAG, "send: " + cmd[1]);
					waitFor = cmd[0];
					write(cmd[1]);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
