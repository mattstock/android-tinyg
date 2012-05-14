package org.csgeeks.TinyG;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.csgeeks.TinyG.Support.TinyGDriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TinyGNetwork extends TinyGDriver {
	private String tgfx_hostname;
	private int tgfx_port;
	private Socket socket;

	private static final String STATUS_REPORT = "{\"sr\":{";
	private static final String CMD_PAUSE = "!\n";
	private static final String CMD_RESUME = "~\n";

	public void disconnect() {
		super.disconnect();
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO
			}
		}
		socket = null;
	}

	protected void initialize() {
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));		
	}

	protected boolean background_connect() {
		try {
			socket = new Socket(tgfx_hostname, tgfx_port);
			os = socket.getOutputStream();
			is = socket.getInputStream();
		} catch (UnknownHostException e) {
			socket = null;
			Log.e(TAG,"Socket: " + e.getMessage());
			Toast.makeText(TinyGNetwork.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		} catch (IOException e) {
			socket = null;
			Log.e(TAG,"Socket: " + e.getMessage());
			Toast.makeText(TinyGNetwork.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
}

