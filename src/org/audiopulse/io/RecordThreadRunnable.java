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
import org.audiopulse.utilities.SignalProcessing;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
	private int minFrameSize=160;
	private int IN_REC_MODE;
	final short[] samples ;
	Double recordRMS;
	Handler mainThreadHandler = null;
	private Bundle results;
	public int clipped;
	Context context;
	
	public RecordThreadRunnable(Handler h, double playTime,Context context)
	{
		Log.v(TAG,"constructing record thread");
		mainThreadHandler = h;
		Buffer_Size=(int) (1.2*playTime*sampleRate);  //TODO: remove this manual 1.2 factor magic!
		samples = new short[Buffer_Size];
		initRecord();
		IN_REC_MODE=0;
		this.context=context;
		Log.d(TAG,"Buffe_Size= " + Buffer_Size);
	}

	public synchronized void run()
	{
		//AudioManager maudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //float volume = (float) maudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		informStart();
		
		//Record Stimulus
		Log.d(TAG,"Recording stimulus");
		this.IN_REC_MODE=1;
		record();
		
		android.media.AudioManager mgr = (android.media.AudioManager) context.getSystemService(android.content.Context.AUDIO_SERVICE);
		int streamVolume = mgr.getStreamVolume(android.media.AudioManager.STREAM_MUSIC); 
		informMiddle("Volume is set to: " + streamVolume);
		this.IN_REC_MODE=0;
		informMiddle("RMS= " + recordRMS);
		//Finish up
		informFinish();
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
		String msg="Released recording in " + record_time/1000 + " seconds. RMS = " + recordRMS;
		results.putString("message", msg);
		results.putShortArray("samples",samples);
		results.putFloat("recSampleRate",sampleRate);
		results.putLong("N",(long) samples.length);
		results.putInt("clipped",clipped);
		results.putDouble("recordRMS",recordRMS);
		m.setData(results);
		this.mainThreadHandler.sendMessage(m);
		if(this.clipped==1){
			Log.v(TAG,"Recording was clipped!!");
			Message m2 = this.mainThreadHandler.obtainMessage();
			m2.setData(Utils.getStringAsABundle("Recording was clipped!!"));
			this.mainThreadHandler.sendMessage(m2);
		}
	}

	public int getRecMode(){
		return this.IN_REC_MODE;
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
		long st = System.currentTimeMillis();
		mAudio.startRecording();
		int ind=0;
		int endbuffer;
		int nRead=1;
		int frameSize=minFrameSize*4;
		//Log.v(TAG,"frame size is: " + frameSize + " card size is" + soundCardBufferSize + " play time is: " + playTime);
		int dataLeft=samples.length;
		while(dataLeft>0){
			endbuffer=(frameSize<dataLeft) ? frameSize: dataLeft;
			//Log.v(TAG, "lthis.samples.length=" + this.samples.length+ " index: " + ind*frameSize + " size:" + frameSize);
			nRead=mAudio.read(this.samples,ind*frameSize,endbuffer);
            if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Audio read failed: " + nRead);
                break;
            }
            dataLeft -= endbuffer;
            //Log.v(TAG, "dataleft: " + dataLeft);
			ind++;	
			//Log.v(TAG,"Read : " + nRead + " short from " + endbuffer + " " +  100*(float)nRead/endbuffer + " %");			
		}
		mAudio.stop();
		record_time = System.currentTimeMillis()-st;
		Log.v(TAG,"low level recording took: " + record_time/1000);
		recordRMS=SignalProcessing.rms(samples);
		Log.v(TAG,"recording RMS= " + recordRMS);		
	}

}
