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
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;
import org.audiopulse.utilities.AudioSignal;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DeviceCalibrationActivity extends GeneralAudioTestActivity implements OnSeekBarChangeListener

{
	public static final String TAG="DeviceCalibrationActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=0.5;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);
	
	int[] toneFrequencies = {500, 1000, 2000, 4000, 8000};
	int[] amplitudes = {-60, -40, -20, 0};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_calibration);
		
		SeekBar frequencyBar = (SeekBar) findViewById(R.id.calibration_frequency_bar);
		SeekBar amplitudeBar = (SeekBar) findViewById(R.id.calibration_amplitude_bar);
		//set frequency and amplitude bar properties
		frequencyBar.setMax(toneFrequencies.length-1);
		amplitudeBar.setMax(amplitudes.length-1);
		setFrequency();
		setAmplitude();
		//set this class to be the listener for the bars. That's why this class "implements OnSeekBarChangeListener".
		frequencyBar.setOnSeekBarChangeListener(this);
		amplitudeBar.setOnSeekBarChangeListener(this);
		
		
		//findViewById(android.R.id.content).invalidate();
        //performTest();			
	}
    
	public void toggleSound(View view) {
		if (((ToggleButton)view).isChecked()) {
				
			//TODO: make this toggle a continuous sound. For now it will just play a sound.
			beginTest();
			
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
	
			endTest();
		} else {
			
		}
		
		
	}
	
	public void startTest(View callingView){
		
		// first task: play a tone. yay!

		
	}
	
	private short[] generateStimulus() {
		double length = 2.000;	//stimulus length (sec)
		double ramp = 0.200;	//ramp length for onset/offset (sec)
		int Fs = 44100;			//sample rate
		double f = getFrequency();		//stimulus frequency (Hz) 
		double a = SignalProcessing.dB2lin(getAmplitude());			//stimulus amplitude]
		
		int N = (int) (length * Fs);
		int N_ramp = (int) (ramp*Fs);
		double w = 2 * Math.PI * f;
		double[] x = new double[N];
		
		//create signal
		for( int n = 0; n < N; n++ )
		{
			x[n] = a * Math.sin(w*n/Fs);
		}
		
		//apply ramp on/off
		for (int n=0; n<N_ramp; n++) {
			double r = (double)(n)/(double)(N_ramp);
			x[n] *= r;
			x[x.length-n-1] *= r;
		}
		
		return AudioSignal.getAudioTrackData(x,true);

	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int barId = seekBar.getId();
		if (barId == R.id.calibration_amplitude_bar) {
			setAmplitude();
		} else if (barId == R.id.calibration_frequency_bar) {
			setFrequency();
		}
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		int value = seekBar.getProgress();
		
	}
	
	public void setFrequency() {
		TextView text = (TextView)findViewById(R.id.calibration_frequency_text);
		text.setText(getString(R.string.frequency) + ": " + getFrequency() + " Hz");
	}
	public void setAmplitude () {
		TextView text = (TextView)findViewById(R.id.calibration_amplitude_text);
		text.setText(getString(R.string.amplitude) + ": " + getAmplitude() + "dBfs");
	}
	
	private int getFrequency(){
		int freq_ind = ((SeekBar)findViewById(R.id.calibration_frequency_bar)).getProgress();
		assert freq_ind < toneFrequencies.length;
		return toneFrequencies[freq_ind];
	}
	private int getAmplitude(){
		int amp_ind = ((SeekBar)findViewById(R.id.calibration_amplitude_bar)).getProgress();
		assert amp_ind < amplitudes.length;
		return amplitudes[amp_ind];		
	}
	
}