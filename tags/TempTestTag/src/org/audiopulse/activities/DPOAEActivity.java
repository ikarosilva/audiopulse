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
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.DPOAEStimulus;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;
import org.audiopulse.utilities.SignalProcessing;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//TODO: change the scope of this activity's purpose.
//this activity will be a base class for test procedures (rename it TestProcedureActivity?)
//and will have a default layout: text log, quick graph of input & output
//other tests procedures can extend this, define their own layouts
//e.g. define a DPOAE activity that has an online DPgram display
//e.g. calibration activity that has an I/O display
//this activity and extended classes will be launched from TestMenuActivity.

public class DPOAEActivity extends GeneralAudioTestActivity 
{
	public static final String TAG="DPOAEActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	String testNameKey="testName";
	String testNameValue;
	Bundle DPOAERequest;
	String filePrefix="DPOAE_";
	DPOAEStimulus stimulus;
	
	public static double playTime=0.5;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dpoae);
		DPOAERequest = getIntent().getExtras();
		testNameValue=DPOAERequest.getString(testNameKey);
		
		//findViewById(android.R.id.content).invalidate();
        //performTest();			
	}
    
	public void startTest(View callingView){
		//TODO: plot audiogram results
		//Generate list of tests to run
		List<String> RunTest= new ArrayList<String>();
		
		if (testNameValue.equalsIgnoreCase(getResources().getString(R.string.dpgram_right)) ||
				testNameValue.equalsIgnoreCase(getResources().getString(R.string.dpgram_left)) ){
			RunTest.add(getResources().getString(R.string.dpoae_2k));
			RunTest.add(getResources().getString(R.string.dpoae_3k));
			RunTest.add(getResources().getString(R.string.dpoae_4k));
		} else {
			//In this case we are running a single test, the name of which is already passed by the Bundle
			RunTest.add(testNameValue);
		}
		 
		for(String runme: RunTest){
			emptyText(); //Clear text for new stimuli test and spectral plotting
			playRecordThread();
			//TODO: Implement a hold between playing threads
		}
		
		//TODO: Extract these results from data!
		double[] DPOAEData={7.206, -7, 5.083, 13.1,3.616, 17.9,2.542, 11.5,1.818, 17.1};
        double[] noiseFloor={7.206, -7-10,5.083, 13.1-10,3.616, 17.9-10,2.542, 11.5-10,1.818, 17.1-10};
        double[] f1Data={7.206, 64,5.083, 64,3.616, 64,2.542, 64,1.818, 64};
        double[] f2Data={7.206, 54.9,5.083, 56.6,3.616, 55.6,2.542, 55.1,1.818, 55.1};

        //double[] Pxx=SignalProcessing.getDPOAEResults(audioBundle);
        
		Bundle DPGramresults= new Bundle();
		DPGramresults.putString("title","DPOAE Results");
		DPGramresults.putDoubleArray("DPOAEData",DPOAEData);
		DPGramresults.putDoubleArray("noiseFloor",noiseFloor);
		DPGramresults.putDoubleArray("f1Data",f1Data);
		DPGramresults.putDoubleArray("f2Data",f2Data);
		
		//Plot all results 
		plotAudiogram(DPGramresults);
		
	}

	private void plotAudiogram(Bundle dPGramresults) {
		// TODO Auto-generated method stub
		
	}
	
}