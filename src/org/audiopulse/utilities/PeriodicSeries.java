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

package org.audiopulse.utilities;

import org.audiopulse.graphics.SpectralWindows;


import android.media.AudioFormat;
import android.util.Log;

public class PeriodicSeries {


	public static final String TAG="PeriodicSeries";
	private double[] frequency;
	private double[] amplitude;
	private short[] data;
	private int N;
	private double Fs; //Sampling frequency in Hz
	private double windowSize=0.05; //window ramp size in seconds in order to avoid speaker clipping artifact
	private int windowN;
	private int channelConfig;	
	
	public PeriodicSeries(int N,double Fs,double[] frequency,
			double[] amplitude,int channelConfig){
		this.N=N;
		this.Fs=Fs;
		this.frequency=frequency;
		this.channelConfig=channelConfig;
		this.amplitude=amplitude; //Amplitudes are in intensity!!
		windowN=(int) (windowSize*Fs);
		if(channelConfig == AudioFormat.CHANNEL_OUT_MONO){
			data=new short[N];
		}else if (channelConfig == AudioFormat.CHANNEL_OUT_STEREO) {
			data=new short[2*N];
		}
		
		assert ( windowSize < N ) : "Window size: " + windowSize
		+ " is larget than number of samples: "+ N;
	}
	
	public double[] getSignalFrequency(){
		return frequency;
	}
	
	public double[] getSignalAmplitude(){
		return amplitude;
	}
	
	public int getChannelConfig(){
		return channelConfig;
	}
	
	public short[] generateSignal(){

		double tmpSample;
		double PI2=2*Math.PI;
		double[] increment= new double[frequency.length];
		int index;
		int windowOffset0=N -1 - Math.round(windowN/2);
		for (int i=0;i<frequency.length;i++){
			increment[i] = PI2 * frequency[i] /Fs; // angular increment for each sample
		}
		Log.v(TAG,"Calculating = " + N + " samples at fs=" + Fs + " array size is=" + data.length);
		for( int i = 0; i < N; i++ )
		{
			tmpSample=0;
			for( int k = 0; k < frequency.length; k++ )
			{
				tmpSample += amplitude[k]*Math.sin(increment[k]*i);
			}
			//At onset/offset apply window in order to avoid non-linear distortions
			if(i < (windowN/2 - 1) ){
				tmpSample=tmpSample*SpectralWindows.hamming(i,windowN);
			}else if(i > windowOffset0){
				index=windowN/2 + i-windowOffset0;
				tmpSample=tmpSample*SpectralWindows.hamming(index,windowN);
			}	
			if(channelConfig == AudioFormat.CHANNEL_OUT_MONO){
				data[i]=(short) tmpSample;

			}else{
				//Stereo case
				data[2*i]=(short) tmpSample;
				data[2*i+1]=(short) tmpSample;
			}

		}
		return data;
	}

} //of Class definition
