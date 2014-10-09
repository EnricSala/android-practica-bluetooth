package edu.upc.mcia.practicabluetoothmicros.bluetooth;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class BluetoothEventHandler extends Handler {

	private final WeakReference<BluetoothEventListener> listenerWeakRef;

	public BluetoothEventHandler(BluetoothEventListener listener) {
		this.listenerWeakRef = new WeakReference<BluetoothEventListener>(listener);
	}

	@Override
	public void handleMessage(Message msg) {
		BluetoothEventListener listener = listenerWeakRef.get();
		if (listener != null) {
			listener.handleBluetoothEvent(msg);
		}
	}

	public interface BluetoothEventListener {
		public void handleBluetoothEvent(Message msg);
	}
}
