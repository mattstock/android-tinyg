package org.csgeeks.TinyG;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FileFragment extends Fragment {
	private static final String TAG = "TinyG";
	private Activity mActivity;
	private EditText mFilename;
	private FileClickListener mClickListener;
	private boolean active_file = false;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		mClickListener = new FileClickListener(); 
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.gcodefile, container, false);
        
        mFilename = (EditText) v.findViewById(R.id.filename);
        // TODO find and load the last filename used from preferences
        mFilename.setText("/sdcard/gcode.txt");
        Button b = (Button) v.findViewById(R.id.filepick);
        b.setOnClickListener(mClickListener);
        b = (Button) v.findViewById(R.id.start);
        b.setOnClickListener(mClickListener);
        b = (Button) v.findViewById(R.id.pause);
        b.setOnClickListener(mClickListener);
        b = (Button) v.findViewById(R.id.stop);
        b.setOnClickListener(mClickListener);
        
        return v;
    }
    
    private class FileClickListener implements OnClickListener {
		public void onClick(View view) {
			Log.d(TAG, "In click handler");
			switch (view.getId()) {
			case R.id.filepick:
				openFile();
				break;
			case R.id.start:
				break;
			case R.id.pause:
				break;
			case R.id.stop:
				break;
			}
		}        	    	
    }
    
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	if (requestCode != 1)
    		return;
		if (resultCode == android.app.Activity.RESULT_OK && data != null) {
			String fileName = data.getData().getPath();
			if (fileName != null) {
				mFilename.setText(fileName);
				active_file = loadFile(fileName);
			}
		}
    }
    
    private boolean loadFile(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	private void openFile() {
		String fileName = mFilename.getText().toString();
		
		Intent intent = new Intent("org.openintents.action.PICK_FILE");
		
		// Construct URI from file name.
		File file = new File(fileName);
		intent.setData(Uri.fromFile(file));
				
		try {
			startActivityForResult(intent, 1);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(mActivity, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}

}
