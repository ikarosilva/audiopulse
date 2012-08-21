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

import android.util.Log;

public class PeriodicSeries {


	public static final String TAG="PeriodicSeries";
	public double[] frequency;
	public double[] amplitude;
	private short[] data;
	public int N;
	public double Fs; //Sampling frequency in Hz
	public double windowSize=0.3; //window size in seconds
	public int windowN;

	public PeriodicSeries(int N,double Fs,double[] frequency){
		this.N=N;
		this.Fs=Fs;
		this.frequency=frequency;
		data=new short[N];
		amplitude= new double[frequency.length];
		for (int i=0;i<frequency.length;i++){
			amplitude[i]=1; //Set default amplitudes in intensity
		}
		windowN=(int) (windowSize*Fs);
	}
	
	//Constructor for Calibration signals
	public PeriodicSeries(int N,double Fs,CalibrationTone caltone){
		this.N=N;
		this.Fs=Fs;
		frequency=caltone.getStimulusFrequency();
		amplitude=caltone.getStimulusAmplitude(); //in intensity relative to 1
		Log.v(TAG,"Generating calibration tone amplitude = " + amplitude[0] + " of length= " + amplitude.length);
		//Log.v(TAG," f =" + frequency);
		data=new short[N];
		windowN=(int) (windowSize*Fs);
	}
		
	//Constructor for DPOAE signals, which is a Periodic Series
	public PeriodicSeries(int N,double Fs,DPOAESignal dpoae){
		this.N=N;
		this.Fs=Fs;
		frequency=dpoae.getStimulusFrequency();
		amplitude=dpoae.getStimulusAmplitude();
		data=new short[N];
		windowN=(int) (windowSize*Fs);
	}

	public PeriodicSeries(int N,double Fs,double[] frequency,double[] amplitude){
		this.N=N;
		this.Fs=Fs;
		this.frequency=frequency;
		data=new short[N];
		this.amplitude=amplitude; //Amplitudes are in intensity!!
		windowN=(int) (windowSize*Fs);
	}

	public short[] generatePeriodicSeries(){

		double tmpSample;
		double PI2=2*Math.PI;
		double[] increment= new double[frequency.length];
		int index;
		int windowOffset0=N -1 - Math.round(windowN/2);
		for (int i=0;i<frequency.length;i++){
			increment[i] = PI2 * frequency[i] /Fs; // angular increment for each sample
		}
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
			data[i]=(short) ((Short.MAX_VALUE-1)*tmpSample);
		
		}
		return data;
	}
	
} //of Class definition
