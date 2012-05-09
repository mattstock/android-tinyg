package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.csgeeks.TinyG.Support.Machine;
import org.csgeeks.TinyG.Support.Parser;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TinyGDriver extends Service {
	private Machine machine;
	private ListenerTask mListener;
	private String tgfx_hostname;
	private int tgfx_port;
	private boolean ready;
	private InputStream is;
	private OutputStream os;
	private Socket socket;

	private static final String TAG = "TinyG";
	public static final String TINYG_UPDATE = "org.csgeeks.TinyG.UPDATE";
	private static final String STATUS_REPORT = "{\"sr\":{";
	private static final String CMD_PAUSE = "!\n";
	private static final String CMD_RESUME = "~\n";

    public class NetworkBinder extends Binder {
        public TinyGDriver getService() {
            return TinyGDriver.this;
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
		machine = new Machine();
		ready = false;
    }

    @Override
    public void onDestroy() {
        disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new NetworkBinder();

	public Machine getMachine() {
		return machine;
	}


	public boolean isReady() {
		return ready;
	}

	public void disconnect() {
		if (mListener != null) {
			mListener.cancel(true);	
		}
		if (socket != null) {
			try {
				Log.d(TAG,"closing socket");
				socket.close();
				is.close();
				os.close();
			} catch (IOException e) {
				Log.e(TAG, "Close: " + e.getMessage());
			}
			is = null;
			os = null;
			socket = null;
			ready = false;
		}
		Log.d(TAG,"disconnect done");
	}

	public void write(String message) {
		try {
			os.write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "write: " + e.getMessage());
		}
	}

	public void connect() {
		if (ready) {
			return;
		}
		Context mContext = getApplicationContext();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		tgfx_hostname = settings.getString("tgfx_hostname", "127.0.0.1");
		tgfx_port = Integer.parseInt(settings.getString("tgfx_port", "4444"));
		
		new ConnectTask().execute(0);
		Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
	}
	
	private class ConnectTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect to " + tgfx_hostname + " in background");
			try {
				socket = new Socket(tgfx_hostname, tgfx_port);
				os = socket.getOutputStream();
				is = socket.getInputStream();
			} catch (UnknownHostException e) {
				socket = null;
				Log.e(TAG,"Socket: " + e.getMessage());
				Toast.makeText(TinyGDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				return false;
			} catch (IOException e) {
				socket = null;
				Log.e(TAG,"Socket: " + e.getMessage());
				Toast.makeText(TinyGDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean res) {
			if (res) {
				Toast.makeText(TinyGDriver.this, "Connected", Toast.LENGTH_SHORT)
						.show();
				mListener = new ListenerTask();
				mListener.execute(new InputStream[] { is });
				ready = true;
				Log.i(TAG, "Listener started");
				refresh();
			}
		}
	}

	private class ListenerTask extends AsyncTask<InputStream, String, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] buffer = new byte[1024];
			InputStream is = params[0];
			int b;
			int idx = 0;
			try {
				while (!isCancelled()) {
					if ((b = is.read()) == -1) {
						break;
					}
					buffer[idx++] = (byte) b;
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
			if (values.length > 0) {
				Log.i(TAG, "onProgressUpdate: " + values[0]);
				if (Parser.processJSON(values[0], machine)) {
					Intent intent = new Intent(TINYG_UPDATE);
					sendBroadcast(intent);
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "ListenerTask cancelled");
		}
	}

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

