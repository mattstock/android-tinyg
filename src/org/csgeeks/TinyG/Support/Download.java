package org.csgeeks.TinyG.Support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.csgeeks.TinyG.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Download {
	private static final String TAG = "TinyG";
	private int numLines;
	private Activity mActivity;
	private FileWriteTask mFileTask;
	private Dialog mDialog;
	private TinyGMessenger mTinyG;
	private static final Object synctoken = new Object();
	private boolean throttle;

	public Download(Activity a, TinyGMessenger m) {
		mActivity = a;
		mTinyG = m;
		throttle = false;
	}

	public Object getSyncToken() {
		return synctoken;
	}

	public boolean isThrottled() {
		return throttle;
	}

	public void setThrottle(boolean t) {
		throttle = t;
	}

	// Given a filename, start up the file writer task and provide status on
	// progress dialog
	@SuppressLint("NewApi")
	public void openFile(String string) {
		// Count lines
		try {
			BufferedReader in = new BufferedReader(new FileReader(string));
			numLines = 0;
			while (in.readLine() != null) {
				numLines++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(mActivity, "Invalid filename", Toast.LENGTH_SHORT)
					.show();
			return;
		} catch (IOException e) {
			Toast.makeText(mActivity, "Gcode file read error",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Log.d(TAG, "lines = " + numLines);

		mDialog = new Dialog(mActivity);
		mDialog.setContentView(R.layout.download_dialog);
		mDialog.setTitle("Download");
		((TextView) mDialog.findViewById(R.id.current)).setText("0");
		((TextView) mDialog.findViewById(R.id.filename)).setText(string);
		((TextView) mDialog.findViewById(R.id.total)).setText(Integer
				.toString(numLines));
		((ProgressBar) mDialog.findViewById(R.id.progressBar)).setProgress(0);

		// Now send them!

		mFileTask = new FileWriteTask();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			mFileTask.execute(string);
		else
			mFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, string);
	}

	public void cancel() {
		if (mFileTask != null)
			mFileTask.cancel(true);
	}

	protected class FileWriteTask extends AsyncTask<String, String, Void> {

		@Override
		protected void onPreExecute() {
			mActivity.findViewById(android.R.id.content).setKeepScreenOn(true);
			mDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			String filename = params[0];
			String line;
			int idx = 0;

			try {
				BufferedReader in = new BufferedReader(new FileReader(filename));
				while (!isCancelled() && (line = in.readLine()) != null) {
					idx++;
					try {
						synchronized (synctoken) {
							while (throttle)
								synctoken.wait();
						}
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// This is probably ok, just proceed.
					}
					publishProgress(line, Integer.toString(idx));
				}
				in.close();
			} catch (IOException e) {
				Log.e(TAG, "error reading file: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if (values.length > 1) {
				((ProgressBar) mDialog.findViewById(R.id.progressBar))
						.setProgress((int) (100 * (Double
								.parseDouble(values[1]) / numLines)));
				((TextView) mDialog.findViewById(R.id.line)).setText(values[0]);
				((TextView) mDialog.findViewById(R.id.current))
						.setText(values[1]);
				mTinyG.send_gcode(values[0]);
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "FileWriteTask cancelled");
			// TODO add a hard stop instruction to TinyG
			mDialog.dismiss();
			Toast.makeText(mActivity, "Download cancelled", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void v) {
			Log.i(TAG, "FileWriteTask complete");
			mDialog.dismiss();
		}

	}
}
