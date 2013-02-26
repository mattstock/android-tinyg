package org.csgeeks.TinyG.USBAccessory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.csgeeks.TinyG.Support.TinyGService;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

@TargetApi(12)
public class USBAccessoryService extends TinyGService {
	private String TAG = "TinyG-USBAccessory";
	private BroadcastReceiver receiver;
	private UsbManager usbManager;
	private UsbAccessory mAccessory;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;
	private ParcelFileDescriptor mFileDescriptor;
	protected ListenerTask mListener;
	private static final String ACTION_USB_PERMISSION = "org.csgeeks.TinyG.Operator.action.USB_PERMISSION";

	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "USB service onCreate()");
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);		
		
		receiver = new UsbReceiver();
		// So we know when it's disconnected
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(receiver, filter);		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		Log.d(TAG, "onDestroy()");
	}
	
	public void write(String cmd) {
		Log.d(TAG, "Writing |" + cmd + "| to USB");
		try {
			mOutputStream.write(cmd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}

	@Override
	public void connect() {
		Log.d(TAG, "connect()");
		if (mInputStream != null || mOutputStream != null) {
			Log.d(TAG, "already connected");
			return;
		}

		UsbAccessory[] accessories = usbManager.getAccessoryList();
		mAccessory = (accessories == null ? null
				: accessories[0]);
		if (mAccessory == null) {
			Toast.makeText(this, "No TinyG USB accessory devices attached!", Toast.LENGTH_SHORT).show();
			return;
		}
		mFileDescriptor = usbManager.openAccessory(mAccessory);
		if (mFileDescriptor != null) {
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			if (fd.valid()) {
				mInputStream = new FileInputStream(fd);
				mOutputStream = new FileOutputStream(fd);
			} else {
				Log.d(TAG, "FD for accessory not valid!");
			}
		} else {
			Log.d(TAG,"openAccessory failed!");
		}

		// Watch and parse content from device
		mListener = new ListenerTask();
		mListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new InputStream[] { mInputStream });
	}

	public void disconnect() {
		super.disconnect();

		if (mListener != null)
			mListener.cancel(true);	

		try {
			if (mInputStream != null)
				mInputStream.close();
			if (mOutputStream != null)
				mOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "Close: " + e.getMessage());
		}
		mInputStream = null;
		mOutputStream = null;

		if (mFileDescriptor != null) {
			try {
				mFileDescriptor.close();
			} catch (IOException e) {
			}
		}
	}
	
	private class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory)
		                   intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					Log.i(TAG, "closing accessory");
				}
			}
		}
	};
	
	protected class ListenerTask extends AsyncTask<InputStream, String, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] buffer = new byte[1024];
			InputStream lis = params[0];
			int r_idx;
			int res = 0;
			int idx = 0;
			try {
				Log.d(TAG, "Starting read loop");
				while (!isCancelled()) {
					res = lis.read(buffer, idx, buffer.length-idx);
					if (res < 0) {
						break;
					}
					idx += res;
					r_idx = processLine(buffer, idx);
					Log.d(TAG, "Leftovers: " + r_idx + "bytes");
					if (r_idx > 0) {
						System.arraycopy(buffer, r_idx, buffer, 0, idx-r_idx);
						idx = idx-r_idx;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "listener read: " + e.getMessage());
			}
			return null;
		}

		private int processLine(byte[] buf, int len) {
			Log.d(TAG, "processing " + len + " bytes (" + new String(buf, 0, len) + ")");
			for (int i=0; i < len; i++) {
				if (buf[i] == '\n') {
					Log.d(TAG, "found newline at " + i);
					publishProgress(new String(buf, 0, i));
					return i+1;
				}
			}
			return 0;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Bundle b;
			if (values.length > 0) {
				if ((b = machine.processJSON(values[0])) != null) {
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
