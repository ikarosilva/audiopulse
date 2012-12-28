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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DeviceCalibrationActivity extends GeneralAudioTestActivity implements OnSeekBarChangeListener//, OnItemSelectedListener 

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
	
	double[] toneFrequencies = {500, 1000, 2000, 4000, 8000};
	double[] clickFrequencies = {1, 2, 5, 10, 20};
	double[] amplitudes = {.2, .4, .6, .8, 1};
	
	int Fs = 44100; 		//TODO: make this app-wide

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_calibration);
		
		//Spinner sourceSpinner = (Spinner) findViewById(R.id.calibration_source);
		//sourceSpinner.setOnItemSelectedListener(this);
		
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
			
			/*
			 * GenerateThread(PlayThreadRunnable)
			 * GenerateThread(StimulusGeneratorRunnable)
			 * ExecuteThreads
			 * 
			 * StimuluGeneratorRunnable(Fs, blockSize, anonnymousStimulusFunction) {
			 * 
			 * loop {
			 * x = generateStimulusBlock();
			 * waitUntilPlayThreadNeedsMoreData();
			 * if checkForTerminateSignal(), terminate();
			 * writeStimulusBlock(x);
			 * }
			 * } 
			 */
			
			short[] stimulus = this.generateStimulus();

			// BEGIN: all this should be a function call
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
			// END
			
	
			endTest();
		} else {
			
		}
		
		
	}
	
	public void startTest(View callingView){
		
		// first task: play a tone. yay!

		
	}
	
	private short[] generateStimulus() {

		double[] x = {0};
		
		String source = getSource();
		if (source.equals(getString(R.string.calibration_tone))) {
			x = generateTone();
		} else if (source.equals(getString(R.string.calibration_noise))) {
			x = generateNoise();
		} else if (source.equals(getString(R.string.calibration_clicks))) {
			x = generateClicks();
		} else { assert false : "Unknown calibration source selected";}
		
		return AudioSignal.getAudioTrackData(x, true);	//TODO: determine stereo output
	}
	
	private double[] generateTone() {
		double length = 2.000;	//stimulus length (sec)
		double f = getFrequency();		//stimulus frequency (Hz) 
		double a = getAmplitude();			//stimulus amplitude]
		
		int N = (int) (length * Fs);
		double w = 2 * Math.PI * f;
		double[] x = new double[N];
		
		//create signal
		for( int n = 0; n < N; n++ )
		{
			x[n] = a * Math.sin(w*n/Fs);
		}
		
		x = applyRamp(x);
				
		return x;
	}
	
	private double[] generateNoise() {
		double length = 2.000;	//stimulus length (sec)
		double a = getAmplitude();			//stimulus amplitude]
		
		int N = (int) (length * Fs);
		double[] x = new double[N];
		
		//create signal
		for( int n = 0; n < N; n++ )
		{
			x[n] = a * 2*(Math.random()-1);
		}
		
		x = applyRamp(x);
		
		return x;
	}
	
	private double[] generateClicks() {
		//TODO: make this
		double length = 2.000;	//stimulus length (sec)
		double f = getFrequency();		//click frequency (Hz) 
		double a = getAmplitude();			//stimulus amplitude]
		
		int N = (int) (length * Fs);
		double[] x = new double[N];
		int period = (int) (1/f * Fs);
		
		//create signal
		for( int n = 0; n < N; n++ )
		{
			x[n] = (n%period == 0) ? a:0;
		}
		
		//no ramp for clicks
				
		return x;
	}
	
	private double[] applyRamp(double[] x) {
		double ramp = 0.200;	//ramp length for onset/offset (sec)
		int N_ramp = (int) (ramp*Fs);

		for (int n=0; n<N_ramp; n++) {
			double r = (double)(n)/(double)(N_ramp);
			x[n] *= r;
			x[x.length-n-1] *= r;
		}
		
		return x;
		
	}
	
	public void setFrequency() {
		TextView text = (TextView)findViewById(R.id.calibration_frequency_text);
		text.setText(getString(R.string.frequency) + ": " + getFrequency() + " Hz");
	}
	public void setAmplitude () {
		TextView text = (TextView)findViewById(R.id.calibration_amplitude_text);
		text.setText(getString(R.string.amplitude) + ": " + getAmplitude());
	}
	
	//get frequency, depends on stimulus type
	private double getFrequency(){
		String source = getSource();
		if (source.equals(getString(R.string.calibration_tone))) {
			int freq_ind = ((SeekBar)findViewById(R.id.calibration_frequency_bar)).getProgress();
			assert freq_ind < toneFrequencies.length;
			return toneFrequencies[freq_ind];			
		} else if (source.equals(getString(R.string.calibration_noise))) {
			return 0;
		} else if (source.equals(getString(R.string.calibration_clicks))) {
			int freq_ind = ((SeekBar)findViewById(R.id.calibration_frequency_bar)).getProgress();
			assert freq_ind < clickFrequencies.length;
			return clickFrequencies[freq_ind];						
		} else { assert false : "Unknown calibration source selected"; return 0;}
	}
	private double getClickFrequency(){
		int freq_ind = ((SeekBar)findViewById(R.id.calibration_frequency_bar)).getProgress();
		assert freq_ind < toneFrequencies.length;
		return toneFrequencies[freq_ind];
	}
	private double getToneFrequency(){
		int freq_ind = ((SeekBar)findViewById(R.id.calibration_frequency_bar)).getProgress();
		assert freq_ind < toneFrequencies.length;
		return toneFrequencies[freq_ind];
	}
	private double getAmplitude(){
		int amp_ind = ((SeekBar)findViewById(R.id.calibration_amplitude_bar)).getProgress();
		assert amp_ind < amplitudes.length;
		return amplitudes[amp_ind];		
	}

	private String getSource(){
		return ((Spinner)findViewById(R.id.calibration_source)).getSelectedItem().toString();
	}
	private void setSource(){
		
	}
	// --- implementations for OnSeekBarChangeListener --- //
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

	// --- implementations for OnItemSelectedListener --- //
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}