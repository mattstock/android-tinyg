package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

abstract public class TinyGDriver extends Service {
	public static final String MACHINE_CONFIG = "org.csgeeks.TinyG.MACHINE_CONFIG";
	public static final String AXIS_CONFIG = "org.csgeeks.TinyG.AXIS_CONFIG";
	public static final String MOTOR_CONFIG = "org.csgeeks.TinyG.MOTOR_CONFIG";
	public static final String STATUS = "org.csgeeks.TinyG.STATUS";
	public static final String THROTTLE = "org.csgeeks.TinyG.THROTTLE";
	public static final String CONNECTION_STATUS = "org.csgeeks.TinyG.CONNECTION_STATUS";
	public static final String USB_SERVICE = "org.csgeeks.TinyG.USB.USBService";
	public static final String NETWORK_SERVICE = "org.csgeeks.TinyG.Support.TinyGNetwork";
	public static final int GET_MOTOR = 0;
	public static final int GET_AXIS = 1;
	public static final int GET_MACHINE = 2;
	public static final int REFRESH = 13;
	public static final int GCODE = 14;
	public static final int CONNECT = 15;
	public static final int DISCONNECT = 16;

	protected static final String TAG = "TinyG";
	protected Machine machine;
	protected final Messenger mMessenger = new Messenger(new ServiceHandler());

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
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

	private class ServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Bundle b;
			Intent i;

			switch (msg.what) {
			case REFRESH:
				refresh();
				break;
			case CONNECT:
				connect();
				break;
			case DISCONNECT:
				disconnect();
				break;
			case GCODE:
				b = msg.getData();
				write(b.getString("gcode"));
				break;
			case GET_MOTOR:
				b = machine.getMotorBundle(msg.arg1);
				if (b == null)
					break;
				i = new Intent(MOTOR_CONFIG);
				i.putExtras(b);
				sendBroadcast(i, null);
				break;
			case GET_AXIS:
				b = machine.getAxisBundle(msg.arg1);
				if (b == null)
					break;
				Log.d(TAG, "sending axis config intent");
				i = new Intent(AXIS_CONFIG);
				i.putExtras(b);
				sendBroadcast(i, null);
				break;
			case GET_MACHINE:
				b = machine.getStatusBundle();
				i = new Intent(MACHINE_CONFIG);
				i.putExtras(b);
				sendBroadcast(i, null);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	abstract public void connect();

	// Asks for the service to send a full update of all state.
	public void refresh() {
		write(Parser.CMD_DISABLE_LOCAL_ECHO);
		write(Parser.CMD_SET_STATUS_UPDATE_INTERVAL);
		write(Parser.CMD_GET_STATUS_REPORT);
		write(Parser.CMD_GET_X_AXIS);
		write(Parser.CMD_GET_Y_AXIS);
		write(Parser.CMD_GET_Z_AXIS);
		write(Parser.CMD_GET_A_AXIS);
		write(Parser.CMD_GET_B_AXIS);
		write(Parser.CMD_GET_C_AXIS);
		write(Parser.CMD_GET_MOTOR_1_SETTINGS);
		write(Parser.CMD_GET_MOTOR_2_SETTINGS);
		write(Parser.CMD_GET_MOTOR_3_SETTINGS);
		write(Parser.CMD_GET_MOTOR_4_SETTINGS);
		write(Parser.CMD_GET_MACHINE_SETTINGS);
	}
}
