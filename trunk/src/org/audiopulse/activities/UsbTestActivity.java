package org.audiopulse.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.audiopulse.R;
import org.audiopulse.activities.TestActivity.Messages;
import org.audiopulse.analysis.DPOAEAnalyzer;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.hardware.APulseIface;
import org.audiopulse.hardware.USBIface;
import org.audiopulse.io.AudioPulseFilePackager;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.tests.TestProcedure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class UsbTestActivity extends Activity implements Handler.Callback {
	private static final String TAG = "UsbTestActivity";
	protected Button reset_button;
	protected Button status_button;
	protected Button start_button;
	protected Button getdata_button;
	protected Button plotdata_button;
	private double[] data;
	private double[] psd;
	private static final File root = Environment.getExternalStorageDirectory();

	protected TextView textview;
	protected EditText app_out;
	protected Switch toggle_button;
	protected APulseIface apulse;

	short f1;
	double db1;
	short f2;
	double db2;
	DPOAEResults dpoaResults;
	double respSPL, noiseSPL, respHz;
	private static final String protocol = "USbTest";
	private String fileName; // File name that will be used to save the test
								// data if the user decide to

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("musbreceiver", "activity received broadcast");
			if (USBIface.ACTION_USB_PERMISSION.equals(action)) {

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usb_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		textview = (TextView) findViewById(R.id.textView);
		toggle_button = (Switch) findViewById(R.id.switch1);

		app_out = (EditText) findViewById(R.id.editText3);
		Log.v(TAG, "initialized app_out to:" + app_out);
		reset_button = (Button) findViewById(R.id.button5);
		status_button = (Button) findViewById(R.id.button6);
		start_button = (Button) findViewById(R.id.button7);
		getdata_button = (Button) findViewById(R.id.button8);
		plotdata_button = (Button) findViewById(R.id.button10);

		status_button.setEnabled(false);
		getdata_button.setEnabled(false);
		reset_button.setEnabled(false);
		start_button.setEnabled(false);

		apulse = new APulseIface(this);
		app_out.setKeyListener(null);

		// Attempt to connect as soon as activity is launched
		// toggle_button.setChecked(true);
		// connectToggleButton(toggle_button);

		// IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		// registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_usb, container,
					false);
			return rootView;
		}
	}

	public void connectToggleButton(View view) {
		final Switch sw = (Switch) view;
		int ret;
		app_out.setText("");
		if (sw.isChecked()) {
			textview.setText("Attempting to connect...");
			ret = apulse.usb.connect(new USBIface.USBConnHandler() {
				public void handleConnected() {
					textview.setText("Connected successfully!");
					status_button.setEnabled(true);
					getdata_button.setEnabled(true);
					reset_button.setEnabled(true);
					start_button.setEnabled(true);
				}

				public void handleError() {
					textview.setText("Error with permissions");
					sw.setChecked(false);

					status_button.setEnabled(false);
					getdata_button.setEnabled(false);
					reset_button.setEnabled(false);
					start_button.setEnabled(false);
				}
			});

			if (ret != 0) {
				textview.setText(String.format("Error connecting: %d ", ret)
						+ apulse.usb.error);
				sw.setChecked(false);
			}
		} else {
			// Ignore for now
			textview.setText("Disconnected!");
		}
	}

	public void resetButton(View view) {
		apulse.reset();
	}

	public void statusButton(View view) {
		Log.v(TAG, "getting status");
		APulseIface.APulseStatus status = apulse.getStatus();

		String out = String.format("Version:       %d\n"
				+ "Test state:    %s\n" + "WaveGen state: %s\n"
				+ "Input state:   %s\n" + "Error:         %d", status.version,
				status.testStateString(), status.wgStateString(),
				status.inStateString(), status.err_code);
		Log.v(TAG, "setting text");
		app_out.setText(out);
	}

	public void startButton(View view) {
		APulseIface.ToneConfig[] tones = new APulseIface.ToneConfig[2];

		try {
			f1 = Short.decode(((TextView) findViewById(R.id.f_1)).getText()
					.toString());
			short t1 = Short.decode(((TextView) findViewById(R.id.t1_1))
					.getText().toString());
			short t2 = Short.decode(((TextView) findViewById(R.id.t2_1))
					.getText().toString());
			db1 = (double) Short.decode(((TextView) findViewById(R.id.db_1))
					.getText().toString());

			tones[0] = new APulseIface.FixedTone(f1, t1, t2, db1, 0);

			f2 = Short.decode(((TextView) findViewById(R.id.f_2)).getText()
					.toString());
			t1 = Short.decode(((TextView) findViewById(R.id.t1_2)).getText()
					.toString());
			t2 = Short.decode(((TextView) findViewById(R.id.t2_2)).getText()
					.toString());
			db2 = (double) Short.decode(((TextView) findViewById(R.id.db_2))
					.getText().toString());

			tones[1] = new APulseIface.FixedTone(f2, t1, t2, db2, 1);
		} catch (NullPointerException e) {
			app_out.setText("Error with arguments");
			return;
		}

		apulse.configCapture(2000, 256, 200);
		apulse.configTones(tones);
		apulse.start();
	}

	public void getdataButton(View view) {
		if (apulse.getStatus().test_state == APulseIface.APulseStatus.TEST_DONE) {
			APulseIface.APulseData data = apulse.getData();
			psd = data.getPSD(); // PSD returns data in dB
			String out = "";
			for (int i = 0; i < psd.length; i++) {
				out += String.format("%d:\t%.10f\n",
						(int) (((double) i) * 31.25), psd[i]);
			}
			app_out.setText(out);
			Log.v(TAG, "setting data average");
			this.data = data.getAverage();
			if (this.data == null)
				Log.e(TAG, "Got null average!!");
		} else {
			app_out.setText("Data not ready...");
		}
	}

	public void plotdataButton(View view) {
		int Fs = getResources()
				.getInteger(R.integer.recordingSamplingFrequency);
		respHz = (2.0 * f1 - f2);
		respHz = (respHz <= 0) ? Fs : respHz;
		respHz = (respHz > (Fs / 2.0)) ? Fs : respHz;
		DPOAEAnalyzer dpoaeAnalysis=new DPOAEAnalyzer(psd,Fs,f2,f1,respHz,protocol,fileName);
		DPOAEResults responseData=null;
		try {
			responseData = dpoaeAnalysis.call();
			responseData.setWave(data);
			respSPL=responseData.getRespSPL();
			noiseSPL=responseData.getNoiseSPL();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.v(TAG,"handling call back ");
		Bundle extraData = new Bundle();
		extraData.putDoubleArray("psd", psd);
		extraData.putDoubleArray("data", data);
		extraData.putShort("f1", f1);// Test frequency
		extraData.putDouble("respSPL", respSPL);
		extraData.putDouble("noiseSPL", noiseSPL);
		Log.v(TAG, "respSPL=" + respSPL + " noiseSPL= " + noiseSPL);
		extraData.putFloat("recSampleRate", Fs);
		extraData.putDouble("respHz", respHz);
		extraData.putInt("fftSize", 0);
		Intent testIntent = new Intent(UsbTestActivity.this,
				PlotSpectralActivity.class);
		testIntent.putExtras(extraData);
		startActivity(testIntent);
	
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			// Prompt user on how to continue based on the display of the
			// analyzed results
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("Select an option");

			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Exit",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.i(TAG,
									"Setting users result to cancell and exiting");
							setResult(RESULT_CANCELED, null);
							UsbTestActivity.this.finish();
						}
					});

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Try Again",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// this should go back to the test activity by
							// default
							UsbTestActivity.this.finish();
						}
					});

			dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Save & Exit",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Pass URI if called from Sana procedure
							showDialog(0);
							// Generate the file name based on time stamp of the
							// test and protocol name
							// This name will be used to store both the raw file
							// as well as the zipped (packaged)
							// file, so the file type extension is not added in
							// here.
							fileName = "AP_" + "-" + protocol + "-" + '-' + f1
									+ "kHz-" + new Date().toString();
							fileName = fileName.replace(" ", "-").replace(":",
									"-");

							respHz = (double) (2.0 * f1 - f2);
							dpoaResults = new DPOAEResults(respSPL, noiseSPL,
									db1, db2, respHz, f1, f2, psd, data,
									fileName, protocol);
							// Start lengthy operation in a background thread
							new Thread(new Runnable() {
								public void run() {

									List<String> fileList = new ArrayList<String>();
									// Store only the PSD, not the frequency x
									// axis
									AudioPulseFileWriter fftWriter = new AudioPulseFileWriter(
											new File(root + "/"
													+ dpoaResults.getFileName()
													+ "-fft.raw"), dpoaResults
													.getDataFFT());
									fftWriter.start();
									AudioPulseFileWriter wavWriter = new AudioPulseFileWriter(
											new File(root + "/"
													+ dpoaResults.getFileName()
													+ "-wav.raw"), dpoaResults
													.getWave());
									wavWriter.start();
									try {
										Log.v(TAG, "Adding file to zip: "
												+ root + fileName + "-fft.raw");
										fftWriter.join();
										Log.v(TAG, "Adding file to zip: "
												+ root + fileName + "-wav.raw");
										wavWriter.join();
										// Add file to list of files to be
										// zipped
										fileList.add(root + "/" + fileName
												+ "-fft.raw");
										fileList.add(root + "/" + fileName
												+ "-wav.raw");
									} catch (InterruptedException e) {
										Log.e(TAG,
												"InterruptedException caught: "
														+ e.getMessage());
									}

									// Zip files
									AudioPulseFilePackager packager = new AudioPulseFilePackager(
											fileList);
									packager.start();
									File PackagedFile = new File(root, fileName
											+ ".zip");

									// Add the Packaged filename to the bundle,
									// which is passed to Test Activity.
									Intent output = new Intent();
									// TODO:figure out why output.putExtra is
									// giving an exception!!
									fileName = PackagedFile.getAbsolutePath();
									output.putExtra("ZIP_URI", Uri
											.encode(PackagedFile
													.getAbsolutePath()));
									Log.i(TAG,
											"Setting users result to ok and passing intent to activity: "
													+ PackagedFile
															.getAbsolutePath());
									setResult(RESULT_OK, output);
									try {
										packager.join();
									} catch (InterruptedException e) {
										Log.e(TAG,
												"Error while packaging data: "
														+ e.getMessage());
										e.printStackTrace();
									}

									dismissDialog(0);
									UsbTestActivity.this.finish();
								}
							}).start();
						}
					});
			dialog.show();
			return true;
		}

		} // of switches

		// exit activity
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle("Saving data to: " + fileName);
		dialog.setMessage("Please wait...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		return dialog;
	}

	@Override
	public boolean handleMessage(Message msg) {
		//TODO: clean up. not sure we need this anymore
		return true;
	}

}
