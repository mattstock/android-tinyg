package org.csgeeks.TinyG.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class NetworkDriver extends TinyGDriver {
	private static final String TAG = "TinyG";
	private String hostname;
	private int port;
	InputStream is;
	OutputStream os;
	Socket socket;

	public RetCode connect() {
		try {
			socket = new Socket(hostname, port);
			os = socket.getOutputStream();
			is = socket.getInputStream();
		} catch (UnknownHostException e) {
			socket = null;
			Log.e(TAG,"Socket: " + e.getMessage());
			return new RetCode(false, e.getMessage());
		} catch (IOException e) {
			socket = null;
			Log.e(TAG,"Socket: " + e.getMessage());
			return new RetCode(false, e.getMessage());
		}
		return new RetCode(true, null);
	}
	
	public RetCode disconnect() {
		if (socket != null) {
			try {
				Log.d(TAG,"closing socket");
				socket.close();
				is.close();
				os.close();
			} catch (IOException e) {
				Log.e(TAG, "Close: " + e.getMessage());
				return new RetCode(false, e.getMessage());
			}
			is = null;
			os = null;
			socket = null;
			ready = false;
		}
		Log.d(TAG,"disconnect done");
		return new RetCode(true, null);
	}
	
	public void setHostname(String h) {
		hostname = h;
	}
	
	public NetworkDriver(String h, int p) {
		hostname = h;
		port = p;
	}

	public void setPort(int p) {
		port = p;
	}

	public InputStream getInputStream() {
		return is;
	}

	public void write(String message) {
		try {
			Log.d(TAG,"writing to tinyg: " + message);
			os.write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "write: " + e.getMessage());
		}
	}
}
