package org.csgeeks.TinyG;

import com.actionbarsherlock.app.SherlockFragment;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FileFragment extends SherlockFragment {
	private static final String TAG = "TinyG";
	private FileFragmentListener parent;
	private Button startButton;
	private String filename;
	private EditText fileView;
	private SharedPreferences settings;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			parent = (FileFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FileFragmentListener");
		}
		settings = PreferenceManager.getDefaultSharedPreferences(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("filename", filename);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString("filename", filename);
		editor.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.gcodefile, container, false);

		if (savedInstanceState != null)
			filename = savedInstanceState.getString("filename");
		else {
			filename = settings.getString("filename", Environment
					.getExternalStorageDirectory().getPath() + "/test.nc");
		}

		fileView = (EditText) v.findViewById(R.id.filename);
		fileView.setText(filename);
		startButton = (Button) v.findViewById(R.id.start);
		return v;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 1)
			return;
		if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            String newname = data.getStringExtra(FileDialog.RESULT_PATH);
			if (newname != null) {
				filename = newname;
				fileView.setText(newname);
			}
		}
	}

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.filepick:
			pickFile();
			break;
		case R.id.start:
			parent.toggleDownload(filename);
			break;
		}
	}

	public interface FileFragmentListener {
		void toggleDownload(String filename);
	}

	private void pickFile() {
		Intent intent = new Intent(getActivity(), FileDialog.class);

		intent.putExtra(FileDialog.START_PATH, "/sdcard");
		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
		
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
			startButton.setText(R.string.stop_label);
		else
			startButton.setText(R.string.start);
	}
}
