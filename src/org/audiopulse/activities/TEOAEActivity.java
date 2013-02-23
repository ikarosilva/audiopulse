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

import org.audiopulse.R;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.utilities.APAnnotations.UnderConstruction;
import org.audiopulse.utilities.AudioSignal;
import org.sana.android.Constants;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

@UnderConstruction(owner="Ikaro Silva")
public class TEOAEActivity extends TestActivity implements Handler.Callback

{
	private static final String TAG="TEOAEActivity";
	private InputProcessor recorder;
	public final int sampleFrequency = 44100; 		//TODO: make this app-wide

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
		
		//TODO: Get data from procedure and display the results in a graph
		Bundle data = msg.getData();
		
		//double spl = data.getDouble("spl");
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
		private int runningBufferLength = sampleFrequency;
		private int readLength = 1024;
		
		private int bufferIndex = 0;		//current index into runningBuffer
		private Short[] runningBuffer;		//dump read samples into this buffer
		
		//constructor
		public InputProcessor(TEOAEActivity parentActivity) {
			Log.d(TAG,"Creating InputProcessor");
			int minBuffer = AudioRecord.getMinBufferSize(
					parentActivity.sampleFrequency,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT
					);
			int bufferSizeInBytes = runningBufferLength * 2;
			if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
			
			Log.d(TAG,"Recording buffer length: " + bufferSizeInBytes + "bytes");
			
			//create AudioRecord object
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					parentActivity.sampleFrequency,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes);
			
			//assign handler to be parent activity (which implements handleMessage)
			handler = new Handler(parentActivity);
		}
		
		//start the record handling
		public void run() {
			Log.d(TAG,"Starting TEOAERecording");
			runningBuffer = new Short[runningBufferLength];
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
				for (int n=0; n<nRead ; n++) {
					runningBuffer[bufferIndex] = frameBuffer[n];
					bufferIndex++;
					if (bufferIndex >= runningBufferLength) {
						Log.d(TAG,"End of buffer reached.");
						this.stop();
						break;
					}
				}	
			if (bufferIndex>=runningBufferLength) {
					bufferIndex = 0;
					final int start = runningBufferLength-computeLength;				
			}
			}
			recorder.stop();
			//Save data to disk
			File file= AudioPulseFileWriter.generateFileName("TEOAE","click");
			Log.d(TAG,"Saving file to disk:" + file.getName());
			Thread fileWriter=new AudioPulseFileWriter(file,runningBuffer);
			fileWriter.start();
			
		}
		
		public void stop() {
			requestStop = true;
		}
		
	}
	
}