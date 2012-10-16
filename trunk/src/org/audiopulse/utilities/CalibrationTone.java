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


public class CalibrationTone extends PeriodicSeries {
	private String device;
	private static String TAG="CalibrationTone";
		
	public static enum device {

		//ER10C Specs
				/*
				private double sensitivity1kHzSPL=72; 
				private double sensitivity1kHzVRMS=1; 
				private double output0SPLtodBuV=0; 
				*/
		ER10C_LGVM670((double) 1000,40,"ER10C"), //using ER10C with +40dB gain
												//for this phone the audio jack needs to have a really
												//tight connection in order to works
		DUMMY_LGVM670((double) 1000,20,"DUMMY_LGVM670"); //used for debugging in free field mode
		
		
		private double[] f=new double[1];
		private double[] A=new double[1];	//Attenuation in dB relative to AudioTrack.getMaxVolume()
		private String deviceName;
		
		device(Double f,int attenuation, String deviceName) {
			this.f[0]=f;
			//Convert attenuation in dB relative to the maximum track level
			this.A[0]=Short.MAX_VALUE*Math.pow(10,(double)(-attenuation)/20); 
			this.deviceName=deviceName;
			Log.v(TAG,"Amplitude =" + this.A[0]);
		}
	}

	public CalibrationTone(int N, double Fs, device caltone, int channelConfig){
		//Call constructor on PeriodicSeries
		super(N,Fs, caltone.f,caltone.A,channelConfig);
		device=caltone.deviceName;
	}
	
	public String getProtocol(){
		return device;
	}
}
