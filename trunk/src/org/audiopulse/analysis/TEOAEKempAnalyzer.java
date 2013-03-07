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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.audiopulse.utilities.SignalProcessing;

public class TEOAEKempAnalyzer implements Callable<ConcurrentMap<String,Double>>, AudioPulseDataAnalyzer {

	private final String TAG="TEOAEKempAnalyzer";
	private short[] data;
	private final static double spectralToleranceHz=50;
	private double Fs;
	private int epochTime; //Time in samples for which to break the FFT analysis
							  //Should be power of two
	ConcurrentMap<String, Double> resultMap;
	
	public TEOAEKempAnalyzer(short[] data, double Fs, double epochTime){
		this.Fs=Fs;
		this.data=data;
		resultMap= new ConcurrentHashMap<String, Double>();
	}
	

	public ConcurrentMap<String, Double> call() throws Exception {
		// TODO This function should calculate the "Audiogram" results that will be 
		//plotted and save as the final analysis of the data. 
		//For now, using generic as return type to allow for flexibility,
		//but maybe we should consider creating a AP Data Type 

		//All the analysis will be done in the fft domain for now
		//using the AudioPulseDataAnalyzer interface to obtain the results
		double[][] XFFT= SignalProcessing.getSpectrum(data,Fs,epochTime);
		
		//Get responses for 2,3 and 4 kHz for now...
		resultMap.put(RESPONSE_2KHZ, getResponseLevel(XFFT, 2000));
		resultMap.put(RESPONSE_3KHZ, getResponseLevel(XFFT,3000));
		resultMap.put(RESPONSE_4KHZ, getResponseLevel(XFFT,4000));
		
		
		//Get estimated noise floor levels
		resultMap.put(NOISE_2KHZ, getNoiseLevel(XFFT, 2500));
		resultMap.put(NOISE_3KHZ, getNoiseLevel(XFFT,3500));
	    resultMap.put(NOISE_4KHZ, getNoiseLevel(XFFT,4500));
	    
	    
		return null;
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


	public double getResponseLevel(short[] rawdata, double frequency, double Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getResponseLevel(double[][] dataFFT, double frequency) {	
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}


	public double getNoiseLevel(short[] rawdata, double frequency, double Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getNoiseLevel(double[][] dataFFT, double frequency) {
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}


	public double getStimulusLevel(short[] rawdata, double frequency, double Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getStimulusLevel(double[][] dataFFT, double frequency) {
		return getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
	}

}
