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


public class CalibrationTone {

	private double[] f;
	private double[] A; //returned Amplitudes are in intensity
	private String device;
	public static final double dBSPLRef= 0.00002; //20 micro Pascal is the physical reference
	public static final double dBuRef= Math.sqrt(0.6); //20 micro Pascal is the physical reference
	
	//err=20*log10(data_rms/ref_rms);
	public static enum device {

		//For calibration of the ER10C we will use a 1 kHz
		ER10C(1000,0.5,"ER10C");
		
		//ER10C Specs
		/*
		private double sensitivity1kHzSPL=72; 
		private double sensitivity1kHzVRMS=1; 
		private double output0SPLtodBuV=0; 
		*/
		private double f;
		private double A;	//Amplitudes are in dB
		private String deviceName;
		device(double f,double A, String deviceName) {
			this.f=f;
			this.A=A;//dBuRef*Math.pow(10,A/20); //Convert amplitude in dBu to intensity
			this.deviceName=deviceName;
		}
	}

	public CalibrationTone(device caltone){
		double[] f={caltone.f};
		this.f=f;
		double[] A= {caltone.A};
		this.A=A;
		device=caltone.deviceName;
	}
	public double[] getStimulusFrequency(){
		return f;
	}
	public double[] getStimulusAmplitude(){
		return A;
	}
	public String getProtocol(){
		return device;
	}
}
