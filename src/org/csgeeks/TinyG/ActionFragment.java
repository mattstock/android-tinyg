package org.csgeeks.TinyG;

// Copyright 2012 Matthew Stock

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ActionFragment extends ListFragment {
	ArrayList<String> actions;
	boolean mDualPane;
	int actionChoice = 0;
	ActionFragmentListener mListener;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		actions = new ArrayList<String>(Arrays.asList(getResources()
				.getStringArray(R.array.actionArray)));

		// Populate activity list
		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, actions));

		View displayFrame = getActivity().findViewById(R.id.displayF);
		mDualPane = displayFrame != null
				&& displayFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null)
			actionChoice = savedInstanceState.getInt("actionChoice", 0);

		if (mDualPane) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			showAction(actionChoice);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ActionFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ActionFragmentListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showAction(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("actionChoice", actionChoice);
	}

	private void showAction(int index) {
		actionChoice = index;

		getListView().setItemChecked(index, true);
		Fragment f = getFragmentManager().findFragmentById(R.id.displayF);

		switch (index) {
		case 0: // Jog
			if (mDualPane) {
				if (f == null || f.getClass() != JogFragment.class) {
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.displayF, new JogFragment());
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
			} else {
				Intent intent = new Intent();
				intent.setClass(getActivity(), JogActivity.class);
				startActivity(intent);
			}
			break;
		case 1: // File
			if (mDualPane) {
				if (f == null || f.getClass() != FileFragment.class) {
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.displayF, new FileFragment());
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
			} else {
				Intent intent = new Intent();
				intent.setClass(getActivity(), FileActivity.class);
				intent.putExtra("connection", mListener.connectionState());
				startActivity(intent);
			}
			break;
		case 2: // Path
			break;
		case 3: // System
			/*
			 * if (mDualPane) { if (f == null || f.getClass() !=
			 * MachineFragment.class) { FragmentTransaction ft =
			 * getFragmentManager().beginTransaction();
			 * ft.replace(R.id.displayF, new MachineFragment());
			 * ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			 * ft.commit(); } } else { Intent intent = new Intent();
			 * intent.setClass(getActivity(), MachineActivity.class);
			 * startActivity(intent); }
			 */
			break;
		case 4: // Motor
			if (mDualPane) {
				if (f == null || f.getClass() != MotorFragment.class) {
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.displayF, new MotorFragment());
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
			} else {
				Intent intent = new Intent();
				intent.setClass(getActivity(), MotorActivity.class);
				startActivity(intent);
			}
			break;
		case 5: // Axis
			if (mDualPane) {
				if (f == null || f.getClass() != AxisFragment.class) {
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.displayF, new AxisFragment());
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
			} else {
				Intent intent = new Intent();
				intent.setClass(getActivity(), AxisActivity.class);
				startActivity(intent);
			}
			break;
		}
	}

	public interface ActionFragmentListener {
		public boolean connectionState();
	}
}
