package edu.upc.mcia.practicabluetoothmicros.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectionManager {

	// Constants
	public static final String TAG = "MANAGER";
	private final static UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int ACTION_SEARCHING = 99;
	public static final int ACTION_CONNECTING = 98;
	public static final int ACTION_CONNECTED = 97;
	public static final int ACTION_DISCONNECTED = 96;
	public static final int ACTION_RESPONSE_RIGHT = 95;
	public static final int ACTION_RESPONSE_WRONG = 94;
	public static final int ACTION_FROM_QUESTION = 93;

	// Bluetooth
	private BluetoothAdapter bluetoothAdapter;

	// Threads
	private ConnectThread connectThread;
	private CommunicationThread communicationThread;

	// Handlers & Events
	private BluetoothEventHandler handler;
	private static volatile boolean forceDisconect;

	public ConnectionManager(BluetoothAdapter bluetoothAdapter, BluetoothEventHandler.BluetoothEventListener listener) {
		forceDisconect = false;
		this.bluetoothAdapter = bluetoothAdapter;
		handler = new BluetoothEventHandler(listener);
	}

	public synchronized void turnOn() {
		handler.obtainMessage(ACTION_SEARCHING).sendToTarget();
		turnOff();
		for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
			if (device.getName().startsWith("RN42")) {
				// Si es troba un RN42, inicia Thread de connexio
				Log.d(TAG, "S'ha trobat el RN-42");
				connectThread = new ConnectThread(device);
				connectThread.start();
				return;
			}
		}
		Log.e(TAG, "NO ES TROBA EL RN-42!");
	}

	public synchronized void turnOff() {
		forceDisconect = true;
		if (communicationThread != null) {
			communicationThread.cancel();
			communicationThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
	}

	public synchronized void sendCommand(int command) throws Exception {
		communicationThread.write(command);
	}

	/* Thread per connectar el socket */
	private class ConnectThread extends Thread {

		public static final String TAG = "CONNECT";
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectThread(BluetoothDevice bluetoothDevice) {
			forceDisconect = false;
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
			while (!connexioEstablerta && !forceDisconect) {
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
		private final BufferedReader input;
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
			input = new BufferedReader(new InputStreamReader(tmpIn));
			output = tmpOut;
		}

		public void run() {
			Log.d(TAG, "-- Communication Thread started --");
			handler.obtainMessage(ACTION_CONNECTED).sendToTarget();
			String str;
			try {
				while (!forceDisconect) {
					str = input.readLine();
					Log.d(TAG, "@Rebut: " + str);
					processResponse(str);
				}
			} catch (Exception e) {
			}
			if (!forceDisconect) {
				Log.w(TAG, "S'ha perdut la connexio bluetooth!");
				handler.obtainMessage(ACTION_DISCONNECTED).sendToTarget();
			}
			Log.d(TAG, "-- Communication Thread closed --");
		}

		private void processResponse(String str) {
			try {
				String aux = str.toLowerCase();
				if (aux.startsWith("ack")) {// ignore?
				} else if (aux.startsWith("nack")) {// re-send send last command? better not
				} else if (aux.startsWith("right")) {
					handler.obtainMessage(ACTION_RESPONSE_RIGHT, str).sendToTarget();
				} else if (aux.startsWith("wrong")) {
					handler.obtainMessage(ACTION_RESPONSE_WRONG, str).sendToTarget();
				} else if (aux.startsWith("quest")) {
					handler.obtainMessage(ACTION_FROM_QUESTION, str).sendToTarget();
				}
			} catch (Exception ex) {
				Log.e(TAG, "Error processant: " + ex.getMessage());
			}
		}

		public void write(int data) throws Exception {
			output.write(0x00FF & data);
		}

		public void cancel() {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}
}
