package org.csgeeks.TinyG.Net;

// Copyright 2012 Matthew Stock

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
	private static final int NETWORK_BUFFER_SIZE = 16 * 1024;
	private static final String TAG = "TinyG-Network";
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
				Log.e(TAG, "Socket: " + e.getMessage());
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
				Toast.makeText(TinyGNetwork.this, "Connection failed",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}

	protected void initialize() {
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));
	}

	protected class ListenerTask extends AsyncTask<InputStream, String, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] inbuffer = new byte[NETWORK_BUFFER_SIZE];
			byte[] linebuffer = new byte[1024];
			InputStream lis = params[0];
			int cnt, idx = 0;
			try {
				while (!isCancelled()) {
					if ((cnt = lis.read(inbuffer, 0, NETWORK_BUFFER_SIZE)) < 0) {
						Log.e(TAG, "network read failure");
						break;
					}
					for (int i = 0; i < cnt; i++)
						if (inbuffer[i] == '\n') {
							String foo = new String(linebuffer, 0, idx);
							Log.d(TAG, "string inside listenertask: " + foo);
							publishProgress(foo);
							idx = 0;
						} else
							linebuffer[idx++] = inbuffer[i];
				}
			} catch (IOException e) {
				Log.e(TAG, "listener read exception: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Bundle b;
			if (values.length <= 0)
				return;
			Log.d(TAG, "read = " + values[0]);
			if ((b = machine.processJSON(values[0])) == null)
				return;
			updateInfo(values[0], b);
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
		}
	}

}
