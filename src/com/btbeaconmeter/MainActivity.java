package com.btbeaconmeter;

import com.example.btbeaconcollector.R;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity {

	public static TextView tvDetails;
	private BTDataReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvDetails = (TextView) findViewById(R.id.tvDetails);

		//Starting the BTDataCollector Service
		Button btnStartService = (Button) findViewById(R.id.btnStartService);
		btnStartService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Create the  BTDataReceiver
				receiver = new BTDataReceiver(new Handler());
				//Register the Broadcast Receiver (BTDataReceiver)
				registerReceiver(receiver, new IntentFilter(
						"com.test.sendintent.IntentToUnity"));
				Intent myintent = new Intent(getBaseContext(), BTDataCollectorService.class);
				myintent.putExtra("FileName", Long.toString(System.currentTimeMillis()));
				//startService(new Intent(getBaseContext(), BTDataCollectorService.class));
				startService(myintent);
			}
		});

		Button btnStopService = (Button) findViewById(R.id.btnStopService);
		btnStopService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopService(new Intent(getBaseContext(), BTDataCollectorService.class));
				unregisterReceiver(receiver);
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

	public static class BTDataReceiver extends BroadcastReceiver {

		private final Handler handler; // Handler used to execute code on the UI
										// thread

		public BTDataReceiver(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void onReceive(final Context context, Intent intent) {

			// public Intent intent = intent;
			// handler.post(new Runnable() {
			// @Override
			// public void run() {
			// String sentIntent =
			String text = "";
			String sentIntent = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (sentIntent != null) {
				// We assigned it to our static variable
				text = sentIntent + "\n";
			}
			tvDetails.append(text);
			// Toast.makeText(context, "Toast from broadcast receiver",
			// Toast.LENGTH_SHORT).show();
			// }
		}
		// );
	}
}
