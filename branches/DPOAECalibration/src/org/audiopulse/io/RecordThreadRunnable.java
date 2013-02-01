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

package org.audiopulse.io;
import java.io.File;
import java.io.IOException;
import java.util.Date;


import org.audiopulse.utilities.SignalProcessing;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


//TODO: Translate all message (informMiddle informEnd) Strings into the Resource folder for easy changes to other languages
public class RecordThreadRunnable implements Runnable
{
	private static final String TAG="RecordThreadRunnable";
	private double record_time;
	private int soundCardBufferSize;
	private static int soundCardBufferSizeScale=5;
	private AudioRecord mAudio;
	final static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	final static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	final static int recSource=MediaRecorder.AudioSource.MIC;
	private int sampleRate=8000;
	private int Buffer_Size;
	private double expectedFrequency; 
	final short[] samples ;
	Double recordRMS;
	Handler mainThreadHandler = null;
	private Bundle results;
	public int clipped;
	
	//TODO: handle external storage unavailability
	private static File root = Environment.getExternalStorageDirectory();
	
	Context context;
	private String testType;
	private boolean showSpectrum;
	private File outFile =null;
	
	public RecordThreadRunnable(Handler h, double playTime,Context context)
	{
		Log.v(TAG,"constructing record thread");
		mainThreadHandler = h;
		Buffer_Size=(int) (playTime*sampleRate); 
		samples = new short[Buffer_Size];
		initRecord();
		this.context=context;

	}

	public RecordThreadRunnable(Handler h, double playTime,Context context, String itemSelected)
	{
		Log.v(TAG,"constructing record thread");
		mainThreadHandler = h;
		Buffer_Size=(int) (playTime*sampleRate); 
		samples = new short[Buffer_Size];
		initRecord();
		this.context=context;
		testType = itemSelected;
	}
	
	public void setExpectedFrequency(double eFrequency){
		expectedFrequency=eFrequency;
	}

	public synchronized void run()
	{
		informStart();
		//Record Stimulus
		android.media.AudioManager mgr = (android.media.AudioManager) context.getSystemService(android.content.Context.AUDIO_SERVICE);
		int streamVolume = mgr.getStreamVolume(android.media.AudioManager.STREAM_MUSIC); 
		if(streamVolume != 15){
		informMiddle("Warning: volume is set to: " + streamVolume);
		}
		informMiddle("Recording, please wait...");
		record();

		// Write file to disk
		// Define file name here beacause inform finish is adding the Uri to the message bundle 
		// TODO Does the bundling need to happen post SHortfile.writeFile
		String fileName="AP_" + testType + new Date().toString()+".raw";
		outFile = new File(root, fileName.replace(" ","-").replace(":", "-") ); 
		
		//Finish up
		informFinish();

		Log.d(TAG, "outFile => "+ outFile.getAbsolutePath());
		informMiddle("Saving file: "+ outFile.getAbsolutePath());
		try {
			ShortFile.writeFile(outFile,samples);
		} catch (IOException e) {	
			informMiddle("Error in saving file: ");
			informMiddle(e.getLocalizedMessage());
		}
		informMiddle("Finished!");

	}

	public void informMiddle(String str)
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.mainThreadHandler.sendMessage(m);
	}

	public void informStart()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Recording for: " + (double) Buffer_Size/sampleRate +" s"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		mAudio.release();
		Message m = this.mainThreadHandler.obtainMessage();
		results= new Bundle();
		String msg="Release time= " + record_time/1000 + " seconds. RMS = " + recordRMS +
				" eFrequency= "+ expectedFrequency;
		results.putString("message", msg);
		results.putShortArray("samples",samples);
		results.putFloat("recSampleRate",sampleRate);
		results.putLong("N",(long) samples.length);
		results.putInt("clipped",clipped);
		results.putDouble("recordRMS",recordRMS);
		results.putDouble("expectedFrequency",expectedFrequency);
		results.putBoolean("showSpectrum",showSpectrum);
		// TODO use the final zip file uri instead of the raw file
		Uri output = (outFile != null)? Uri.fromFile(outFile):
						Uri.EMPTY;
		Log.d(TAG, "Output Uri: " + output);
		results.putParcelable("outfile", output);
	    
		m.setData(results);
		this.mainThreadHandler.sendMessage(m);
	}

	private void initRecord(){
		//Log.v(TAG,"Initialized record track");
		try {
			soundCardBufferSize=AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
			mAudio = new AudioRecord(recSource,sampleRate,channelConfig,
					audioFormat, soundCardBufferSize*soundCardBufferSizeScale);
			mAudio.setPositionNotificationPeriod(soundCardBufferSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	

	}



	private synchronized void record() {
		//Log.v(TAG,"Starting recording of "+ this.samples.length +" samples through mAudio");
		int ind=0;
		boolean isActive=false;
		int endbuffer;
		int nRead=0;
		int minFrameSize=160;
		int frameSize=minFrameSize*4;
		int dataLeft=samples.length;
		//TODO: Implement a getNotificationMarkerPosition() to read 
		long st = System.currentTimeMillis();
		mAudio.startRecording();
		while(dataLeft>0){
			endbuffer=(frameSize<dataLeft) ? frameSize: dataLeft;
			//nRead=mAudio.read(this.samples,ind*frameSize,endbuffer);
			nRead=mAudio.read(samples,ind,endbuffer);
			if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
				Log.e(TAG, "Audio read failed: " + nRead);
				break;
			}

			//TODO: There is an initial zeroing in the data 
			//this is likely due to reading the initial soundcard buffer  that is not fullly filled.
			//FIXME: Should try to set a   a method using getPositionNotificationPeriod () to get  a cleaner working solution
			//for now just check until the first nonzero sample is achieved
			if(!isActive){
				for(int k=0;k<endbuffer;k++){
					//ind should be 0 ! This only runs in the initial stage
					if(samples[k] >0){
						isActive=true;
						//Shift data and set index
						for(int j=k;j<endbuffer;j++){
							samples[j-k]=samples[j];
						}
						ind=nRead-k-nRead; //the last term is needed in because of "ind +=nRead"; below
						dataLeft=dataLeft-(nRead-k)+nRead; 
						break;
					}
				}
			}
			if(isActive){
				dataLeft -= nRead;
				ind +=nRead;
			}
		}

		mAudio.stop();
		record_time = System.currentTimeMillis()-st;
		Log.v(TAG,"low level recording took: " + record_time/1000);
		recordRMS=SignalProcessing.rms(samples);
		Log.v(TAG,"recording RMS= " + recordRMS);		
	}

	// outFile added as raw output to pass to Sana
	// TODO change to use zipped file
	// TODO maybe remove entirely since pass Uri in bundle
	/**
	 *
	 */
	public File getOutFile(){
		
		return outFile;
	}
	
}
