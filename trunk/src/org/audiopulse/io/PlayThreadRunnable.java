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

import org.audiopulse.utilities.CalibrationTone;
import org.audiopulse.utilities.ClickTrain;
import org.audiopulse.utilities.PeriodicSeries;
import org.audiopulse.utilities.WhiteNoise;

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
	int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int audioMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	public final static int sampleRate=44100;
	int PlayBufferSize;
	short[] samples;
	Handler mainThreadHandler = null;
	private long real_play_time;


	public PlayThreadRunnable(Handler h, double playTime)
	{
		Log.v(TAG,"constructing playback thread");
		mainThreadHandler = h;
		PlayBufferSize=(int) (playTime*sampleRate);

		if(channelConfig == AudioFormat.CHANNEL_OUT_STEREO){
			//Interleave the tracks for processing
			samples = new short[2*PlayBufferSize];
		}else{
			samples = new short[PlayBufferSize];
		}
		this.initPlayTrack();
		this.generateStimulus();

	}

	public synchronized void run()
	{
		informStart();
		//Play Stimulus
		playStimulus();
		//Finish up
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
		m.setData(Utils.getStringAsABundle("Playing sound for "  + samples.length/sampleRate + " seconds"));
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
				track.setStereoVolume(AudioTrack.getMaxVolume()/2,AudioTrack.getMaxVolume()/2);
			}
			track.setPlaybackRate(sampleRate);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	
		Log.v(TAG,"Ch count= " + track.getChannelCount() + " Play Fs= " + track.getPlaybackRate() +
				" Coding= " + track.getAudioFormat() + " Fs= " + track.getSampleRate());
		Log.v(TAG,"Ch config= " + track.getChannelConfiguration() + " mono is= " + AudioFormat.CHANNEL_OUT_MONO);
		
		if(track.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			Log.e(TAG,"Error: Audio record was not properly initialized!!");
			return;
		}
	}


	private void generateStimulus(){
		Log.v(TAG,"generating stimulus of length = " + (double) PlayBufferSize/sampleRate + " seconds with samples= " + PlayBufferSize);
		//CalibrationTone caltone = new CalibrationTone(CalibrationTone.device.ER10C);
		//PeriodicSeries stimuli=new PeriodicSeries(PlayBufferSize,sampleRate,caltone);
		//short[] tmpSamples = stimuli.generatePeriodicSeries();
		ClickTrain stimuli = new ClickTrain(PlayBufferSize,PlayThreadRunnable.sampleRate,0.1,0.1);
		short[] tmpSamples = stimuli.generateClickTrain();
		//WhiteNoise stimuli = new WhiteNoise(PlayBufferSize,PlayThreadRunnable.sampleRate);
	    //short[] tmpSamples = stimuli.generateWhiteNoise();
		Log.v(TAG,"Generate stimulus of length " + tmpSamples.length + " ,seconds= " + tmpSamples.length/(double) sampleRate);
		if(channelConfig == AudioFormat.CHANNEL_OUT_STEREO){
			//Interleave the tracks for processing
			for(int i=0;i<tmpSamples.length;i++){
					samples[i]=tmpSamples[i];
					samples[i+1]=tmpSamples[i];
			}
		}else if(channelConfig == AudioFormat.CHANNEL_OUT_MONO) {
			//Mono channel
			samples=tmpSamples;
		}else {
			Log.v(TAG,"Unexpected channel configuration: " + channelConfig);
		}
	}


	private void playStimulus() {
		int frameSize=AudioBufferSize; 
		int dataLeft=samples.length;	
		int ind=0;
		int endbuffer;
		int nWrite=1;
		int total=0;
		Log.v(TAG,"frame size is: " + frameSize + " card size is: " + AudioBufferSize);
		Log.v(TAG, "samples.length= " + samples.length + " frameSize=" + frameSize + " expected time is: " + samples.length/sampleRate);
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












