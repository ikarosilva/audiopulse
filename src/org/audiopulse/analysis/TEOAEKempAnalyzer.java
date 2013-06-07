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
package org.audiopulse.analysis;

import java.util.HashMap;

import android.util.Log;

public class TEOAEKempAnalyzer implements AudioPulseDataAnalyzer {

	private final String TAG="TEOAEKempAnalyzer";
	private short[] data;
	private int Fs;
	private static final int spectralToleranceHz=50;
	private int epochSize; //Size in samples of a single trial
							  
	HashMap<String, Double> resultMap;
	
	public TEOAEKempAnalyzer(short[] data, int Fs, int epochSize){
		this.Fs=Fs;
		this.data=data;
		resultMap= new HashMap<String, Double>();
		this.epochSize=epochSize;	
	}
	

	public HashMap<String, Double> call() throws Exception {
		// TODO This function should calculate the "Audiogram" results that will be 
		//plotted and save as the final analysis of the data. 
		//For now, using generic as return type to allow for flexibility,
		//but maybe we should consider creating a AP Data Type 

		//All the analysis will be done in the fft domain for now
		//using the AudioPulseDataAnalyzer interface to obtain the results
		Log.v(TAG,"Calling analysis thread with: data.size= " + data.length +" Fs= " + Fs +" epochSize= " + epochSize);
		double[] results=TEOAEKempClientAnalysis.mainAnalysis(data,Fs,epochSize);
		resultMap.put(TestType,(double) 1);//According to the interface, 1 =TEOAE
		//Get responses for 2,3 and 4 kHz for now...
		Log.v(TAG,"Estimated responses: " + results[0] + " , " + results[1] +
				" , " + results[2]+ " noise= " + results[3]);
		resultMap.put(RESPONSE_2KHZ, results[0]);
		resultMap.put(RESPONSE_3KHZ, results[1]);
		resultMap.put(RESPONSE_4KHZ, results[2]);
		
		
		//Get estimated noise floor levels, which is constant for this method
		resultMap.put(NOISE_2KHZ, results[3]);
		resultMap.put(NOISE_3KHZ, results[3]);
	    resultMap.put(NOISE_4KHZ, results[3]);   
		return resultMap;
	}
	
	public static double getFreqAmplitude(double[][] XFFT, double desF, double tolerance){

		//Search through the spectrum to get the closest bin 
		//to the respective frequencies
		double dminF=Short.MAX_VALUE;
		double dF; 
		//Results will be stored in a vector where first row is the closest
		//bin from the FFT wrt the frequency and second row is the power in that
		//bin. 
		double Amp=Double.NaN;
		for(int n=0;n<XFFT[0].length;n++){
			dF=Math.abs(XFFT[0][n]-desF);
			if( dF < dminF ){
				dminF=dF;
				Amp=XFFT[1][n];
			}		
		}
		return Amp;
	}


	public double getResponseLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getResponseLevel(double[][] dataFFT, double frequency) {	
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}


	public double getNoiseLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getNoiseLevel(double[][] dataFFT, double frequency) {
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}


	public double getStimulusLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getStimulusLevel(double[][] dataFFT, double frequency) {
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}

}
