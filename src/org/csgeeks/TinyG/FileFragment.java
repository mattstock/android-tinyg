package org.csgeeks.TinyG;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class FileFragment extends Fragment {
	private static final String TAG = "TinyG";
	private EditText mFilename;
	private String filename;
		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.gcodefile, container, false);
        
        mFilename = (EditText) v.findViewById(R.id.filename);
        
        if (savedInstanceState != null)
        	filename = savedInstanceState.getString("filename");
        else 
        	filename = "/sdcard/test.nc";
    
		mFilename.setText(filename);
        return v;
    }
       
}
