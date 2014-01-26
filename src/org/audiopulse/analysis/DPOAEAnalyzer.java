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

import android.util.Log;

public class DPOAEAnalyzer {

	private final static String TAG="DPOAEAnalyzer";
	public static final String TestType="DPOAE";
	private double[] XFFT;
	private final static double spectralToleranceHz=50;
	private double Fs;
	private double F1;
	private double F2;
	private double Fres;	
	public final String protocol;
	public final String fileName;
	//Estimated levels above the threshold below will be logged as errors
	private static final double dBErrorWarningThreshold=70;

	public DPOAEAnalyzer(double [] XFFT, double Fs, double F2, double F1, double Fres,
			String protocol, String fileName){
		this.Fs=Fs;
		this.XFFT=XFFT;
		this.F2=F2;
		this.F1=F1;
		this.Fres=Fres;//Frequency of the expected response
		this.protocol=protocol;
		this.fileName=fileName;
	}


	public DPOAEResults call() throws Exception {
		// TODO This function calculates the "Audiogram" results that will be 
		//plotted and save as the final analysis of the data. 
		//For now, using generic as return type to allow for flexibility,
		//but maybe we should consider creating a AP Data Type 

		//All the analysis will be done in the fft domain 
		//using the AudioPulseDataAnalyzer interface to obtain the results
		if(XFFT == null)
			Log.e(TAG,"received null spectrum!");
		double[][] PXFFT= getSpectrum(XFFT,Fs);
		double F1SPL=getStimulusLevel(PXFFT,F1);
		double F2SPL=getStimulusLevel(PXFFT,F2);
		double respSPL=getResponseLevel(PXFFT,Fres);
		double noiseSPL=getNoiseLevel(PXFFT,Fres);
		//This is a little messy...should make DPOAEResults parceable and pass it as an object.
		DPOAEResults dResults=new DPOAEResults(respSPL,noiseSPL,F1SPL,F2SPL,
				Fres,F1,F2,PXFFT[1],null,fileName,protocol);
		if(dResults == null)
			Log.e(TAG,"Received null results!");
		return dResults;
	}

	private double[][] getSpectrum(double[] xFFT2, double fs) {
		// Reformat data to two arrays where the first is the frequency index
		double[][] PFFT=new double[2][xFFT2.length];
		double step= (double) fs/(2.0*(xFFT2.length-1));
		for(int n=0;n<xFFT2.length;n++){
			PFFT[0][n]=n*step;
			PFFT[1][n]=xFFT2[n];
		}
		return PFFT;
	}

	public static int getFreqIndex(double[][] XFFT, double desF){

		//Search through the spectrum to get the closest bin 
		//to the respective frequencies
		double dminF=Double.MAX_VALUE; //set initial value to very high level
		double dF; 
		//Results will be stored in a vector where first row is the closest
		//bin from the FFT wrt the frequency and second row is the power in that
		//bin. 
		int index=-1;
		for(int n=0;n<XFFT[0].length;n++){
			dF=Math.abs(XFFT[0][n]-desF);
			if( dF < dminF && dF<spectralToleranceHz){
				dminF=dF;
				index=n;
			}		
		}
		return index;
	}

	public static double getFreqNoiseAmplitude(double[][] XFFT, double desF, int Find){

		//Estimates noise by getting the average level of 3 frequency bins above and below
		//the desired response frequency (desF)
		double noiseLevel=0;	
		//Get the average from 3 bins below and 3 bins above
		for(int i=0;i<=6;i++){
			if(i !=3){
				noiseLevel+= XFFT[1][(Find+i-3)];
			}
		}
		noiseLevel= noiseLevel/6.0;
		noiseLevel= Math.round(noiseLevel*10)/10.0;
		if(noiseLevel > dBErrorWarningThreshold)
			Log.e(TAG,"Estimated noise level is too high: " + noiseLevel);
		return noiseLevel;
	}

	public double getResponseLevel(double[][] dataFFT, double frequency) {	
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		if(amp[1] > dBErrorWarningThreshold)
			Log.e(TAG,"Estimated response level is too high: " + amp[1]);
		return amp[1];
	}

	public double getNoiseLevel(double[][] dataFFT, double frequency) {
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		return getFreqNoiseAmplitude(dataFFT,frequency,ind);
	}

	public double getStimulusLevel(double[][] dataFFT, double frequency) {
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		if(amp[1] > dBErrorWarningThreshold)
			Log.e(TAG,"Estimated stimulus level is too high: " + amp[1]);
		return amp[1];
	}
}
