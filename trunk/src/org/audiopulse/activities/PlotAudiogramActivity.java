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

import java.util.ArrayList;
import java.util.HashMap;

import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.graphics.PlotAudiogramView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

public class PlotAudiogramActivity extends AudioPulseActivity {
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState
	 */
	private HashMap<String, Double> results;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle data = getIntent().getExtras();

		Log.v(TAG,"extracting bundled data");
		//Get Data generated according to the AudioPulseDataAnalyzer interface
		//this should be a HashMap with keys defined in the interface

		results=(HashMap<String, Double>) data.getSerializable(AudioPulseDataAnalyzer.Results_MAP);

		//Decode test name from the results and the mapping in the interface
		Log.v(TAG,"results= "+ results.toString());
		Log.v(TAG,"results keys= "+ results.keySet());

		String title=AudioPulseDataAnalyzer.testName.get(
				results.get(AudioPulseDataAnalyzer.TestType).intValue());

		//Loop through the bundle to get the results 
		//The arrays should be interleaved, with odd samples representing X
		//and even samples representing Y coordinates
		ArrayList<Double> responseData= new ArrayList<Double>();
		ArrayList<Double> noiseData= new ArrayList<Double>();
		ArrayList<Double> stimData= new ArrayList<Double>();
		for(String key: AudioPulseDataAnalyzer.responseKeys){
			Double tmpdata = results.get(key);
			if(tmpdata !=null){
				//Get frequency
				responseData.add(AudioPulseDataAnalyzer.frequencyMapping.get(key));
				responseData.add(tmpdata);
			}else{
				Log.v(TAG,"null key: "+ key);
			}
		}

		for(String key: AudioPulseDataAnalyzer.noiseKeys){
			Double tmpdata = results.get(key);
			if(tmpdata !=null){
				//Get frequency
				noiseData.add(AudioPulseDataAnalyzer.frequencyMapping.get(key));
				noiseData.add(tmpdata);
			}else{
				Log.v(TAG,"null key: "+ key);
			}
		}

		for(String key: AudioPulseDataAnalyzer.stimKeys){
			Double tmpdata = results.get(key);
			if(tmpdata !=null){
				//Get frequency
				stimData.add(AudioPulseDataAnalyzer.frequencyMapping.get(key));
				stimData.add(tmpdata);
			}else{
				Log.v(TAG,"null key: "+ key);
			}
		}

		Log.v(TAG,"finished xtracting data from bundle");  	
		PlotAudiogramView mView = new PlotAudiogramView(this,title,responseData,noiseData,stimData);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(mView);
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
					//TODO: clear state variables and pass a NULL URI
					//if called from Sana
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Try Again",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// TODO: maybe do nothing here or clear state variables?  
					//this should go back to the test activity by default
				}
			});

			dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Save & Exit",
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//TODO: Implement saving file to disk (zipped) as  a thread
					//and passing URI if called from Sana procedure
					
				}
			});

			dialog.show();
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}




}
