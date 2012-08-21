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

public class DPOAESignal {

	private double[] f;
	private double[] A; //returned Amplitudes are in intensity
	private double f12; //Expected response frequency
	private String protocol;
	
	public static enum protocolBioLogic {
		//Generate a specific set of DPOAE stimuli based on the same parameters from 
		//that of the Bio-Logic Otoacoustic emissions Report (2012)
		F8k(6516,7969,64.8,54.9),
		F6k(4594,5625,64.8,56.6),
		F4k(3281,3984,64.8,55.6),
		F3k(2297,2813,64.6,55.1),
		F2k(1641,2016,64.4,53.4);

		private double f1;
		private double f2;
		private double A1;	//Amplitudes are in dB
		private double A2;
		private double f12; //Expected response frequency
		private String protocol="BioLogic";
		protocolBioLogic(double f1,double f2, double A1, double A2) {
			this.f1=f1;
			this.f2=f2;
			this.A1=Math.pow(10,A1/20); //Convert amplitudes to normalized intensity
			this.A2=Math.pow(10,A2/20);
			this.f12=2*f1-f2;
		}
	}

	public static enum protocolHOAE{
		//Generate a specific set of DPOAE stimuli based on the same parameters from 
		//"Handbook of Otocoustic Emissions" J. Hall, Singular Publishing Group Copyright 2000
		// Screening parameters in page 136.
		F8k(8000),
		F6k(6000),
		F4k(4000),
		F3k(3000),
		F2k(2000);
		private double f1;
		private double f2;
		private double A1; //Amplitudes are in dB
		private double A2;
		private double f12; //Expected response frequency
		private String protocol="HOAE";
		protocolHOAE(double f2) {
			this.f1=f2/1.2;
			this.f2=f2;
			this.A1=Math.pow(10,65/20); //Convert amplitudes to normalized intensity
			this.A2=Math.pow(10,65/20);
			this.f12=2*f1-f2;
		}
	}

	public DPOAESignal(protocolBioLogic dpoae){
		double[] f={dpoae.f1,dpoae.f2};
		this.f=f;
		double[] A= {dpoae.A1, dpoae.A2};
		this.A=A;
		this.f12=dpoae.f12;
		this.protocol=dpoae.protocol;
	}
	public DPOAESignal(protocolHOAE dpoae){
		double[] f={dpoae.f1,dpoae.f2};
		this.f=f;
		double[] A= {dpoae.A1, dpoae.A2};
		this.A=A;
		this.f12=dpoae.f12;
		this.protocol=dpoae.protocol;
	}
	
	public double[] getStimulusFrequency(){
		return this.f;
	}
	public double[] getStimulusAmplitude(){
		return this.A;
	}
	public double getExpectedResponseFrequency(){
		return this.f12;
	}
	public String getProtocol(){
		return this.protocol;
	}
}
