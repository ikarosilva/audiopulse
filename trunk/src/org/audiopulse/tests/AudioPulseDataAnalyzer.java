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
 * TestProcedure.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   Andrew Schwartz
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

//General class for analysis of Audio Pulse Data
//This class should be thread safe and able to run on either 
//and Android Client or Desktop environment
package org.audiopulse.tests;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPulseDataAnalyzer implements Callable {

	private final String TAG="AudioPulseDataAnalyzer";
	private short[] data;
	private double Fs;
	private analysisType myAnalysis;
	
	public static enum analysisType {

		////TODO: Implement this analysis (some require just moving the analysis
		//functions from the SignalProcessing library.		
		TEOAE_KEMP("TEOAE_KEMP"),
		DPOAE_HOAE("DPOAE_HOAE");

		private String testName;
		public static final String TEOAE_KEMP_STRING="TEOAE_KEMP";
		public static final String DPOAE_HOAE_STRING="DPOAE_HOAE";

		analysisType( String key ) {
			//Convert attenuation in dB relative to the maximum track level
			this.testName=key; 
		}
	}

	public AudioPulseDataAnalyzer(short[] data, double Fs, analysisType analysis){
		this.Fs=Fs;
		this.data=data;
		this.myAnalysis=analysis;
	}
	

	public ConcurrentHashMap<String,Double> call() throws Exception {
		// TODO This function should calculate the DPGRAM results that will be 
		//plotted. Among these: Expected Response, Stimulus level, and NoiseFloor
		//note that Map will be different between DPOAE, TEOAE, Calibration etc
		return null;
	}

   

}
