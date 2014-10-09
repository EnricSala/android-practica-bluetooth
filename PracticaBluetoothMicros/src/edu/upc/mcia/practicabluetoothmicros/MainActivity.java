package edu.upc.mcia.practicabluetoothmicros;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.BluetoothEventHandler;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.ConnectionManager;

public class MainActivity extends Activity implements BluetoothEventHandler.BluetoothEventListener {

	// Constants
	private final static String TAG = "UI";
	private final static int ENABLE_BLUETOOTH_REQUEST = 30862;

	// Dialogs
	private ProgressDialog progressDialog;
	private AlertDialog aboutDialog;

	// Drawables
	private Drawable tickDrawable;

	// Bluetooth
	private BluetoothAdapter bluetoothAdapter;
	private ConnectionManager connectionManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "-- onCreate --");

		// Create bluetooth conection manager
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		connectionManager = new ConnectionManager(bluetoothAdapter, this);

		// Read and scale drawables
		// Bitmap myBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_tick_verd)).getBitmap();
		// tickDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(myBitmap, 60, 50, true));
		tickDrawable = getResources().getDrawable(R.drawable.ic_tick_verd);
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
		// intentaConnectarAmbLaPlaca();
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
				progressDialog.setTitle("Buscant el mòdul Bluetooth...");
				progressDialog.setMessage("Establint connexió");
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
		if (aboutDialog != null) {
			aboutDialog.dismiss();
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
			String missatgeError = "Cal emparellar aquest dispositiu Android amb el mòdul Bluetooth!";
			Log.e(TAG, missatgeError);
			Toast.makeText(this, missatgeError, Toast.LENGTH_LONG).show();
			break;
		case ConnectionManager.ACTION_CONNECTING:
			String str = "Connectant...";
			progressDialog.setMessage((msg.arg1 > 1) ? str + String.format(" (intent %d)", msg.arg1) : str);
			break;
		case ConnectionManager.ACTION_CONNECTED:
			progressDialog.setMessage("Conexió establerta!");
			progressDialog.setIndeterminateDrawable(tickDrawable);
			progressDialog.setProgressDrawable(tickDrawable);
			new Thread() {
				@Override
				public void run() {
					try {
						sleep(1500);
						progressDialog.dismiss();
					} catch (InterruptedException ie) {
					}
				}
			}.start();
			break;
		case ConnectionManager.ACTION_DISCONNECTED:
			intentaConnectarAmbLaPlaca();
			break;
		case ConnectionManager.ACTION_RECEPTION:
			processCommandReception((Integer) msg.obj);
			break;
		}
	}

	private void processCommandReception(int i) {

	}
}
