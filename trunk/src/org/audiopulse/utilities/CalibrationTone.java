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



public class CalibrationTone extends PeriodicSeries {
	private String device;
		
	public static enum device {

		//ER10C Specs
				/*
				private double sensitivity1kHzSPL=72; 
				private double sensitivity1kHzVRMS=1; 
				private double output0SPLtodBuV=0; 
				*/
		ER10C((double) 1000,(double) 5,"ER10C");
		
		private double[] f=new double[1];
		private double[] A=new double[1];	//Amplitudes are in dB
		private String deviceName;
		
		device(Double f,Double A, String deviceName) {
			this.f[0]=f;
			this.A[0]=Math.pow(10,-A/20); //Convert amplitude in dBu to intensity
			this.deviceName=deviceName;
		}
	}

	public CalibrationTone(int N, double Fs, device caltone, int channelConfig){	
		super(N,Fs, caltone.f,caltone.A,channelConfig);
		device=caltone.deviceName;
	}
	
	public String getProtocol(){
		return device;
	}
}
