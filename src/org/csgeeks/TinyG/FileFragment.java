package org.csgeeks.TinyG;

import java.io.File;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FileFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private FileFragmentListener mListener;
	private View mView;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (FileFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FileFragmentListener");
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	String filename;

    	// Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.gcodefile, container, false);
        
        if (savedInstanceState != null)
        	filename = savedInstanceState.getString("filename");
        else  {
        	// TODO pull this from preferences and save last filename on exit.
        	Log.d(TAG, "Using default filename");
        	filename = Environment.getExternalStorageDirectory().getPath() + "/test.nc";
        }
        
        EditText fv = (EditText) mView.findViewById(R.id.filename);        
		fv.setText(filename);
        return mView;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 1)
			return;
		if (resultCode == android.app.Activity.RESULT_OK && data != null) {
			String fileName = data.getData().getPath();
			if (fileName != null) {
				EditText mFilename = (EditText) mView.findViewById(R.id.filename);
				if (mFilename != null)
					mFilename.setText(fileName);
			}
		}
	}

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.filepick:
			pickFile();
			break;
		case R.id.start:
			EditText mFilename = (EditText) mView.findViewById(R.id.filename);
			mListener.toggleDownload(mFilename.getText().toString());
			break;
		}		
	}
	
	public interface FileFragmentListener {
		void toggleDownload(String filename);
	}
	
	private void pickFile() {
		EditText fv = (EditText) mView.findViewById(R.id.filename);
		String fileName = fv.getText().toString();

		// TODO write our own
		Intent intent = new Intent("org.openintents.action.PICK_FILE");

		// Construct URI from file name.
		File file = new File(fileName);
		intent.setData(Uri.fromFile(file));

		try {
			startActivityForResult(intent, 1);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(getActivity(), R.string.no_filemanager_installed,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void updateState(boolean download) {
		if (download)
			((Button) mView.findViewById(R.id.start)).setText(R.string.start);
		else
			((Button) mView.findViewById(R.id.start)).setText(R.string.stop);
	}
}
