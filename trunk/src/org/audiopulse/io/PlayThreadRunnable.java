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

import org.audiopulse.utilities.DPOAESignal;
import org.audiopulse.utilities.DPOAESimulationSignal;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PlayThreadRunnable implements Runnable
{
	public static String TAG = "PlayThreadRunnable";
	private int AudioBufferSize;
	private AudioTrack track;
	int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int audioMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	double expectedResponse=0; //Results of where to expect a response, such as DPOAE
	
	//The track volume should be held fixed always at the maximum!!
	//if the signal is being clipped than attenuation the signal relative to this value!!
	public static final float trackVolume=AudioTrack.getMaxVolume();
	public final static int sampleRate=44100;
	int PlayBufferSize;
	short[] samples;
	Handler mainThreadHandler = null;
	private long real_play_time;
	String trackConfig;
	


	public PlayThreadRunnable(Handler h, double playTime)
	{
		Log.v(TAG,"constructing playback thread");
		mainThreadHandler = h;
		PlayBufferSize=(int) (playTime*sampleRate);
		this.initPlayTrack();
		this.generateStimulus();
	}

	public synchronized void run()
	{
		informStart();
		informMiddle("channel is: " + trackConfig + " volume= " + trackVolume);
		playStimulus();
		informFinish();
	}

	public void informMiddle(String str)
	{
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		mainThreadHandler.sendMessage(m);
	}

	public void informStart()
	{
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Playing sound for "  + PlayBufferSize/sampleRate));
		mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		track.release();
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Released soundcard in " + real_play_time/1000 + " seconds"));
		mainThreadHandler.sendMessage(m);
	}


	private void initPlayTrack(){
		Log.v(TAG," rate= " + AudioTrack.getNativeOutputSampleRate(channelConfig));
		try {
			AudioBufferSize =AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
			track = new AudioTrack(audioMode, sampleRate, channelConfig, audioFormat, 
					AudioBufferSize*2,trackMode); 
			assert ( track.getChannelConfiguration() == channelConfig ) : "Incorrect channel configuration: " +
					track.getChannelConfiguration() + " expected: " + channelConfig ;
			if(channelConfig == AudioFormat.CHANNEL_OUT_STEREO){
				track.setStereoVolume(trackVolume,trackVolume);
			}
			track.setPlaybackRate(sampleRate);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	
		Log.v(TAG,"Ch count= " + track.getChannelCount() + " Play Fs= " + track.getPlaybackRate() +
				" Coding= " + track.getAudioFormat() + " Fs= " + track.getSampleRate());
		if(track.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			Log.e(TAG,"Error: Audio record was not properly initialized!!");
			return;
		}
	}


	private void generateStimulus(){
		Log.v(TAG,"generating stimulus of length = " + (double) PlayBufferSize/sampleRate + " seconds with samples= " + PlayBufferSize);
		MobilePhone phone = new HTCOne(HTCOne.deviceCalParam.ER10C,
				AcousticDevice.ioDevice.ER10C);
		
		DPOAESignal stimulus = new DPOAESignal(DPOAESignal.protocolBioLogic.F2k,PlayBufferSize,
				sampleRate,phone,channelConfig);
		expectedResponse=stimulus.expectedResponse;
		
		short[] tmpSamples = stimulus.generateSignal();
		if(stimulus.getStereoFlag() == AudioFormat.CHANNEL_OUT_STEREO){
			trackConfig="stereo";
		}else if(stimulus.getStereoFlag() == AudioFormat.CHANNEL_OUT_MONO) {
			trackConfig="mono";
		}else {
			trackConfig="Unexpected channel configuration!!";
		}
		samples=tmpSamples;
	}


	private void playStimulus() {
		int frameSize=AudioBufferSize; 
		int dataLeft=samples.length;	
		int ind=0;
		int endbuffer;
		int nWrite=1;
		int total=0;
		Log.v(TAG,"frame size is: " + frameSize + " card size is: " + AudioBufferSize);
		Log.v(TAG, "samples.length= " + samples.length + " frameSize=" + frameSize );
		track.play();
		long st = System.currentTimeMillis();
		while(dataLeft>0){
			endbuffer=(frameSize<dataLeft) ? frameSize: dataLeft;	
			Log.v(TAG, "Index: " + ind*frameSize + " size: " + endbuffer);
			nWrite=track.write( samples,ind*frameSize,endbuffer);
			if (nWrite == AudioTrack.ERROR_INVALID_OPERATION || nWrite == AudioTrack.ERROR_BAD_VALUE) {
				Log.e(TAG, "Audio read failed: " + nWrite);
				break;
			}
			Log.v(TAG,"wrote " + nWrite + " samples ( " + (long) 100*nWrite/endbuffer + " %)");
			dataLeft -= endbuffer;
			Log.v(TAG, "data left to play: " + dataLeft);
			ind++;	
			total+=nWrite;
		}
		real_play_time = System.currentTimeMillis()-st;
		Log.v(TAG,"play time " + real_play_time/1000 + " seconds" + " total samples: " + total);
	}

}












