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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;
import org.sana.android.Constants;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//NOTE: This class has been Deprecated!! Do not use it! Each audio test will have its own activity,
//rather than have this class control all types of test. 
//A cleaner and abstract class was derived from this one (GeneralAudioTestActivities). 
//You AudioTest class should extend that class, which can be called from the TestMenuActivity. 

@Deprecated public class ThreadedPlayRecActivity extends GeneralAudioTestActivity 
{
	public static final String TAG="ThreadedPlayRecActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=0.5;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);
	List<Uri> outFiles = new ArrayList<Uri>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check if sana is starting the application
		String caller = this.getCallingPackage();
		Log.d(TAG, "Called by: " + caller);
		if (caller != null && getCallingPackage().compareToIgnoreCase("org.moca") == 0){
			setContentView(R.layout.sana_thread);
			//initMetaData();
			String selected = getString(R.string.dpgram_right);
			selectAndRunThread(selected);
		} else {
			setContentView(R.layout.thread);
			// set listener for menu items
			ListView menuList = (ListView) findViewById(R.id.menu_list);
			menuList.setOnItemClickListener(
					new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

							TextView item = (TextView) itemClicked;
							String itemText = item.getText().toString();
							selectAndRunThread(itemText);

						}});
		}
        
	}
    
	private final void selectAndRunThread(String selected){

		if (selected.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
			plotWaveform();
		} else if(selected.equalsIgnoreCase(getResources().getString(R.string.dpgram_right)) ||
				selected.equalsIgnoreCase(getResources().getString(R.string.dpgram_left))) {
			//TODO: plot audiogram results
			//Generate list of tests to run
			List<String> RunTest= new ArrayList<String>();
			
			RunTest.add(getResources().getString(R.string.dpoae_2k));
			//RunTest.add(getResources().getString(R.string.menu_3k));
			//RunTest.add(getResources().getString(R.string.menu_4k));
			for(String runme: RunTest){
				emptyText(); //Clear text for new stimuli test and spectral plotting

				Log.d(TAG, "playRecordThread() => Starting");
				playRecordThread(runme,false);
				Log.d(TAG, "playRecordThread() => Complete?");
				//TODO: Implement a hold between playing threads
			}
			
			//TODO: Extract these results from data!
			double[] DPOAEData={7.206, -7, 5.083, 13.1,3.616, 17.9,2.542, 11.5,1.818, 17.1};
	        double[] noiseFloor={7.206, -7-10,5.083, 13.1-10,3.616, 17.9-10,2.542, 11.5-10,1.818, 17.1-10};
	        double[] f1Data={7.206, 64,5.083, 64,3.616, 64,2.542, 64,1.818, 64};
	        double[] f2Data={7.206, 54.9,5.083, 56.6,3.616, 55.6,2.542, 55.1,1.818, 55.1};

	        //double[] Pxx=SignalProcessing.getDPOAEResults(audioBundle);
	        
			Bundle DPGramresults= new Bundle();
			DPGramresults.putString("title",selected);
			DPGramresults.putDoubleArray("DPOAEData",DPOAEData);
			DPGramresults.putDoubleArray("noiseFloor",noiseFloor);
			DPGramresults.putDoubleArray("f1Data",f1Data);
			DPGramresults.putDoubleArray("f2Data",f2Data);
			
		}
		else {
			emptyText(); //Clear text for new stimuli test and spectral plotting
			playRecordThread(selected,true);
		} 
		
	
	}
	
	private RecordThreadRunnable playRecordThread(String item_selected, boolean showSpectrum)
	{
		
		//Ignore playing thread when obtaining SOAEs
		beginTest();	
		Context context=this.getApplicationContext();		
		
		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context,item_selected);
		
		if(item_selected.equalsIgnoreCase(getResources().getString(R.string.soae)) ){
			ExecutorService execSvc = Executors.newFixedThreadPool( 1 );
			rRun.setExpectedFrequency(0);
			recordThread = new Thread(rRun);	
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.shutdown();
		}else{
			
			//quick hack. depracated anyway.
			int f;
			if (item_selected.equalsIgnoreCase(getResources().getString(R.string.menu_3k)))
				f = 3;
			else if (item_selected.equalsIgnoreCase(getResources().getString(R.string.menu_4k)))
				f = 4;
			else
				f = 2;

					
			playStatusBackHandler = new ReportStatusHandler(this);
			PlayThreadRunnable pRun = new PlayThreadRunnable(playStatusBackHandler,playTime, f);
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


	@Override
	public void startTest(View callingView) {
		// TODO Auto-generated method stub
		
	}
	
	protected void startMockTest(){
		String path =Constants.MEDIA_PATH +"DPOAE.jpg";
		File data = new File(path);
		data.mkdirs();
		//this.setResultOkAndData(Uri.fromFile(data));
	}
	
	public void appendData(Bundle b){
		Uri output = b.getParcelable("outfile");
		outFiles.add(output);
		// TODO remove when done testing
		Log.d(TAG, "Setting output " + output);
		//this.setResult(Result.OK, output);
		//this.setResultOkAndData(output);
	}
	
	protected void onPause(){
		super.onPause();
		Log.d(TAG, "onPause()");
	}
}