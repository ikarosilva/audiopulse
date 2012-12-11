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
public class TestMenuActivity extends AudioPulseActivity 
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
        			//TODO: Tests should return a Bundle containing the data they are suppose to provide,
        			//and URLs to any file location they create
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
        				//TODO: change this to launch a plot activity
        				//plotWaveform();
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.dpgram_right)) ||
        					itemText.equalsIgnoreCase(getResources().getString(R.string.dpgram_left)) ||
        					itemText.equalsIgnoreCase(getResources().getString(R.string.dpoae_4k)) ||
        					itemText.equalsIgnoreCase(getResources().getString(R.string.dpoae_3k)) ||
        					itemText.equalsIgnoreCase(getResources().getString(R.string.dpoae_2k)) ) {
        				
        				Bundle DPOAERequest= new Bundle();
        				DPOAERequest.putString("testName",itemText);
        				Intent DPOAEIntent = new Intent(TestMenuActivity.this, DPOAEActivity.class);
        				DPOAEIntent.putExtras(DPOAERequest);
        				startActivity(DPOAEIntent);
        				
        			}
        			else {
        				//TODO: launch test activity
        			} 
        			
        		}
        	}
		);
	}
    
	
	
	
}