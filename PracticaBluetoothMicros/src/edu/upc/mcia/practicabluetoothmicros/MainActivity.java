package edu.upc.mcia.practicabluetoothmicros;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.BluetoothEventHandler;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.Command;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.ConnectionManager;

public class MainActivity extends Activity implements BluetoothEventHandler.BluetoothEventListener {

	// Constants
	private final static String TAG = "UI";
	private final static int ENABLE_BLUETOOTH_REQUEST = 30862;

	// Dialogs
	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;

	// Drawables
	private Drawable tickDrawable;
	private Drawable errorDrawable;

	// Bluetooth
	private BluetoothAdapter bluetoothAdapter;
	private ConnectionManager connectionManager;

	// Boolean indicators and controls
	private RadioButton[] indicators;
	private CheckBox[] controls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "-- onCreate --");

		// Create bluetooth connection manager
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		connectionManager = new ConnectionManager(bluetoothAdapter, this);

		// Read and scale drawables
		Bitmap bitmap;
		bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_tick_verd)).getBitmap();
		tickDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 75, 75, true));
		bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_creu_vermella)).getBitmap();
		errorDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 75, 75, true));

		// Get references to the indicators
		indicators = new RadioButton[4];
		indicators[0] = (RadioButton) findViewById(R.id.radio1);
		indicators[1] = (RadioButton) findViewById(R.id.radio2);
		indicators[2] = (RadioButton) findViewById(R.id.radio3);
		indicators[3] = (RadioButton) findViewById(R.id.radio4);

		// Get references to the controls
		controls = new CheckBox[4];
		controls[0] = (CheckBox) findViewById(R.id.check1);
		controls[1] = (CheckBox) findViewById(R.id.check2);
		controls[2] = (CheckBox) findViewById(R.id.check3);
		controls[3] = (CheckBox) findViewById(R.id.check4);

		// Link button to function
		((Button) findViewById(R.id.sendButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendCommandToModule();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "-- onStart --");
		intentaConnectarAmbLaPlaca();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Si resultat de request bluetooth i result_ok
		if (requestCode == ENABLE_BLUETOOTH_REQUEST) {
			if (resultCode == RESULT_OK) {
				intentaConnectarAmbLaPlaca();
			} else {
				Toast.makeText(this, "La App no funciona sense Bluetooth!", Toast.LENGTH_LONG).show();
				finish(); // Engega Bluetooth o tenca app
			}
		}
	}

	private void intentaConnectarAmbLaPlaca() {
		// Comprova si aquest terminal te Bluetooth
		if (bluetoothAdapter == null) {
			String missatgeError = "Aquest dispositiu no té Bluetooth!";
			Log.e(TAG, missatgeError);
			Toast.makeText(this, missatgeError, Toast.LENGTH_LONG).show();
			finish(); // tanca app
		} else {
			// Comprova si el Bluetooth esta engegat, sino demana-ho
			if (bluetoothAdapter.isEnabled()) {
				Log.d(TAG, "El Bluetooth esta habilitat!");
				progressDialog = new ProgressDialog(this);
				progressDialog.setTitle("Bluetooth");
				progressDialog.setMessage("Buscant mòdul Bluetooth...");
				progressDialog.setCancelable(false);
				progressDialog.setIndeterminate(true);
				progressDialog.show();
				connectionManager.turnOn();
			} else {
				Log.d(TAG, "Es necessari habilitar el Bluetooth!");
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "-- onStop --");
		connectionManager.turnOff();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "-- onDestroy --");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "-- onPause --");
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "-- onResume --");
	}

	@Override
	public void handleBluetoothEvent(Message msg) {
		switch (msg.what) {
		case ConnectionManager.ACTION_SEARCHING_DEVICE:
			progressDialog.setMessage("Buscant el mòdul Bluetooth...");
			break;
		case ConnectionManager.ACTION_SEARCHING_FAILED:
			Log.e(TAG, "Cal emparellar aquest dispositiu Android amb el mòdul Bluetooth!");
			progressDialog.dismiss();
			showErrorDialog();
			break;
		case ConnectionManager.ACTION_CONNECTING:
			String str = "Connectant...";
			progressDialog.setMessage((msg.arg1 > 1) ? str + String.format(" (intent %d)", msg.arg1) : str);
			break;
		case ConnectionManager.ACTION_CONNECTED:
			progressDialog.dismiss();
			showSuccessDialog();
			break;
		case ConnectionManager.ACTION_DISCONNECTED:
			intentaConnectarAmbLaPlaca();
			break;
		case ConnectionManager.ACTION_RECEPTION:
			processCommandFromModule((Command) msg.obj);
			break;
		}
	}

	private void showSuccessDialog() {
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Bluetooth connectat!");
		alertDialog.setCancelable(false);
		alertDialog.setIcon(tickDrawable);
		alertDialog.show();
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(2500);
					alertDialog.dismiss();
				} catch (InterruptedException ie) {
				}
			}
		}.start();
	}

	private void showErrorDialog() {
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Error Bluetooth");
		alertDialog.setCancelable(false);
		alertDialog.setMessage("Cal emparellar aquest dispositiu Android amb el mòdul Bluetooth!");
		alertDialog.setIcon(errorDrawable);
		alertDialog.show();
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(7000);
					alertDialog.dismiss();
					finish();
				} catch (InterruptedException ie) {
				}
			}
		}.start();
	}

	private void processCommandFromModule(Command command) {
		indicators[0].setChecked(command.bit0);
		indicators[1].setChecked(command.bit1);
		indicators[2].setChecked(command.bit2);
		indicators[3].setChecked(command.bit3);
	}

	private void sendCommandToModule() {
		Command command = new Command();
		command.bit0 = controls[0].isChecked();
		command.bit1 = controls[1].isChecked();
		command.bit2 = controls[2].isChecked();
		command.bit3 = controls[3].isChecked();
		try {
			Log.i(TAG, "Comanda: " + command);
			connectionManager.sendCommand(command);
		} catch (Exception e) {
			Log.e(TAG, "Error enviant comanda: " + e);
			Toast.makeText(this, "Error enviant comanda: " + command, Toast.LENGTH_SHORT).show();
		}
	}
}
