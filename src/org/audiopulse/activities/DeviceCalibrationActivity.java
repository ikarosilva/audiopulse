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
import org.audiopulse.utilities.DPOAESignal;
import org.audiopulse.utilities.PeriodicSeries;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.SpectralWindows;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceCalibrationActivity extends GeneralAudioTestActivity //change this  BACK to TestActivity fuckdamnit

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dpoae);
		//findViewById(android.R.id.content).invalidate();
		
        //performTest();			
	}
    
	public void startTest(View callingView){
		
		// first task: play a tone. yay!
		//beginTest();
		/*
		short[] stimulus = this.generateStimulus();
		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,getApplicationContext());
		
		playStatusBackHandler = new ReportStatusHandler(this);
		PlayThreadRunnable pRun = new PlayThreadRunnable(playStatusBackHandler,stimulus);
		ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
		playThread = new Thread(pRun);
		recordThread = new Thread(rRun);	
		playThread.setPriority(Thread.MAX_PRIORITY);
		recordThread.setPriority(Thread.MAX_PRIORITY);
		execSvc.execute( recordThread );
		execSvc.execute( playThread );
		execSvc.shutdown();

		//endTest();
		 * */
		 
		
	}
	
	private short[] generateStimulus() {
		double length = 1.000;	//stimulus length (sec)
		double ramp = 0.100;	//ramp length for onset/offset (sec)
		int Fs = 44100;			//sample rate
		double f = 1000;		//stimulus frequency (Hz) 
		double a = (double) Short.MAX_VALUE;			//stimulus amplitude
		
		int N = (int) (length * Fs);
		int N_ramp = (int) (ramp*Fs);
		double w = 2 * Math.PI * f;
		
		short[] x = new short[2*N];

		//create signal
		for( int n = 0; n < N; n++ )
		{
			x[2*n] = x[2*n+1] = (short) (a * Math.sin(w*n));
		}
		
		//apply ramp on/off
		for (int n=0; n<N_ramp; n++) {
			double r = (double) (n/N_ramp);
			x[2*n] = (short) ((double) (x[2*n])*r);
			x[2*n+1] = (short) ((double) (x[2*n+1])*r);
			x[x.length-2*n-1] = (short) ((double) (x[x.length-2*n-1])*r);;
			x[x.length-(2*n+1)-1] = (short) ((double) (x[x.length-(2*n+1)-1])*r);;
		}
		return x;

	}
	
	
	
}