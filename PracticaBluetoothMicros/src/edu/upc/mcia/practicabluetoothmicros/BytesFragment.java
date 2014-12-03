package edu.upc.mcia.practicabluetoothmicros;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.upc.mcia.practicabluetoothmicros.command.BytesCommand;

public class BytesFragment extends Fragment {

	private OnBytesFragmentListener listener;

	public static BytesFragment newInstance() {
		BytesFragment fragment = new BytesFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public BytesFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_bytes, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnBytesFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public void onButtonPressed() {
		if (listener != null) {
			listener.onSendBytesCommand(new BytesCommand());
		}
	}

	public interface OnBytesFragmentListener {

		public void onSendBytesCommand(BytesCommand command);

	}

}
