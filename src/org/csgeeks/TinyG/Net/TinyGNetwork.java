package org.csgeeks.TinyG.Net;

// Copyright 2012 Matthew Stock

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.csgeeks.TinyG.Support.JSONParser;
import org.csgeeks.TinyG.Support.TinyGService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TinyGNetwork extends TinyGService {
	private String tgfx_hostname;
	private int tgfx_port;
	private Socket socket;
	protected InputStream is;
	protected OutputStream os;
	protected ListenerTask mListener;

	public void disconnect() {
		super.disconnect();
		try {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		} catch (IOException e) {
			Log.e(TAG, "Close: " + e.getMessage());
		}
		is = null;
		os = null;

		if (mListener != null) {
			mListener.cancel(true);	
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		socket = null;
	}

	public void write(String message) {
		try {
			os.write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "network write exception: " + e.getMessage());
		} catch (NullPointerException e) {
			Log.e(TAG, "write to network attempted without socket established.");
		}
	}
	
	// We call the initialize function to configure any local variables, pulling
	// preferences in for example.
	public void connect() {
		initialize();
		
		new ConnectTask().execute(0);
		Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
	}
	
	// This AsyncTask runs the client-specific connection code. 
	private class ConnectTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect in background");
		
			try {
				socket = new Socket(tgfx_hostname, tgfx_port);
				os = socket.getOutputStream();
				is = socket.getInputStream();
			} catch (Exception e) {
				socket = null;
				Log.e(TAG,"Socket: " + e.getMessage());
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean res) {
			if (res) {
				mListener = new ListenerTask();
				mListener.execute(new InputStream[] { is });
				// Let everyone know we are connected
				Bundle b = new Bundle();
				b.putBoolean("connection", true);
				Intent i = new Intent(CONNECTION_STATUS);
				i.putExtras(b);
				sendBroadcast(i, null);
				refresh();
				Log.i(TAG, "Listener started, connection_status intent sent");
			} else {
				Toast.makeText(TinyGNetwork.this, "Connection failed", Toast.LENGTH_SHORT).show();			
			}
		}
	}

	protected void initialize() {
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));	
	}
	
	protected class ListenerTask extends AsyncTask<InputStream, String, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] buffer = new byte[1024];
			InputStream lis = params[0];
			int b;
			int idx = 0;
			try {
				while (!isCancelled()) {
					if ((b = lis.read()) == -1) {
						break;
					}
					buffer[idx++] = (byte) b;
					if (b == 0x13) {
						Log.d(TAG, "Found XOFF!");
						idx--;
						setThrottle(true);
					}
					if (b == 0x11) {
						Log.d(TAG, "Found XON!");
						idx--;
						setThrottle(false);
					}

					if (b == '\n') {
						publishProgress(new String(buffer, 0, idx));
						idx = 0;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "listener read: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Bundle b;
			if (values.length > 0) {
				if ((b = JSONParser.processJSON(values[0], machine)) != null) {
					String json = b.getString("json");
					if (json.equals("sr")) {
						Intent i = new Intent(STATUS);
						i.putExtras(b);
						sendBroadcast(i, null);
					}
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
		}
	}

}

