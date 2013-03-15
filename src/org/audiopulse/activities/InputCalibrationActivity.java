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

import org.audiopulse.R;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.utilities.AudioSignal;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class InputCalibrationActivity extends TestActivity implements Handler.Callback

{
	public static final String TAG="InputCalibrationActivity";
	
	private InputProcessor recorder;		

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_calibration);
	
		//launch recorder thread
		recorder = new InputProcessor(this);
		new Thread (recorder).start();
	}

	@Override
	protected void onDestroy() {
		recorder.stop();
		super.onDestroy();
	}

	// implement handleMessage
	@Override
	public boolean handleMessage(Message msg) {
		Log.d(TAG,"Message received! Printing stats");
		TextView text;
		
		Bundle data = msg.getData();
		double spl = data.getDouble("spl");
		double min = data.getDouble("min");
		double max = data.getDouble("max");
		double pp = data.getDouble("pp");
		double rms = data.getDouble("rms");
		double clip = data.getDouble("clip");
		text = (TextView)findViewById(R.id.input_level_meter);
		text.setText(String.format(" %.1f dB SPL",spl));
		text = (TextView)findViewById(R.id.input_rms_meter);
		text.setText(String.format(" %.1f fpu",rms));
		text = (TextView)findViewById(R.id.input_pp_meter);
		text.setText(String.format(" %.2f fpu",pp));
		text = (TextView)findViewById(R.id.input_max_meter);
		text.setText(String.format(" %.2f fpu",max));
		text = (TextView)findViewById(R.id.input_min_meter);
		text.setText(String.format(" %.2f fpu",min));
		text = (TextView)findViewById(R.id.input_clip_meter);
		text.setText(String.format(" %.0f%%",clip));
		return true;
	}
	
	//continuously running input monitor
	private class InputProcessor implements Runnable
	{
		private Handler handler;
		private AudioRecord recorder;
		private AcousticConverter converter = new AcousticConverter();
		private volatile boolean requestStop = false;
		
		private int computeLength = 4096;
		private int runningBufferLength = 10000;
		private int readLength = 1024;
		
		private int bufferIndex = 0;		//current index into runningBuffer
		private double[] runningBuffer;		//dump read samples into this buffer
		private double[] computingBuffer;	//send this buffer to compute stats for display
		
		//constructor
		public InputProcessor(InputCalibrationActivity parentActivity) {
			Log.d(TAG,"Creating InputProcessor");
			int minBuffer = AudioRecord.getMinBufferSize(
					parentActivity.recordingSamplingFrequency,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT
					);
			int bufferSizeInBytes = runningBufferLength * 2;
			if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
			
			Log.d(TAG,"Recording buffer length: " + bufferSizeInBytes + "bytes");
			
			//create AudioRecord object
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					parentActivity.recordingSamplingFrequency,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes);
			
			//assign handler to be parent activity (which implements handleMessage)
			handler = new Handler(parentActivity);
		}
		
		//start the record handling
		public void run() {
			Log.d(TAG,"Starting InputProcessor");
			runningBuffer = new double[runningBufferLength];
			short[] frameBuffer = new short[readLength];
			recorder.startRecording();
			requestStop = false;
			
			while(!requestStop){ 
				//read from hardware
				int nRead=recorder.read(frameBuffer,0,readLength);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					//TODO: send a useful message to main activity informing of failure
				}
				
				//copy read frame into runningBuffer
				//TODO: we don't need to read every data sample if we only compute on a few of them
				for (int n=0; n<nRead ; n++) {
					runningBuffer[bufferIndex] = AudioSignal.convertSampleToDouble(frameBuffer[n]);
					bufferIndex++;
					if (bufferIndex >= runningBufferLength) {
						Log.d(TAG,"End of buffer reached.");
						break;
					}
				}
				//if we've reached the end of runningBuffer (hack method of timing for now), perform compute
				if (bufferIndex>=runningBufferLength) {
					bufferIndex = 0;
					final int start = runningBufferLength-computeLength;
//					//compute in new thread to not interrupt recording process
//					new Thread( new Runnable() {
//						public void run() {
							
							computeBufferStats(runningBuffer,start,computeLength);
//						}
//					}).start();					
				}
				
			}
			recorder.stop();
		}
		
		public void stop() {
			requestStop = true;
		}
		
		//compute stats on section of buffer
		private void computeBufferStats(double[] buffer, int start, int length) {
			Log.d(TAG,"Computing stats");
			
			double min = 1, max = -1;
			double rms = 0, spl = 0; 
			double mean = 0;
			double nClipped = 0;

			//compute stats
			for (int n=start; n < start+length; n++) {
				double currentSample = buffer[n];
				if (currentSample<min) min=currentSample;
				if (currentSample>max) max=currentSample;
				rms = rms + (currentSample*currentSample) / length;
				mean = mean + currentSample/length;
				if (Math.abs(currentSample)==1) nClipped++;
			}
			Log.d(TAG,"Finished buffer compute loop");
			rms = Math.sqrt(rms);
			spl = converter.getInputLevel(rms);
			
			//put stats into message, send back to UI thread
			Bundle msgData = new Bundle();
			msgData.putDouble("max", max);
			msgData.putDouble("min", min);
			msgData.putDouble("pp", max-min);
			msgData.putDouble("rms", rms);
			msgData.putDouble("spl", spl);
			msgData.putDouble("clip", (100*nClipped)/length);
			Message msg = handler.obtainMessage();
			msg.setData(msgData);
			handler.sendMessage(msg);
		}
	}
	
}