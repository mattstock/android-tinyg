package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

abstract public class TinyGService extends Service {
	public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
	public static final String CMD_GET_STATUS_REPORT = "{\"sr\":null}\n";
	public static final String CMD_SET_QR_VERBOSITY = "{\"qv\":1}\n";
	public static final String CMD_ENABLE_JSON_MODE = "{\"ej\":1}\n";
	public static final String CMD_JSON_VERBOSITY = "{\"jv\":5}\n";
	public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
	public static final String CMD_DISABLE_XON_XOFF = "{\"ex\":0}\n";
	public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":100}\n";
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

	// buffer size on TinyG
	public static final int TINYG_BUFFER_SIZE = 254;

	// broadcast messages when we get updated data
	public static final String STATUS = "org.csgeeks.TinyG.STATUS";
	public static final String CONNECTION_STATUS = "org.csgeeks.TinyG.CONNECTION_STATUS";

	protected static final String TAG = "TinyG";
	protected Machine machine;
	private final Semaphore serialBufferAvail = new Semaphore(
			TINYG_BUFFER_SIZE, true);
	protected final Semaphore writeLock = new Semaphore(1, true);
	private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	private final IBinder mBinder = new TinyGBinder();
	private final QueueProcessor procQ = new QueueProcessor();
	private Thread dequeueWorker;

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

	public void connect() {
		if (dequeueWorker == null || !dequeueWorker.isAlive()) {
			dequeueWorker = new Thread(procQ);
			dequeueWorker.start();
		}			
	}

	abstract protected void write(String cmd);
	abstract protected void write(byte b[]);

	public void disconnect() {
		// Let everyone know we are disconnected
		Bundle b = new Bundle();
		b.putBoolean("connection", false);
		Intent i = new Intent(CONNECTION_STATUS);
		i.putExtras(b);
		sendBroadcast(i, null);
		queue.clear();
		serialBufferAvail.release();
		writeLock.release();
		if (dequeueWorker != null)
			dequeueWorker.interrupt();
		dequeueWorker = null;
		Log.d(TAG, "disconnect done");
	}

	public class TinyGBinder extends Binder {
		public TinyGService getService() {
			return TinyGService.this;
		}
	}

	@SuppressLint("DefaultLocale")
	public static String short_jog(String axis, double step) {
		return String.format("g91g0%s%f", axis, step);
	}

	public void send_gcode(String gcode) {
		send_message("{\"gc\": \"" + gcode + "\"}\n"); // In verbose mode
														// 2, only the f is
														// returned for gc
	}

	// Enqueue a command
	public void send_message(String cmd) {
		try {
			Log.d(TAG, "adding " + cmd);
			queue.put(cmd);
		} catch (InterruptedException e) {
			// This really shouldn't happen
			e.printStackTrace();
		}
	}

	public void send_stop() {
		Log.d(TAG, "in send_stop()");
		queue.clear();
		try {
			writeLock.acquire();
			Log.d(TAG, "sending feedhold");
			write("!");
			Log.d(TAG, "sending {\"qf\":1}");
			write("{\"qf\":1}\n");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		writeLock.release();
	}

	public void send_reset() {
		byte[] rst = {0x18};
		
		Log.d(TAG, "in send_reset()");
		queue.clear();
		try {
			writeLock.acquire();
			Log.d(TAG, "sending reset");
			write(rst);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		writeLock.release();
	}

	public Bundle getMotor(int m) {
		return machine.getMotorBundle(m);
	}

	// apply any new values in the bundle to the machine state
	// and sends the necessary change commands to TinyG
	public void putMotor(int m, Bundle b) {
		String cmd = machine.updateMotorBundle(m, b);
		Log.d(TAG, "update motor command: " + cmd);
		send_message(cmd + "\n");
	}

	public void putAxis(int a, Bundle b) {
		String cmd = machine.updateAxisBundle(a, b);
		Log.d(TAG, "update axis command: " + cmd);
		send_message(cmd + "\n");
	}

	public void putSystem(Bundle b) {
		List<String> cmds = machine.updateSystemBundle(b);

		for (String cmd : cmds) {
			Log.d(TAG, "update system command: " + cmd);
			send_message(cmd + "\n");
		}
	}

	public Bundle getAxis(int a) {
		return machine.getAxisBundle(a);
	}

	public Bundle getMachineStatus() {
		return machine.getStatusBundle();
	}

	protected void updateInfo(String line, Bundle b) {
		String json = b.getString("json");
		Intent i;

		// Currently the only async communication
		if (json != null && json.equals("sr")) {
			i = new Intent(STATUS);
			i.putExtras(b);
			sendBroadcast(i, null);
		}

		int freed = b.getInt("buffer");
		if (freed > 0)
			serialBufferAvail.release(freed);
	}

	// Asks for the service to send a full update of all state.
	public void refresh() {
		send_message(CMD_DISABLE_LOCAL_ECHO);
		send_message(CMD_JSON_VERBOSITY);
		send_message(CMD_SET_QR_VERBOSITY);
		send_message(CMD_SET_STATUS_UPDATE_INTERVAL);
		send_message(CMD_GET_STATUS_REPORT);

		// Preload all of these for later display
		send_message(CMD_GET_A_AXIS);
		send_message(CMD_GET_B_AXIS);
		send_message(CMD_GET_C_AXIS);
		send_message(CMD_GET_X_AXIS);
		send_message(CMD_GET_Y_AXIS);
		send_message(CMD_GET_Z_AXIS);
		send_message(CMD_GET_MOTOR_1_SETTINGS);
		send_message(CMD_GET_MOTOR_2_SETTINGS);
		send_message(CMD_GET_MOTOR_3_SETTINGS);
		send_message(CMD_GET_MOTOR_4_SETTINGS);
		send_message(CMD_GET_MACHINE_SETTINGS);
	}

	private class QueueProcessor implements Runnable {
		public void run() {
			try {
				while (true) {
					String cmd = queue.take();
					serialBufferAvail.acquire(cmd.length());
					writeLock.acquire();
					write(cmd);
					writeLock.release();
				}
			} catch (InterruptedException e) {
				Log.d(TAG, "Exiting queue processor");
			}
		}
	}

}
