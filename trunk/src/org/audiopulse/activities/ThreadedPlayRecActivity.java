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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadedPlayRecActivity extends AudioPulseRootActivity 
{
	public static final String TAG="ThreadedPlayRecActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=2;
	//public Bundle audioResultsBundle;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
		
		// set listener for menu items
		ListView menuList = (ListView) findViewById(R.id.single_test_menu_list);
        menuList.setOnItemClickListener(
        	new AdapterView.OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        			
        			TextView item = (TextView) itemClicked;
        			String itemText = item.getText().toString();
        			
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
        				plotWaveform();
        			} else {
        				emptyText(); //Clear text for new stimuli test and spectral plotting
        				playRecordThread(itemText);
        			} 
        			
        		}
        	}
		);
	}
    
	
	private void playRecordThread(String item_selected)
	{
		
		//Ignore playing thread when obtaining SOAEs
		beginTest();	
		Context context=this.getApplicationContext();		
		
		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context);
		
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
			rRun.setExpectedFrequency(pRun.getExpectedFrequency());
			recordThread = new Thread(rRun);	
			playThread.setPriority(Thread.MAX_PRIORITY);
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.execute( playThread );
			execSvc.shutdown();
		}
		endTest();
	}

}