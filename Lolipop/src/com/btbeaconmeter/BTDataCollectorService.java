package com.btbeaconmeter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;

public class BTDataCollectorService extends Service {

	// private LeScanCallback mLeScanCallback;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeScanner scanner;
	private ScanCallback mScanCallback;
	private ScanSettings settings;
	public static String BTData = "";
	public String FOLDER_NAME = "BT";
	public static ArrayList<BTBeacon> BTBeaconArray = new ArrayList<BTBeacon>();
	public CSVWriter writer;
	private final Handler handler = new Handler();
	private int numIntent;
	public String fullBTDataRecord = "";
	public String filename = "default";
	
	int i= 0;
	// It's the code we want our Handler to execute to send data
	private Runnable sendData = new Runnable() {
		// the specific method which will be executed by the handler
		public void run() {
			numIntent++;

			// sendIntent is the object that will be broadcast outside our app
			Intent sendIntent = new Intent();

			// We add flags for example to work from background
			sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
					| Intent.FLAG_FROM_BACKGROUND
					| Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

			// SetAction uses a string which is an important name as it
			// identifies the sender of the itent and that we will give to the
			// receiver to know what to listen.
			// By convention, it's suggested to use the current package name
			sendIntent.setAction("com.test.sendintent.IntentToUnity");

			// Here we fill the Intent with our data, here just a string with an
			// incremented number in it.

			// String s = "01 33 44 55 66 01 #"+50+"#1.4142135623730951";
			// s=generateString();

			sendIntent.putExtra(Intent.EXTRA_TEXT, fullBTDataRecord);
			// And here it goes ! our message is send to any other app that want
			// to listen to it.
			sendBroadcast(sendIntent);
			// Log.d("charith", "Intent " + numIntent);

			// In our case we run this method each second with postDelayed
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}

		private String generateString() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	// When service is started
	@Override
	public void onStart(Intent intent, int startid) {
		filename = intent.getStringExtra("FileName");
		numIntent = 0;
		// We first start the Handler
		handler.removeCallbacks(sendData);
		handler.postDelayed(sendData, 1000);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		filename = intent.getStringExtra("FileName");
		initBluetooth();
		Log.d("charith", "Intent ");
		numIntent = 0;
		// We first start the Handler
		handler.removeCallbacks(sendData);
		handler.postDelayed(sendData, 1000);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Toast toast = Toast.makeText(getApplicationContext(),
				"The service stopped", 3000);
		toast.show();
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		Toast toast = Toast.makeText(getApplicationContext(),
				"The service Starteed", 3000);
		toast.show();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private String composeBTData() {
		BTData = "";
		for (int i = 0; i < BTBeaconArray.size(); i++) {
			BTData = BTData + BTBeaconArray.get(i).getId() + "#"
					+ BTBeaconArray.get(i).getRssi() + "#"
					+ BTBeaconArray.get(i).getSd();
			if (i < BTBeaconArray.size() - 1) {
				BTData = BTData + "#";

			}
		}
		return BTData;
	}

	private BTBeacon getBTBeacon(String id) {
		boolean isOnList = false;
		int index = 0;
		for (int i = 0; i < BTBeaconArray.size(); i++) {
			if (BTBeaconArray.get(i).getId().contains(id)) {
				isOnList = true;
				index = i;
			}
		}
		if (isOnList) {
			return BTBeaconArray.get(index);
		} else {
			BTBeacon vBTBeacon = new BTBeacon();
			BTBeaconArray.add(vBTBeacon);
			return vBTBeacon;
		}

	}

	private String calculateSD(int rssi) {

		List<Integer> list = new ArrayList<Integer>();
		Integer total = 0;
		Integer average = 0;
		Integer sd = 0;
		Double standardDeviation = 0.0;
		DecimalFormat dec = new DecimalFormat("#.00");

		System.out.println("adding new element");
		System.out.println(list);
		list.add(rssi);
		if (list.size() > 50) {

			for (Integer x : list) {
				total += x;
			}

			average = total / list.size();
			for (Integer x : list) {
				sd += (x - average) * (x - average);
			}
			;

			standardDeviation = Math.sqrt(sd / list.size());
			list.remove(0);
			total = 0;
			sd = 0;
		}
		return standardDeviation.toString();
	}

	public void initBluetooth() {
		try {
			writer = new CSVWriter(
					new FileWriter(Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ File.separator
							+ FOLDER_NAME
							+ File.separator + filename), '\t');
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.d("charith2", "initBluetooth");
		// Bluetooth SETUP
		// final BluetoothManager manager = (BluetoothManager)
		// getSystemService(BLUETOOTH_SERVICE);
		// mBluetoothAdapter = manager.getAdapter();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		scanner = mBluetoothAdapter.getBluetoothLeScanner();

		settings = new ScanSettings.Builder().setScanMode(
				ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		List<ScanFilter> filters = new ArrayList<ScanFilter>();

		Log.d("charith2", "mBluetoothAdapter");

		mScanCallback = new ScanCallback() {
			// };

			// mLeScanCallback = new LeScanCallback() {

			// @Override
			// public void onLeScan(final BluetoothDevice device, final int
			// rssi,
			// final byte[] scanRecord) {
			//
			// //
			// final StringBuilder stringBuilder = new StringBuilder(
			// scanRecord.length);
			// for (byte byteChar : scanRecord)
			// stringBuilder.append(String.format("%02X ", byteChar));
			// //
			// String id = stringBuilder.substring(51, 69);
			// //
			//
			//
			// try {
			// long timestamp = System.currentTimeMillis();
			// fullBTDataRecord = Long.toString(timestamp) + "#" + id
			// + "#" + Integer.toString(rssi);
			// String[] entries = fullBTDataRecord.split("#");
			// writer.writeNext(entries);
			// writer.flushQuietly();
			// // writer.close();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }

			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				// TODO Auto-generated method stub
				//super.onScanResult(callbackType, result);
				i++;
				Log.d("charith", Integer.toString(i));
				
				byte[] scanRecord = result.getScanRecord().getBytes();
				int rssi = result.getRssi();
				final StringBuilder stringBuilder = new StringBuilder(
						scanRecord.length);
				for (byte byteChar : scanRecord)
					stringBuilder.append(String.format("%02X ", byteChar));
				//
				String id = stringBuilder.substring(51, 69);

				try {
					//long timestamp = System.currentTimeMillis();
					long timestamp = result.getTimestampNanos();
					fullBTDataRecord = Long.toString(timestamp) + "#" + id
							+ "#" + Integer.toString(rssi);
					String[] entries = fullBTDataRecord.split("#");
					writer.writeNext(entries);
					writer.flushQuietly();
					// writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
		// mBluetoothAdapter.startLeScan(mLeScanCallback);
		scanner.startScan(filters, settings, mScanCallback);

		Log.d("MainActivityForTommy", "Scan was started");
	}
}