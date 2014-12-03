package edu.upc.mcia.practicabluetoothmicros.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import edu.upc.mcia.practicabluetoothmicros.bluetooth.BluetoothEventHandler.BluetoothEventListener;
import edu.upc.mcia.practicabluetoothmicros.command.BitsCommand;

public class ConnectionManager {

	// Constants
	public static final String TAG = "BT_MANAGER";
	private final static UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int ACTION_SEARCHING_DEVICE = 1;
	public static final int ACTION_SEARCHING_FAILED = 2;
	public static final int ACTION_CONNECTING = 3;
	public static final int ACTION_CONNECTED = 4;
	public static final int ACTION_DISCONNECTED = 5;
	public static final int ACTION_BITS_RECEPTION = 6;
	public static final int ACTION_ARRAY_RECEPTION = 7;

	// Bluetooth
	private BluetoothAdapter bluetoothAdapter;

	// Threads
	private ConnectThread connectThread;
	private CommunicationThread communicationThread;

	// Handlers & Events
	private BluetoothEventHandler handler;
	// private static volatile boolean forceDisconect;
	private AtomicBoolean forceDisconnect;

	public ConnectionManager(BluetoothAdapter bluetoothAdapter, BluetoothEventListener listener) {
		forceDisconnect = new AtomicBoolean(false);
		this.bluetoothAdapter = bluetoothAdapter;
		handler = new BluetoothEventHandler(listener);
	}

	public synchronized void turnOn() {
		handler.obtainMessage(ACTION_SEARCHING_DEVICE).sendToTarget();
		turnOff();
		for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
			// if (device.getName().startsWith("RN42") || device.getName().startsWith("RN-42") || device.getName().startsWith("RN")) {
			if (device.getName().startsWith("RN")) {
				// Si es troba un RN42, inicia Thread de connexio
				Log.d(TAG, "S'ha trobat el RN-42");
				connectThread = new ConnectThread(device);
				connectThread.start();
				return;
			}
		}
		Log.e(TAG, "NO ES TROBA EL RN-42!");
		handler.obtainMessage(ACTION_SEARCHING_FAILED).sendToTarget();
	}

	public synchronized void turnOff() {
		forceDisconnect.set(true);
		if (communicationThread != null) {
			communicationThread.cancel();
			communicationThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
	}

	public synchronized void sendCommand(BitsCommand command) throws Exception {
		communicationThread.write(command);
	}

	/* Thread per connectar el socket */
	private class ConnectThread extends Thread {

		public static final String TAG = "CONNECT";
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectThread(BluetoothDevice bluetoothDevice) {
			forceDisconnect.set(false);
			BluetoothSocket temp = null;
			device = bluetoothDevice;
			try {
				temp = device.createRfcommSocketToServiceRecord(UUID_SPP);
			} catch (IOException ioe) {
				Log.e(TAG, "Error en crear socket: " + ioe.toString());
			}
			socket = temp;
		}

		public void run() {
			int retryCount = 1;
			Boolean connexioEstablerta = false;
			if (socket == null) {
				Log.e(TAG, "Error: socket is null!");
				return;
			}
			Log.d(TAG, "-- Connect Thread started --");
			bluetoothAdapter.cancelDiscovery();
			handler.obtainMessage(ACTION_CONNECTING, retryCount, 0).sendToTarget();
			while (!connexioEstablerta && !forceDisconnect.get()) {
				try {
					socket.connect();
					connexioEstablerta = true;
				} catch (IOException ioe) {
					connexioEstablerta = false;
					retryCount++;
					handler.obtainMessage(ACTION_CONNECTING, retryCount, 0).sendToTarget();
					Log.e(TAG, "Error connectant: " + ioe.getMessage());
					try {
						Thread.sleep(1000); // Dorm una estona abans de tornar a intentar
					} catch (InterruptedException ie) {
					}
				}
			}
			if (connexioEstablerta) {
				Log.w(TAG, "S'ha establert la connexio!");
				communicationThread = new CommunicationThread(socket);
				communicationThread.start();
			}
			Log.d(TAG, "-- Connect Thread closed --");
		}

		public void cancel() {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}

	/* Thread per comunicar amb el modul */
	private class CommunicationThread extends Thread {
		public static final String TAG = "COMMUNICATE";
		private final BluetoothSocket socket;
		private final InputStream input;
		private final OutputStream output;

		public CommunicationThread(BluetoothSocket bluetoothSocket) {
			socket = bluetoothSocket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			input = tmpIn;
			output = tmpOut;
		}

		public void run() {
			Log.d(TAG, "-- Communication Thread started --");
			handler.obtainMessage(ACTION_CONNECTED).sendToTarget();
			int value;
			try {
				while (!forceDisconnect.get()) {
					value = input.read();
					Log.d(TAG, "@Rebut: 0x0" + Integer.toHexString(value).toUpperCase(Locale.ENGLISH));
					handler.obtainMessage(ACTION_BITS_RECEPTION, BitsCommand.decode(value)).sendToTarget();
				}
			} catch (Exception e) {
			}
			if (!forceDisconnect.get()) {
				Log.w(TAG, "S'ha perdut la connexio bluetooth!");
				handler.obtainMessage(ACTION_DISCONNECTED).sendToTarget();
			}
			Log.d(TAG, "-- Communication Thread closed --");
		}

		public void write(BitsCommand command) throws Exception {
			output.write(0x0F & command.encode());
		}

		public void cancel() {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}
}
