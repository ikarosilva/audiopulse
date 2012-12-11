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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

//TODO: change the scope of this activity's purpose.
//this activity will be a base class for test procedures (rename it TestProcedureActivity?)
//and will have a default layout: text log, quick graph of input & output
//other tests procedures can extend this, define their own layouts
//e.g. define a DPOAE activity that has an online DPgram display
//e.g. calibration activity that has an I/O display
//this activity and extended classes will be launched from TestMenuActivity.

//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.

public abstract class GeneralAudioTestActivity extends AudioPulseActivity 
{
	public static final String TAG="GeneralAudioTestActivity";

	private Bundle audioResultsBundle;

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
		setContentView(R.layout.basic_test_layout);

		// // to perform the test automatically rather than by a button press:
		//findViewById(android.R.id.content).invalidate();
		//performTest();
		//TODO: We should always have an option to do the test automatically (perhaps by reading from
		//the context in which the activity was called
	}

	//begin test. Generally, this function is called by a ButtonView in the layout.
	public abstract void startTest(View callingView);
	{

		//perform test	
		//plot results

	}

	//TODO: implement a focused "playRecordThread" function that simply takes params for what to play and what
	// to record, so that the base class implementation can be called from all extended GeneralAudioTestActivity classes.
	protected RecordThreadRunnable playRecordThread()
	{
		//This is the most generic form. Some specific tests could overwrite this for optimization.
		//For example, for SOAE there is no need to play a stimulus, so the play thread can be omitted.

		beginTest();
		Context context=this.getApplicationContext();		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context);

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

		endTest();
		return rRun;
	}


	//TODO: not all of these functions are general to GeneralAudioTestActivity but instead specific to DPOAEActivity
	//NOTE(by Ikaro): Maybe we should still leave these functions here in general form, so that they can be used by other 
	//activities, if we think that they are not being used by most children, than we can push them into more specific classes.
	//But, in general if two or more children use these function, it best to keep it here.
	public void appendText(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + str);
	}

	public void emptyText(){
		TextView tv = getTextView();
		tv.setText("");
	}

	private TextView getTextView(){
		return (TextView)this.findViewById(R.id.text1);
	}

	//plot recorded signal spectrum
	public void plotSpectrum(Bundle audioResultsBundle) {
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(audioResultsBundle);
		this.audioResultsBundle=audioResultsBundle;
		startActivity(intent);
	}

	//plot recorded waveform
	public void plotWaveform() {
		//TODO: Add check for not null audioResultsBundle (notify user that to run stimulus if they press this option before running anything).
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}

}