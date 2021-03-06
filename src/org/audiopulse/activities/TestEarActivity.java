package org.audiopulse.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.audiopulse.R;
import org.audiopulse.analysis.DPOAEAnalyzer;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.hardware.APulseIface;
import org.audiopulse.hardware.USBIface;
import org.audiopulse.io.AudioPulseFilePackager;
import org.audiopulse.io.AudioPulseFileWriter;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

public class TestEarActivity extends Activity implements Handler.Callback {
	private static final String TAG = "TestEarActivity";
	protected Button start_button;
	protected Button plotdata_button;
	protected double[] psd;
	private static final File root = Environment.getExternalStorageDirectory();
	protected TextView textview;
	protected EditText app_out;
	protected Switch toggle_button;
	protected APulseIface apulse;
	private static final short[] f1={2000,3000,4000};
	private static final double db1=65;
	private static final short[] f2={2400,3600,4800};
	private short currentTestFrequencyF1;
	private short currentTestFrequencyF2;
	private static final double db2=55;
	private static final short t1=1000;
	private static final short t2=2000;
	DPOAEResults responseData;
	private static int Fs; //sampling frequency in Hz
	DPOAEResults dpoaResults;
	double respSPL, noiseSPL, respHz;
	private static final String protocol = "USbTest";
	private String fileName; // File name that will be used to save the test
	// data if the user decide to
	private privateUsbHandler UsbHandler;
	private MonitorHandler mUIHandler;

	//Inner class for Usb Handler
	class privateUsbHandler extends USBIface.USBConnHandler {
		private Switch sw;
		public privateUsbHandler(Switch s){
			sw=s;
		}
		public void handleConnected() {
			textview.setText("Connected successfully!");
			start_button.setEnabled(true);
		}
		public void handleError() {
			textview.setText("Error with permissions");
			sw.setChecked(false);
			start_button.setEnabled(false); 
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("musbreceiver", "activity received broadcast");
			if (USBIface.ACTION_USB_PERMISSION.equals(action)) {

			}
		}
	};

	public void setCurrentTestFrequencies(short F1, short F2){
		currentTestFrequencyF1=F1;
		currentTestFrequencyF2=F2;
		//app_out.append("Setting current frequencies to : F1=" 
		//+ F1+ " F2= " + F2 +"\n");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usb_ear_test);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		textview = (TextView) findViewById(R.id.textView);
		toggle_button = (Switch) findViewById(R.id.switch1);
		app_out = (EditText) findViewById(R.id.editText3);
		Log.v(TAG, "initialized app_out to:" + app_out);
		start_button = (Button) findViewById(R.id.button7);
		plotdata_button = (Button) findViewById(R.id.button10);
		start_button.setEnabled(false); 
		plotdata_button.setEnabled(false);

		//Handler responsible for communicating between UI activity and any
		//thread that requires intensive work
		mUIHandler = new MonitorHandler(this); 
		apulse = new APulseIface(this);
		app_out.setKeyListener(null);

		//Attempt to connect to USB as soon as activity is created
		UsbHandler=new privateUsbHandler(toggle_button);
		if (apulse.usb.connect(UsbHandler) != 0) {
			toggle_button.setChecked(false);
		}else{
			toggle_button.setChecked(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);

		//Handler responsible for communicating between UI activity and any
		//thread that requires intensive work
		mUIHandler = new MonitorHandler(this); 	
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
		int ret;
		app_out.setText("");
		if (toggle_button.isChecked()) {
			textview.setText("Attempting to connect...");
			ret = apulse.usb.connect(UsbHandler);
			if (ret != 0) {
				textview.setText(String.format("Error connecting: %d ", ret)
						+ apulse.usb.error);
				toggle_button.setChecked(false);
			}
		} else {
			// Ignore for now
			textview.setText("Disconnected!");
		}
	}

	public void status(View view) {
		APulseIface.APulseStatus status = apulse.getStatus();
		app_out.append("Status: " + status.testStateString()+"\n");
	}

	public void startButton(View view) {
		status(view);
		Thread monitor=new MonitorThread(mUIHandler,apulse,f1,f2);
		monitor.start();
	}

	public synchronized void doOneTest(){
		//TODO: We may need to check that the driver is in the proper state prior
		//to continuing
		APulseIface.ToneConfig[] tones = new APulseIface.ToneConfig[2];
		try {
			tones[0] = new APulseIface.FixedTone(currentTestFrequencyF1, t1, t2, db1, 0);
			tones[1] = new APulseIface.FixedTone(currentTestFrequencyF2, t1, t2, db2, 1);
		} catch (NullPointerException e) {
			app_out.append("\nInvalid inputs: f1= " + currentTestFrequencyF1 + " f2= " 
					+ currentTestFrequencyF2 + " db1= " + db1 + " db2= " + db2+ ".");
			return;
		}
		//TODO: Get rid of the magic numbers
		apulse.configCapture(2000, 256, 200);
		apulse.configTones(tones);
		app_out.append("\n\n TestF1: " + currentTestFrequencyF1 + " kHz.");
		app_out.append("\tTestF2: " + currentTestFrequencyF2 + " kHz.");
		apulse.start();
	}

	public void getData() throws Exception {
			//TODO: Confirm that the DSP buffer gets cleared out with 
			//this call. 
			APulseIface.APulseData data = apulse.getData();
			psd = data.getPSD(); // PSD returns data in dB
	}

	public int getPSDSize(){
		return psd.length;
	}

	public void analyzePSD() throws Exception{
		Fs = getResources()
				.getInteger(R.integer.recordingSamplingFrequency);
		respHz = ((2.0 * currentTestFrequencyF1) - currentTestFrequencyF2);
		if ( (respHz <= 0) || (respHz > (Fs / 2.0)) )
			throw new Exception("Invalid response frequency: " + respHz);
		
		DPOAEAnalyzer dpoaeAnalysis=new DPOAEAnalyzer(psd,Fs,currentTestFrequencyF2,currentTestFrequencyF1,respHz,protocol,fileName);
		try {
			responseData = dpoaeAnalysis.call();
			respSPL=responseData.getRespSPL();
			noiseSPL=responseData.getNoiseSPL();
			Log.v(TAG,"estimated noiseSPL = " + noiseSPL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void plotdataButton(View view) {

		//NOTE: Because for DPOAE we are not interested in waveform data, 
		//we will only the power spectrum data/results
		Bundle extraData = new Bundle();
		extraData.putDoubleArray("psd", psd);
		extraData.putShort("f1", currentTestFrequencyF1);// Test frequency
		extraData.putDouble("respSPL", respSPL);
		extraData.putDouble("noiseSPL", noiseSPL);
		extraData.putDoubleArray("noiseRangeHz",responseData.getNoiseRangeHz());
		Log.v(TAG, "respSPL=" + respSPL + " noiseSPL= " + noiseSPL);
		extraData.putFloat("recSampleRate", Fs);
		extraData.putDouble("respHz", respHz);
		extraData.putInt("fftSize", 0);
		Intent testIntent = new Intent(TestEarActivity.this,
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
							"Setting users result to cancel and exiting");
					setResult(RESULT_CANCELED, null);
					TestEarActivity.this.finish();
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Try Again",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// this should go back to the test activity by
					// default
					TestEarActivity.this.finish();
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
					fileName = "AP_" + "-" + protocol + "-" + '-' + f1[0]
							+ "kHz-" + new Date().toString();
					fileName = fileName.replace(" ", "-").replace(":",
							"-");

					//TODO:Fix frequencies so the results are a package
					respHz = (double) (2.0 * f1[0] - f2[0]);
					//Set waveform data to null because we are not interesed
					dpoaResults = new DPOAEResults(respSPL, noiseSPL,
							db1, db2, respHz, f1[0], f2[0], psd, null,
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
							TestEarActivity.this.finish();
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
		return true;
	}

	public short getCurrentF1() {
		return currentTestFrequencyF1;	
	}

	public short getCurrentF2() {
		return currentTestFrequencyF2;	
	}

}
