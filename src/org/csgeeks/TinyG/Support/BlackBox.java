package org.csgeeks.TinyG.Support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.os.Environment;
import android.os.Process;
import android.util.Log;

public class BlackBox {
	private final Semaphore writeLock = new Semaphore(1, true);
	private File logFile;
	private BufferedOutputStream debugOut;
	private static final String TAG = "BlackBox";
	
	public BlackBox() {
		Log.d(TAG, "debug = " + Environment.getExternalStorageDirectory().getPath());
	    logFile = new File(Environment.getExternalStorageDirectory().getPath(), "tinyg-"+ Process.myPid() + ".txt");
	    try {
			debugOut = new BufferedOutputStream(new FileOutputStream(logFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void close() {
		try {
			debugOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(String dir, String cmd) {
		try {
			writeLock.acquire();
			String tmp = Long.toString(System.currentTimeMillis());
			debugOut.write(dir.getBytes(), 0, dir.length());
			debugOut.write(tmp.getBytes(), 0, tmp.length());
			debugOut.write(':');
			debugOut.write(cmd.getBytes(), 0, cmd.length());
			writeLock.release();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
		
}
