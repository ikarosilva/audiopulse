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
import java.util.MissingResourceException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.AudioStreamer;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;
import org.audiopulse.io.Utils;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.SpectralWindows;
import org.audiopulse.utilities.ThreadedClickGenerator;
import org.audiopulse.utilities.ThreadedNoiseGenerator;
import org.audiopulse.utilities.ThreadedSignalGenerator;
import org.audiopulse.utilities.ThreadedToneGenerator;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class OutputCalibrationActivity extends TestActivity
									implements OnSeekBarChangeListener, OnItemSelectedListener 

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
	
	AudioStreamer player;
	ThreadedSignalGenerator source;
	
	double[] toneFrequencies = {500, 1000, 2000, 4000, 8000};
	double[] clickFrequencies = {5, 10, 20, 30, 40};
	double[] amplitudes = {.2, .4, .6, .8, 1};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_calibration);
		
		//create audio streaming device with 1/4 second buffer
		player = new AudioStreamer(super.playbackSamplingFrequency, 
				super.playbackSamplingFrequency/4);
		
		Spinner sourceSpinner = (Spinner) findViewById(R.id.calibration_source);
		sourceSpinner.setOnItemSelectedListener(this);
		
		//set frequency and amplitude bar properties
		SeekBar frequencyBar = (SeekBar) findViewById(R.id.calibration_frequency_bar);
		SeekBar amplitudeBar = (SeekBar) findViewById(R.id.calibration_amplitude_bar);
		frequencyBar.setMax(toneFrequencies.length-1);
		amplitudeBar.setMax(amplitudes.length-1);
		
		//set this class to be the listener for the bars. That's why this class "implements OnSeekBarChangeListener".
		frequencyBar.setOnSeekBarChangeListener(this);
		amplitudeBar.setOnSeekBarChangeListener(this);
				
		attachSource();
	}
	
	
    
	public void toggleSound(View view) {
		if (((ToggleButton)view).isChecked()) {
			beginTest();	
			player.start();
		} else {
			player.stop();
			endTest();
		}
	}
	
	public void startTest(View callingView){
	}
	
	//Read source from spinner, create this source as new.
	private void attachSource() {
		boolean wasPlaying = player.isPlaying();
		if (wasPlaying) player.stop();
		
		//create new source object
		String sourceName = getSource();
		if (sourceName.equals(getString(R.string.calibration_tone))) {
			source = new ThreadedToneGenerator(player.getFrameLength(), getFrequency(), super.playbackSamplingFrequency);			
		} else if (sourceName.equals(getString(R.string.calibration_noise))) {
			source = new ThreadedNoiseGenerator(player.getFrameLength(), super.playbackSamplingFrequency);
		} else if (sourceName.equals(getString(R.string.calibration_clicks))) {
			source = new ThreadedClickGenerator(player.getFrameLength(), getFrequency(), super.playbackSamplingFrequency);
		} else throw new MissingResourceException("Unknown source selected", sourceName, sourceName);

		updateSource(); 				//update parameters from UI
		source.initialize();			//initialize source (create first buffer);
		player.attachSource(source);	//attach to player object
		
		if (wasPlaying) player.start();
		Log.d(TAG,"Source created: " + sourceName);
		
	}
	
	//update source parameters from UI values
	private void updateSource() {
		SeekBar frequencyBar = (SeekBar) findViewById(R.id.calibration_frequency_bar);
		Class<? extends ThreadedSignalGenerator> sourceClass =  source.getClass();
		if (sourceClass == ThreadedToneGenerator.class) {
			frequencyBar.setMax(toneFrequencies.length-1);
			frequencyBar.setVisibility(View.VISIBLE);
			((ThreadedToneGenerator) source).setFrequency(getFrequency());
			((ThreadedToneGenerator) source).setAmplitude(getAmplitude());
		} else if (sourceClass == ThreadedNoiseGenerator.class) {
			frequencyBar.setVisibility(View.INVISIBLE);
			((ThreadedNoiseGenerator) source).setAmplitude(getAmplitude());
		} else if (source.getClass() == ThreadedClickGenerator.class) {
			frequencyBar.setMax(clickFrequencies.length-1);
			frequencyBar.setVisibility(View.VISIBLE);
			((ThreadedClickGenerator) source).setFrequency(getFrequency());
			((ThreadedClickGenerator) source).setAmplitude(getAmplitude());
		}
		source.recompute();
		
	}
	
	public void updateDisplay() {
		TextView text;
		text = (TextView)findViewById(R.id.calibration_frequency_text);
		text.setText(getString(R.string.frequency) + ": " + getFrequency() + " Hz");
		
		text = (TextView)findViewById(R.id.calibration_amplitude_text);
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

	private double getAmplitude(){
		int amp_ind = ((SeekBar)findViewById(R.id.calibration_amplitude_bar)).getProgress();
		assert amp_ind < amplitudes.length;
		return amplitudes[amp_ind];		
	}

	private String getSource(){
		return ((Spinner)findViewById(R.id.calibration_source)).getSelectedItem().toString();
	}
	
	//functions to allow calibration routine to automatically set stimulus parameters
	private void setSource(){
	}
	private void setFrequency(){		
	}
	private void setAmplitude(){
	}
	
	// --- implementations for OnSeekBarChangeListener --- //
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		updateSource();
		updateDisplay();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		//do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		//do nothing
	}

	// --- implementations for OnItemSelectedListener --- //
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		attachSource();
		updateDisplay();
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// do nothing
	}
		
}