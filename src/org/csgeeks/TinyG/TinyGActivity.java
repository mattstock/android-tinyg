package org.csgeeks.TinyG;

import java.io.IOException;
import java.io.InputStream;

import org.csgeeks.TinyG.driver.NetworkDriver;
import org.csgeeks.TinyG.driver.RetCode;
import org.csgeeks.TinyG.driver.TinyGDriver;
import org.csgeeks.TinyG.system.Machine;
import org.csgeeks.TinyG.system.Machine.unit_modes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TinyGActivity extends FragmentActivity {
	private static final String TAG = "TinyG";
    private boolean mIsBound;
    private TinyGDriver tinyg;
	private Context mContext;
	private Button mConnect;
	private float jogRate = 10;
	private SharedPreferences settings;
    private ServiceConnection mConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();
		settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		mConnect = (Button) findViewById(R.id.connect);
		mConnection = new NetworkServiceConnection();
        bindService(new Intent(TinyGActivity.this, 
                TinyGDriver.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, ShowSettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat("jogRate", jogRate);
	}

	private void restoreState(Bundle inState) {
		jogRate = inState.getFloat("jogRate");
	}
	
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.connect:
			if (mIsBound) {
				tinyg.disconnect();
				((Button) view).setText(R.string.connect);
			} else {
				tinyg.connect("127.0.0.1", 4444);
				((Button) view).setText(R.string.disconnect);
			}
			break;
		}
		// If we're ready, handle buttons that will send messages to TinyG
		if (mIsBound && tinyg.isReady()) {
			switch (view.getId()) {
			case R.id.xpos:
				tinyg.write("{\"gc\": \"g91g0x" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.xneg:
				tinyg.write("{\"gc\": \"g91g0x" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.ypos:
				tinyg.write("{\"gc\": \"g91g0y" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.yneg:
				tinyg.write("{\"gc\": \"g91g0y" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.zpos:
				tinyg.write("{\"gc\": \"g91g0z" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.zneg:
				tinyg.write("{\"gc\": \"g91g0z" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.apos:
				tinyg.write("{\"gc\": \"g91g0a" + Double.toString(jogRate)
						+ "\"}\n");
				break;
			case R.id.aneg:
				tinyg.write("{\"gc\": \"g91g0a" + Double.toString(-jogRate)
						+ "\"}\n");
				break;
			case R.id.rpos:
				jogRate += 1;
				break;
			case R.id.rneg:
				jogRate -= 1;
				break;
			case R.id.units:
				switch (tinyg.getMachine().getUnitMode()) {
				case MM:
					tinyg.write(TinyGDriver.CMD_SET_UNIT_INCHES);
					// A hack
					tinyg.getMachine().setUnits(unit_modes.INCHES);
					break;
				case INCHES:
					tinyg.write(TinyGDriver.CMD_SET_UNIT_MM);
					// A hack
					tinyg.getMachine().setUnits(unit_modes.MM);
					break;
				}
				break;
			case R.id.zero:
				tinyg.write(TinyGDriver.CMD_ZERO_ALL_AXIS);
				// This is a bit of a hack.
				Machine machine = tinyg.getMachine();
				machine.getAxisByName("X").setWork_position(0);
				machine.getAxisByName("Y").setWork_position(0);
				machine.getAxisByName("Z").setWork_position(0);
				machine.getAxisByName("A").setWork_position(0);
				break;
			}
			updateState(tinyg.getMachine());
		}
	}


	private class NetworkServiceConnection implements ServiceConnection {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        tinyg = ((TinyGDriver.NetworkBinder)service).getService();
	        
	        // Tell the user about this for our demo.
	        Toast.makeText(TinyGActivity.this, R.string.local_service_connected,
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        tinyg = null;
	        Toast.makeText(TinyGActivity.this, R.string.local_service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	}


	public void updateState(Machine machine) {
		((TextView) findViewById(R.id.xloc)).setText(Float.toString(machine
				.getAxisByName("X").getWork_position()));
		((TextView) findViewById(R.id.yloc)).setText(Float.toString(machine
				.getAxisByName("Y").getWork_position()));
		((TextView) findViewById(R.id.zloc)).setText(Float.toString(machine
				.getAxisByName("Z").getWork_position()));
		((TextView) findViewById(R.id.aloc)).setText(Float.toString(machine
				.getAxisByName("A").getWork_position()));
		((TextView) findViewById(R.id.jogval)).setText(Float.toString(jogRate));
		((TextView) findViewById(R.id.line)).setText(Integer.toString(machine.getLine_number()));
		switch (machine.getMotionMode()) {
		case traverse:
			((TextView) findViewById(R.id.momo)).setText(R.string.traverse);
			break;
		case straight:
			((TextView) findViewById(R.id.momo)).setText(R.string.straight);
			break;
		case cw_arc:
			((TextView) findViewById(R.id.momo)).setText(R.string.cw);
			break;
		case ccw_arc:
			((TextView) findViewById(R.id.momo)).setText(R.string.ccw);
			break;
		case invalid:
			((TextView) findViewById(R.id.momo)).setText(R.string.invalid);
			break;
		}
		switch (machine.getMachineState()) {
		case reset:
			((TextView) findViewById(R.id.status)).setText(R.string.reset);
			break;
		case nop:
			((TextView) findViewById(R.id.status)).setText(R.string.nop);
			break;
		case stop:
			((TextView) findViewById(R.id.status)).setText(R.string.stop);
			break;
		case end:
			((TextView) findViewById(R.id.status)).setText(R.string.end);
			break;
		case run:
			((TextView) findViewById(R.id.status)).setText(R.string.run);
			break;
		case hold:
			((TextView) findViewById(R.id.status)).setText(R.string.hold);
			break;
		case homing:
			((TextView) findViewById(R.id.status)).setText(R.string.homing);
			break;
		}
		switch (machine.getUnitMode()) {
		case INCHES:
			((Button) findViewById(R.id.units)).setText(R.string.inch);
		case MM:
			((Button) findViewById(R.id.units)).setText(R.string.mm);
		}
		((TextView) findViewById(R.id.velocity)).setText(Float.toString(machine.getVelocity()));
	}
}