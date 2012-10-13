package org.csgeeks.TinyG.Support;

// Copyright 2012 Matthew Stock

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class TinyGMessenger {
	private static final String TAG = "TinyG";

	private Messenger messenger;
	
	public TinyGMessenger(Messenger m) {
		messenger = m;
	}
	
	public void short_jog (String axis, double step) {
		send_gcode(String.format("g91g0%s%f", axis, step));
	}
	
	public void send_gcode(String gcode) {
		send_raw_gcode("{\"gc\": \"" + gcode + "\"}\n");
	}
	
	// Sends a command to the service
	public void send_raw_gcode(String gcode) {
		if (messenger == null) {
			Log.d(TAG, "Messenger uninitialized");
			return;
		}
        Message msg = Message.obtain(null, ServiceWrapper.GCODE, 0, 0, null);
        Bundle b = new Bundle();
        b.putString("gcode", gcode);
        msg.setData(b);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }		
	}

	// Send a command to the service and expect a response.
	public void send_command(int cmd, int arg) {
		if (messenger == null)
			return;
		
		Log.d(TAG, "sending a message to service");
        Message msg = Message.obtain(null, cmd, arg, 0);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }		
	}

	// Helper
	public void send_command(int cmd) {
		send_command(cmd, 0);
	}
}
