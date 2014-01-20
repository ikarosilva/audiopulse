/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sana.android.app.ObservationActivity;
import org.audiopulse.R;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.io.AudioPulseFilePackager;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.tests.TestProcedure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

//TestActivity is a template for all tests.
public class PrintResultsActivity extends Activity {
	public static final String TAG="PrintResultsActivityy";
	public static final int CONFIRM_PLOT_CODE = 1;
	protected TextView testLog;
	private ArrayList<String> fileNames = new ArrayList<String>();
	private Bundle data;
	private File PackagedFile;
	ArrayList<Double> responseData= new ArrayList<Double>();
	ArrayList<Double> noiseData= new ArrayList<Double>();
	ArrayList<Double> stimData= new ArrayList<Double>();
	private ArrayList<DPOAEResults> DPGRAM = new ArrayList<DPOAEResults>();
	private String fileName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_test_layout);
		testLog = (TextView)this.findViewById(R.id.testLog);
		data = getIntent().getExtras();
		fileName=null;
		//Get Data generated according to the AudioPulseDataAnalyzer interface
		//this should be a HashMap with keys defined in the interface
		DPGRAM=(ArrayList<DPOAEResults>) data.getSerializable("DPGRAM");

		//Loop through the bundle to get the results 
		//The arrays should be interleaved, with odd samples representing X
		//and even samples representing Y coordinates
		int ind=1;
		String display="";
		double diff;
		for(DPOAEResults dpoae: DPGRAM){		
			diff=Math.round(10*(dpoae.getRespSPL()-dpoae.getNoiseSPL()))/10.0;
			display+=testLog.getText() + "\n Results for test #" + ind + ":  " +
			        + dpoae.getStim1Hz() + " Hz response =  " + dpoae.getRespSPL() 
					+ " dB SPL, noise =  " + dpoae.getNoiseSPL() + " dB SPL, resp-noise =  "
					+ (diff) + " \n";
			ind++;	
		}
		testLog.setText(display + "\n\n***Finished! Hit back button to save, cancel, or repeat tests.");
	}

	public String getPackageFileName(){
		return this.PackagedFile.getAbsolutePath();
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		{
			//Prompt user on how to continue based on the display of the analyzed results
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("Select an option");

			dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Exit",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					Log.i(TAG,"Setting users result to cancell and exiting");
					setResult(RESULT_CANCELED,null);
					PrintResultsActivity.this.finish();
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Try Again",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) { 
					//this should go back to the test activity by default
					PrintResultsActivity.this.finish();
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Save & Exit",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//Pass URI if called from Sana procedure
					
					Log.v(TAG,"Dialog response was:" + which);
					showDialog(0);

					// Start lengthy operation in a background thread
					new Thread(new Runnable() {
						public void run() {
							
							List<String> fileList=new ArrayList<String>();
							for(DPOAEResults dpoae: DPGRAM){
								//Store only the PSD, not the frequency x axis
								AudioPulseFileWriter writer= new AudioPulseFileWriter(
										new File(dpoae.getFileName()),dpoae.getDataFFT());
								writer.start();
								try {
									writer.join();
									fileName=dpoae.getFileName();
									Log.v(TAG,"Adding file to zip: " + fileName);
									//Add file to list of files to be zipped
									fileList.add(fileName);
								} catch (InterruptedException e) {
									Log.e(TAG,"InterruptedException caught: " + e.getMessage() );
								}	
							}
							//Zip files
							AudioPulseFilePackager packager= new AudioPulseFilePackager(fileList);
							packager.start();
							
							//To be returned to the activity that requested the plotting
							PackagedFile=packager.getOutFile();		
							
							//Add the Packaged filename to the bundle, which is passed to Test Activity.
							Intent output = new Intent();		
							//TODO:figure out why output.putExtra is giving an exception!!
							fileName=PackagedFile.getAbsolutePath();
							output.putExtra("ZIP_URI", Uri.encode(PackagedFile.getAbsolutePath()));
							Log.i(TAG,"Setting users result to ok and passing intent to activity: " + PackagedFile.getAbsolutePath());
							setResult(RESULT_OK, output);							
							try {
								packager.join();
							} catch (InterruptedException e) {
								Log.e(TAG,"Error while packaging data: " + e.getMessage());
								e.printStackTrace();
							}
							
							PrintResultsActivity.this.finish();
						}
					}).start();
				}
			});
			dialog.show();
			return true;
		}

		} //of switches

		//exit activity
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
	
}