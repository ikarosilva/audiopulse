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
 * PlotSpectralActivity.java 
 * based on DeviationRendererDemo02Activity.java
 * from afreechartdemo
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.graphics.PlotAudiogramView;
import org.audiopulse.io.AudioPulseFilePackager;
import org.audiopulse.io.AudioPulseFileWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

public class PlotAudiogramActivity extends Activity {
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState
	 */
	private ArrayList<String> fileNames = new ArrayList<String>();
	private String testName;
	private Bundle data;
	private File PackagedFile;
	private static String TAG="PlotAudiogramActivity";
	ArrayList<Double> responseData= new ArrayList<Double>();
	ArrayList<Double> noiseData= new ArrayList<Double>();
	ArrayList<Double> stimData= new ArrayList<Double>();
	private ArrayList<DPOAEResults> DPGRAM = new ArrayList<DPOAEResults>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"extracting bundled data for plotting");
		data = getIntent().getExtras();
		//Get Data generated according to the AudioPulseDataAnalyzer interface
		//this should be a HashMap with keys defined in the interface
		Log.v(TAG,"deserializing dpgram");
		DPGRAM=(ArrayList<DPOAEResults>) data.getSerializable("DPGRAM");

		//Loop through the bundle to get the results 
		//The arrays should be interleaved, with odd samples representing X
		//and even samples representing Y coordinates
		Log.v(TAG,"looping through frequency data");
		for(DPOAEResults dpoae: DPGRAM){
			Log.v(TAG,"adding response data");
			responseData.add(dpoae.getRespHz());
			responseData.add(dpoae.getRespSPL());
			
			Log.v(TAG,"adding noise data");
			noiseData.add(dpoae.getRespHz());
			noiseData.add(dpoae.getNoiseSPL());
			
			Log.v(TAG,"adding stim data");
			stimData.add(dpoae.getStim1Hz());
			stimData.add(dpoae.getStim1SPL());
			
			Log.v(TAG,"adding stim2 data");
			stimData.add(dpoae.getStim2Hz());
			stimData.add(dpoae.getStim2SPL());
			
			Log.v(TAG,"adding protocol info");
			testName=dpoae.getProtocol();
			Log.v(TAG,"adding file name:" +dpoae.getFileName());
			fileNames.add(dpoae.getFileName());
		}
		
		
		Log.v(TAG,"finished xtracting data from bundle");  	
		//NOTE: PlotAudiogramView assumes data is being send in an interleaved array where
		// odd samples are X-axis and even samples go in the Y-axis
		PlotAudiogramView mView = new PlotAudiogramView(this,testName,responseData,noiseData,stimData);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(mView);
	}

	public String getPackageFileName(){
		return this.PackagedFile.getAbsolutePath();
	}

	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		{

			//TODO: Check that the thread that is saving the file is not running (this could happen if the user hits the back
			//button after hitting the Save& Exit button, so we need to control for that

			//Prompt user on how to continue based on the display of the analyzed results
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("Select an option");

			dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Exit",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					Log.i(TAG,"Setting users result to cancell and exiting");
					setResult(RESULT_CANCELED,null);
					PlotAudiogramActivity.this.finish();
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Try Again",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// TODO: maybe do nothing here or clear state variables?  
					//this should go back to the test activity by default
					PlotAudiogramActivity.this.finish();
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Save & Exit",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//TODO: passs URI if called from Sana procedure
					
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
									Log.v(TAG,"Adding file to zip: " + dpoae.getFileName());
									//Add file to list of files to be zipped
									fileList.add(dpoae.getFileName());
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
							output.putExtra("ZIP_URI", Uri.encode(PackagedFile.getAbsolutePath()));
							Log.i(TAG,"Setting users result to ok and passing intent to activity: " + PackagedFile.getAbsolutePath());
							setResult(RESULT_OK, output);							
							try {
								packager.join();
							} catch (InterruptedException e) {
								Log.e(TAG,"Error while packaging data: " + e.getMessage());
								e.printStackTrace();
							}
							
							dismissDialog(0);
							PlotAudiogramActivity.this.finish();
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
		dialog.setTitle("Saving data");
		dialog.setMessage("Please wait...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		return dialog;
	}


}
