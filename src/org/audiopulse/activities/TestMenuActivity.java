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
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;
import org.audiopulse.utilities.SignalProcessing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//Contains menu from which tests can be selected and run
//tests includ DPOAE, TOAE, device calibration, in-situ calibration
//in situ
//TODO: implement tests as fragments? Or just independent threads?
public class TestMenuActivity extends AudioPulseLaunchActivity 
{
	public static final String TAG="TestMenuActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=0.5;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_menu);
		
		// set listener for clickable menu items
		ListView menuList = (ListView) findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(
        	new AdapterView.OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        			
        			TextView item = (TextView) itemClicked;
        			String itemText = item.getText().toString();
        			        			//item.getId(), R.id.
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
        				//TODO: change this to launch a plot activity
        				plotWaveform();
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.menu_all_right)) ||
        					itemText.equalsIgnoreCase(getResources().getString(R.string.menu_all_left))) {
        				//TODO: launch test activity
        				startActivity(new Intent(TestMenuActivity.this, DPOAEActivity.class));
        				
        			}
        			else {
        				//TODO: launch test activity
        				emptyText(); //Clear text for new stimuli test and spectral plotting
        				playRecordThread(itemText,true);
        			} 
        			
        		}
        	}
		);
	}
    
	
	private RecordThreadRunnable playRecordThread(String item_selected, boolean showSpectrum)
	{
		
		//Ignore playing thread when obtaining SOAEs
		beginTest();	
		Context context=this.getApplicationContext();		
		
		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context,item_selected,showSpectrum);
		
		if(item_selected.equalsIgnoreCase(getResources().getString(R.string.menu_spontaneous)) ){
			ExecutorService execSvc = Executors.newFixedThreadPool( 1 );
			rRun.setExpectedFrequency(0);
			recordThread = new Thread(rRun);	
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.shutdown();
		}else{
			playStatusBackHandler = new ReportStatusHandler(this);
			PlayThreadRunnable pRun = new PlayThreadRunnable(playStatusBackHandler,playTime);
			ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
			playThread = new Thread(pRun);
			rRun.setExpectedFrequency(pRun.stimulus.expectedResponse);
			recordThread = new Thread(rRun);	
			playThread.setPriority(Thread.MAX_PRIORITY);
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.execute( playThread );
			execSvc.shutdown();
		}
		endTest();
		return rRun;
	}
	
	private Bundle generateDPGram(Bundle DPGramresults, String itemText) {
		//TODO: plot audiogram results
		//Generate list of tests to run
		List<String> RunTest= new ArrayList<String>();
		RunTest.add(getResources().getString(R.string.menu_2k));
		RunTest.add(getResources().getString(R.string.menu_3k));
		RunTest.add(getResources().getString(R.string.menu_4k));
		for(String runme: RunTest){
			emptyText(); //Clear text for new stimuli test and spectral plotting
			playRecordThread(runme,false);
			//TODO: Implement a hold between playing threads
		}
		
		//TODO: Extract these results from data!
		double[] DPOAEData={7.206, -7, 5.083, 13.1,3.616, 17.9,2.542, 11.5,1.818, 17.1};
        double[] noiseFloor={7.206, -7-10,5.083, 13.1-10,3.616, 17.9-10,2.542, 11.5-10,1.818, 17.1-10};
        double[] f1Data={7.206, 64,5.083, 64,3.616, 64,2.542, 64,1.818, 64};
        double[] f2Data={7.206, 54.9,5.083, 56.6,3.616, 55.6,2.542, 55.1,1.818, 55.1};

        //double[] Pxx=SignalProcessing.getDPOAEResults(audioBundle);
        
		DPGramresults.putString("title",itemText);
		DPGramresults.putDoubleArray("DPOAEData",DPOAEData);
		DPGramresults.putDoubleArray("noiseFloor",noiseFloor);
		DPGramresults.putDoubleArray("f1Data",f1Data);
		DPGramresults.putDoubleArray("f2Data",f2Data);
		
		return DPGramresults;

	}
	
}