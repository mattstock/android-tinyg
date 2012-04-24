package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;

import org.csgeeks.TinyG.driver.NetworkDriver;
import org.csgeeks.TinyG.driver.RetCode;
import org.csgeeks.TinyG.driver.TinygDriver;
import org.csgeeks.TinyG.driver.UsbDriver;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TinyGActivity extends Activity {
	private static final String TAG = "TinyG";
	private enum conntype { NET, USB };
	private conntype connectionType = conntype.NET;
	private TinygDriver tinyg;	
	private Context mContext;
	private ListenerTask mListener;
	private View mDisconnect, mConnect;
	private Machine machine;
	private Double r = 10.0;
	private Double x,y,z;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.main);
        mContext = getApplicationContext();
        mDisconnect = findViewById(R.id.disconnect);
        mConnect = findViewById(R.id.connect);
        machine = new Machine();
    }
        
	@Override
    public void onDestroy() {
    	super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {       
		case R.id.usb:
			connectionType = conntype.USB;
			return true;        
		case R.id.network:
			connectionType = conntype.NET;
			return true;        
		default:            
			return super.onOptionsItemSelected(item);    
		}
	}
	
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.xpos:
			x += r;
			tinyg.write("{\"gc\": \"g0x" + x.toString() + "\"}");
			break;
		case R.id.xneg:
			x -= r;
			tinyg.write("{\"gc\": \"g0x" + x.toString() + "\"}");
			break;
		case R.id.ypos:
			y += r;
			tinyg.write("{\"gc\": \"g0y" + x.toString() + "\"}");
			break;
		case R.id.yneg:
			y -= r;
			tinyg.write("{\"gc\": \"g0y" + x.toString() + "\"}");
			break;
		case R.id.zpos:
			z += r;
			tinyg.write("{\"gc\": \"g0z" + x.toString() + "\"}");
			break;
		case R.id.zneg:
			z -= r;
			tinyg.write("{\"gc\": \"g0z" + x.toString() + "\"}");
			break;
		case R.id.rpos:
			r += 1;
			break;
		case R.id.rneg:
			r -= 1;
			break;
		case R.id.zero:
			tinyg.write(Machine.CMD_ZERO_ALL_AXIS);
			break;
		case R.id.connect:
			switch (connectionType) {
			case NET:
				if (tinyg == null) {
					tinyg = new NetworkDriver("192.168.1.15",4444);
				} else {
					tinyg.disconnect();
				}
				new ConnectTask().execute(0);
		   		Toast.makeText(mContext, "Connecting...", Toast.LENGTH_SHORT).show();
				view.setVisibility(View.INVISIBLE);
				break;
			case USB:
				if (tinyg == null) {
					tinyg = new UsbDriver("path");
				} else {
					tinyg.disconnect();
				}
				tinyg.connect();
				view.setVisibility(View.INVISIBLE);
				break;
			}
			break;
		case R.id.disconnect:
			if (tinyg != null) {
				Log.d(TAG,"telling listener to cancel");
				mListener.cancel(true);
				Log.d(TAG,"sending disconnect command to tinyg");
				tinyg.disconnect();
				Log.d(TAG,"done with disconnect");
			}
			view.setVisibility(View.INVISIBLE);
		}
	}
	
	private class ConnectTask extends AsyncTask<Integer, Integer, RetCode> {
		@Override
		protected RetCode doInBackground(Integer... params) {
			Log.d(TAG, "Starting connect in background");
	    	RetCode res = tinyg.connect();
	    	Log.d(TAG, "Returned from connect");
			return res;
		}
		
		protected void onPostExecute(RetCode res) {
	   		if (res.result) {
	   			Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
		   		
	   			mDisconnect.setVisibility(View.VISIBLE);
	   			mListener = new ListenerTask();
 				mListener.execute(new InputStream[] {tinyg.getInputStream()});
 				Log.i(TAG,"Listener started");
 				tinyg.write(Machine.CMD_DISABLE_LOCAL_ECHO);
 				tinyg.write(Machine.CMD_GET_OK_PROMPT);
 				tinyg.write(Machine.CMD_SET_STATUS_UPDATE_INTERVAL);
 				tinyg.write(Machine.CMD_GET_OK_PROMPT);
 				tinyg.write(Machine.CMD_GET_STATUS_REPORT);
 				tinyg.write(Machine.CMD_GET_OK_PROMPT);
	   		} else {
				CharSequence c = res.message;
	   			Toast.makeText(mContext, c, Toast.LENGTH_SHORT).show();
	   			mConnect.setVisibility(View.VISIBLE);
	   		}
		}
	}
	
	private class ListenerTask extends AsyncTask<InputStream, String, Void> {

		@Override
		protected Void doInBackground(InputStream... params) {
			byte[] buffer = new byte[1024];
			InputStream is = params[0];
			int b;
			int idx=0;
			try {
				while (!isCancelled()) {
					if ((b = is.read()) == -1) {
						break;
					}
					buffer[idx++] = (byte) b;
					if (b == '\n') {
						publishProgress(new String(buffer,0,idx));
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
                Log.i(TAG, "onProgressUpdate: " + values[0].length() + " bytes received.");
                if (machine.processJSON(values[0])) {
                	((TextView)findViewById(R.id.xloc)).setText(Double.toString(machine.getX()));
                	((TextView)findViewById(R.id.yloc)).setText(Double.toString(machine.getY()));
                	((TextView)findViewById(R.id.zloc)).setText(Double.toString(machine.getZ()));
                }
            }
        }
		
        @Override
        protected void onCancelled() {
            Log.i(TAG, "ListenerTask cancelled");
            mConnect.setVisibility(View.VISIBLE);
        }
	}
}