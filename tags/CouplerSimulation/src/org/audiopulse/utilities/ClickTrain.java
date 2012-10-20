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

import android.util.Log;

public class ClickTrain {


	public static final String TAG="ClickTrain";
	public double clickDuration;
	public short amplitude;
	public short baseline;
	public double restDuration;
	private short[] data;
	public int N;
	public double Fs; //Sampling frequency in Hz

	public ClickTrain(int N,double Fs,double clickDuration, double restDuration){
		//Duration variables are defined in terms of second
		this.N=N;
		this.Fs=Fs;
		this.clickDuration=clickDuration;
		this.restDuration=restDuration;
		data=new short[N];
		amplitude=Short.MAX_VALUE/2;
		baseline=Short.MIN_VALUE/2;
		Log.v(TAG,"Stimulus parameters: N=" + N + " Fs=" + Fs + " clickdur= " 
				+ clickDuration + " restduration= " + restDuration);
	}

	public short[] generateClickTrain(){
		int inClick=1; //Defined in terms of sample with positive values for clickDuration and negative for restDuration
		for( int i = 0; i < N; i++ )
		{		
			data[i]= (short) (Math.random()*amplitude);
			/*
			if(inClick > 0){
				//in click phase
				data[i]= (short) (Math.random()*amplitude);
				//data[i]= (short) (amplitude*Math.sin(2*Math.PI * 1000/Fs*i)/10);
				inClick++;
				if(inClick > (clickDuration*Fs)){
					inClick=-1;
				}
			} else if (inClick < 0){
				//in rest phase
				data[i]=0;
				inClick--;
				if((inClick*-1) > (restDuration*Fs)){
					inClick=1;
				}
			}	
			*/
		}
		return data;
	}

} //of Class definition
