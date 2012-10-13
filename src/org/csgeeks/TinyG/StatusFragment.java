package org.csgeeks.TinyG;

import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	View v;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	Log.d(TAG, "Inflating StatusFragment");
        // Inflate the layout for this fragment
    	v = inflater.inflate(R.layout.status, container, false);
    	return v;
    }
    
	public void updateState(Bundle b) {
		((TextView) v.findViewById(R.id.jogval)).setText(Float
				.toString(b.getFloat("jogRate")));
		((TextView) v.findViewById(R.id.loc)).setText(
				String.format("( %.3f, %.3f, %.3f, %.3f)",
						b.getFloat("posx"), b.getFloat("posy"),
						b.getFloat("posz"), b.getFloat("posa")));
		((TextView) v.findViewById(R.id.line)).setText(Integer.toString(b
				.getInt("line")));
		((TextView) v.findViewById(R.id.momo)).setText(b.getString("momo"));
		((TextView) v.findViewById(R.id.status)).setText(b.getString("status"));
		((TextView) v.findViewById(R.id.velocity)).setText(Float.toString(b
				.getFloat("velocity")));
	}

}
