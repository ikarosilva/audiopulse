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
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadedPlayRecActivity extends AudioPulseRootActivity 
{
	public static final String TAG="ThreadedPlayRecActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	AudioManager audioManager;
	Bundle audio_bundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=0.250;
	public static long playRecDelay=0; //Delay time between beginning of recording and begining of play in ms
										  //The recording will start first, wait playRecDelay ms, and then playback will start
										  //value should not be set to more than 500 (500 ms).
	public Bundle audioResultsBundle;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
		
		// set listener for main menu items
		ListView menuList = (ListView) findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(
        	new AdapterView.OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        			
        			TextView item = (TextView) itemClicked;
        			String itemText = item.getText().toString();
        			int itemId = item.getId();
        			
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
        				plotWaveform();
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_clear))) {
        				emptyText();
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_play))) {
        				setAirplaneMode(true);
        				//TODO: wait until airplane mode is set?
        				//TODO: read airplane mode state onCreate of main activity, turn if on, then restore state onDestroy.
        				playRecordThread();
        				
        			}
        			
        			Log.v(TAG,"Clicked item ID: " + Integer.toString(itemId));

        		}
        	}
		);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater(); //from activity
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		appendMenuItemText(item);
		if (item.getItemId() == R.id.menu_clear)
		{
			emptyText();
			return true;
		}
		if (item.getItemId() == R.id.menu_play_thread)
		{
			Log.v(TAG,"Starting execution of thread pool");
			playRecordThread();
			return true;
		}
		if(item.getItemId() == R.id.plot_waveform)
		{
			plotWaveform();
			return true;
		}
		if(item.getItemId() == R.id.menu_stimulus) {
			editStimulusSettings();
			return true;
		}
		return false;
	}

	private TextView getTextView(){
		return (TextView)this.findViewById(R.id.text1);
	}
	public void appendText(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + str);
	}
	private void appendMenuItemText(MenuItem menuItem){
		String title = menuItem.getTitle().toString();
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + title);
	}
	private void emptyText(){
		TextView tv = getTextView();
		tv.setText("");
	}

	private void playRecordThread()
	{
		playStatusBackHandler = new ReportStatusHandler(this);
		recordStatusBackHandler = new ReportStatusHandler(this);
		Context context=this.getApplicationContext();
		
		//Set the audio properties for play and recording
		//audioManager = (AudioManager) context.getSystemService(android.content.Context.AUDIO_SERVICE);
		//audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
		//		audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),);
		
		ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
		playThread = 
				new Thread(
						new PlayThreadRunnable(playStatusBackHandler,playTime));
		recordThread = 
				new Thread(
						new RecordThreadRunnable(recordStatusBackHandler,playTime+2*playRecDelay,2*playRecDelay, context));

		recordThread.setPriority(Thread.MAX_PRIORITY);
		Log.v(TAG,"Executing thread pool");
		execSvc.execute( recordThread );

		try {
			Thread.sleep(playRecDelay*1000); //playRecDelay is in seconds, convert to ms for sleep
		} catch(InterruptedException e) {
		} 

		execSvc.execute( playThread );
		execSvc.shutdown();

	}

	public void plotSpectrum() {
		Log.v(TAG,"Sample rate is " + audioResultsBundle.getFloat("recSampleRate"));
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}

	public void plotWaveform() {
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	
	public void editStimulusSettings() {
		Log.v(TAG,"Calling view to edit stimulus settings");
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	

}