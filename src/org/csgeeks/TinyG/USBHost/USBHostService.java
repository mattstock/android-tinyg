package org.csgeeks.TinyG.USBHost;

// Copyright 2012 Matthew Stock

import org.csgeeks.TinyG.Support.TinyGService;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

// This is only a bound service at the moment.
@TargetApi(12)
public class USBHostService extends TinyGService {
	private static final String TAG = "TinyG-USBHost";
	private static final String ACTION_USB_PERMISSION = "org.csgeeks.TinyG.USB_PERMISSION";
	private static final int USB_BUFFER_SIZE = 16*1024;

	// USB IDs for the TinyG hardware.
	// TODO make them parameters.
	private BroadcastReceiver mUsbReceiver = new UsbReceiver();
	private UsbDevice deviceFTDI;
	private UsbManager mUsbManager;
	private UsbDeviceConnection conn;
	private UsbEndpoint epIN;
	private UsbEndpoint epOUT;
	protected ListenerTask mListener;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "USB service onCreate()");
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		deviceFTDI = USBHostSupport.loadFTDI(mUsbManager);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbReceiver);
		Log.d(TAG, "onDestroy()");
	}

	// When we get permission, open up the FTDI/TinyG device and
	// initialize the endpoints.
	public class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.d(TAG, "USB attach");
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(TAG, "USB detach");
			}
			if (action.equals(ACTION_USB_PERMISSION)) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					// TODO Make sure we're talking about the same device

					// Do all of the setup and call the listener AsyncTask
					conn = mUsbManager.openDevice(deviceFTDI);
					if (!conn.claimInterface(deviceFTDI.getInterface(0), true)) {
						Toast.makeText(USBHostService.this,
								"TinyG USB device locked", Toast.LENGTH_SHORT)
								.show();
						Log.e(TAG, "Can't claim USB interface");
					}
					// Configure for TinyG serial settings - 115200, 8N1
					USBHostSupport.ftdi_settings(conn);
					epIN = null;
					epOUT = null;

					UsbInterface usbIf = deviceFTDI.getInterface(0);
					for (int i = 0; i < usbIf.getEndpointCount(); i++) {
						if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
							if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
								epIN = usbIf.getEndpoint(i);
							else
								epOUT = usbIf.getEndpoint(i);
						}
					}
					Log.d(TAG, "Got endpoints");
					mListener = new ListenerTask();
					mListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					// Let everyone know we are connected
					Bundle b = new Bundle();
					b.putBoolean("connection", true);
					Intent i = new Intent(CONNECTION_STATUS);
					i.putExtras(b);
					sendBroadcast(i, null);
					refresh();
				} else {
					Toast.makeText(USBHostService.this,
							"USB permission denied", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void disconnect() {
		super.disconnect();
		if (mListener != null) {
			mListener.cancel(true);
		}
		if (deviceFTDI != null && conn != null) {
			conn.releaseInterface(deviceFTDI.getInterface(0));
			conn.close();
		}
	}

	@Override
	public void write(String s) {
		byte b[] = s.getBytes();
		int from_idx, to_idx, res;
		byte tmp[] = new byte[64];

		from_idx = 0;
		while (from_idx < b.length) {
			to_idx = 0;
			while (to_idx < 64 && from_idx < b.length) {
				tmp[to_idx++] = b[from_idx++];
			}

			if ((res = conn.bulkTransfer(epOUT, tmp, to_idx, 500)) != to_idx) {
				Log.e(TAG, "USB send failed with code " + res);
			}
		}
	}

	// Connect actually invokes a permissions check. The actual connection
	// work is done in the BroadcastReceiver above.
	@Override
	public void connect() {
		if (deviceFTDI == null) {
			Toast.makeText(this, "No TinyG USB host devices attached!",
					Toast.LENGTH_SHORT).show();
		} else {
			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this,
					0, new Intent(ACTION_USB_PERMISSION), 0);
			mUsbManager.requestPermission(deviceFTDI, mPermissionIntent);
		}
	}

	// Handle data from the USB endpoint in a separate thread.
	protected class ListenerTask extends AsyncTask<Integer, String, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			byte[] inbuffer = new byte[USB_BUFFER_SIZE];
			byte[] linebuffer = new byte[1024];
			int cnt, idx = 0;
			try {
				while (!isCancelled()) {
					if ((cnt = conn.bulkTransfer(epIN, inbuffer, USB_BUFFER_SIZE, 0)) < 2) {
						Log.e(TAG, "Bulk read failed");
						break;
					}
					for (int i = 0; i < cnt; i++) {
						if (i % 64 == 0) { // Skip the two FTDI bytes that are spaced every 64 bytes
							i++;
							continue;
						}		
						if (inbuffer[i] == '\n') {
							publishProgress(new String(linebuffer, 0, idx));
							idx = 0;
						} else
							linebuffer[idx++] = inbuffer[i];
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "listener read exception: " + e.getMessage());
			}
			return null;
		}

		// When we receive a full line of input, parse it and send
		// a broadcast to notify activities if needed.
		@Override
		protected void onProgressUpdate(String... values) {
			Bundle b;
			if (values.length <= 0)
				return;
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
