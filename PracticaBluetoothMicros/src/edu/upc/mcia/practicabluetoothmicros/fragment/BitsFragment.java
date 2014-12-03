package edu.upc.mcia.practicabluetoothmicros.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import edu.upc.mcia.practicabluetoothmicros.R;
import edu.upc.mcia.practicabluetoothmicros.command.BitsCommand;

public class BitsFragment extends Fragment {

	private OnBitsFragmentListener listener;

	// Boolean indicators and controls
	private RadioButton[] indicators;
	private CheckBox[] controls;

	public static BitsFragment newInstance() {
		BitsFragment fragment = new BitsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public BitsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			// mParam1 = getArguments().getString(ARG_PARAM1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_bits, container, false);

		// Get references to the indicators
		indicators = new RadioButton[4];
		indicators[0] = (RadioButton) view.findViewById(R.id.radio1);
		indicators[1] = (RadioButton) view.findViewById(R.id.radio2);
		indicators[2] = (RadioButton) view.findViewById(R.id.radio3);
		indicators[3] = (RadioButton) view.findViewById(R.id.radio4);

		// Get references to the controls
		controls = new CheckBox[4];
		controls[0] = (CheckBox) view.findViewById(R.id.check1);
		controls[1] = (CheckBox) view.findViewById(R.id.check2);
		controls[2] = (CheckBox) view.findViewById(R.id.check3);
		controls[3] = (CheckBox) view.findViewById(R.id.check4);

		// Link button to function
		((Button) view.findViewById(R.id.sendButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onButtonPressed();
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnBitsFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement the Listener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	private void onButtonPressed() {
		if (listener != null) {
			BitsCommand command = new BitsCommand();
			command.bit0 = controls[0].isChecked();
			command.bit1 = controls[1].isChecked();
			command.bit2 = controls[2].isChecked();
			command.bit3 = controls[3].isChecked();
			listener.onSendBitsCommand(command);
		}
	}

	public void displayReceivedCommand(BitsCommand command) {
		indicators[0].setChecked(command.bit0);
		indicators[1].setChecked(command.bit1);
		indicators[2].setChecked(command.bit2);
		indicators[3].setChecked(command.bit3);
	}

	public interface OnBitsFragmentListener {

		public void onSendBitsCommand(BitsCommand command);

	}

}
