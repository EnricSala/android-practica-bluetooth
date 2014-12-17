package edu.upc.mcia.practicabluetoothmicros.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothCommService extends Service {

	private static final String TAG = "BL-Service";

	private final IBinder binder = new BluetoothCommServiceBinder();

	public BluetoothCommService() {
	}

	public class BluetoothCommServiceBinder extends Binder {
		public BluetoothCommService getService() {
			return BluetoothCommService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "-- onCreate BL-Service --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "-- onDestroy BL-Service --");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "-- onStart BL-Service --");
		return START_STICKY;
	}

}
