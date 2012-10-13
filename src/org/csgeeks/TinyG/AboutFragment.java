package org.csgeeks.TinyG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class AboutFragment extends SherlockDialogFragment {
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.about).setTitle("About");
		return builder.create();
    }
    
}
