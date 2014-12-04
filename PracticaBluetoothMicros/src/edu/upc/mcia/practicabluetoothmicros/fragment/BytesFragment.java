package edu.upc.mcia.practicabluetoothmicros.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.upc.mcia.practicabluetoothmicros.R;
import edu.upc.mcia.practicabluetoothmicros.command.BytesCommand;

public class BytesFragment extends Fragment {

	// Constant
	private static final int INITIAL_INDICATORS = 5;
	private static final int INITIAL_CONTROLS = 5;

	// Listener
	private OnBytesFragmentListener listener;

	// Byte indicators and controls
	private LinearLayout indicators;
	private LinearLayout controls;

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
		View view = inflater.inflate(R.layout.fragment_bytes, container, false);

		// Get references to the indicators
		indicators = (LinearLayout) view.findViewById(R.id.linearBytesDisplay);
		indicators.removeAllViews();
		for (int i = 0; i < INITIAL_INDICATORS; i++) {
			TextView text = new TextView(getActivity());
			text.setText("-");
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			indicators.addView(text);
		}

		// Get references to the controls
		controls = (LinearLayout) view.findViewById(R.id.linearBytesControl);
		controls.removeAllViews();
		TextWatcher controlWatcher = new ByteControlWatcher();
		for (int i = 0; i < INITIAL_CONTROLS; i++) {
			EditText text = new EditText(getActivity());
			text.setText("0");
			text.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			text.addTextChangedListener(controlWatcher);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			controls.addView(text);
		}

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnBytesFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBytesFragmentListener");
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

	public void displayReceivedCommand(BytesCommand command) {

	}

	public interface OnBytesFragmentListener {

		public void onSendBytesCommand(BytesCommand command);

	}

	private static class ByteControlWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			try {
				int input = Integer.parseInt(s.toString());
				if (input < 0) {
					s.clear();
					s.append("0");
				} else if (input > 255) {
					s.clear();
					s.append("255");
				} else {
					// is valid
				}
			} catch (NumberFormatException nfe) {
			}
		}
	}
}
