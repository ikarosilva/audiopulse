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

	/* This analysis is based on
	 * "Otoacoustic emission from normal-hearing and hearing-impaired subjects: Distortion product responses"
	 * Gorga, et al, 1993, JASA 93 (4)
	 * 
	 * In this paper they use the followign parameters:
	 * Fs= 50 kHz
	 * Recording Duration= 0.02048 seconds
	 * Number of trials = 200 ( for a total of 4.096 s)
	 * 
	 * So that the number of samples used in the FFT is 1024 and the
	 * frequency separation in the FFT is 48.8 Hz.
	 * 
	 * 
	 * L1= 65 dB SPL, L2 = 50 dB SPL
	 * 
	 * Background noise at 4 kHz is expected to be around -20 to -25 dB 
	 * and up to 10 - 15 dB at 500 Hz
	 * 
	 * Mean amplitude of the response ranges from 8 to -2 dB SPL (at 1464 and 8007 Hz
	 * respectively). Thus SNR is maximum at about 27 dB in 3906 Hz. The variation in
	 * SNR is mostly due to noise. 
	 * 
	 * Used audiometric criterial of 20 dB HL to separate hearing loss. 
	 * 
	 * A -6dB SPL for DPOAEs at 4kHz resulted in less thatn 10% misses and false alarms.
	 * Regression line to audiometric threshold at 4 Khz is (r=-0.85):
	 * 
	 *  y = 14.3 - 1.714*SNR
	 *  
	 *  SNR of 12 dB at 4kH  identifies 90% of normals and misses 5% of hearing impaired.
	 * 
	 */
	private final static String TAG="DPOAEAnalyzer";
	public static final String TestType="DPOAE";
	private double[] XFFT;
	private final static double spectralToleranceHz=50;
	private double Fs;
	private short F1;
	private short F2;
	private double Fres;	
	public final String protocol;
	public final String fileName;
	private double Fstep;
	//Estimated absolute levels above the threshold below will be logged 
	//and set to infinity
	private static final double dBErrorWarningThreshold=150;
	private double[] noiseRangeHz;

	public DPOAEAnalyzer(double [] XFFT, double Fs, short F2, short F1, double Fres,
			String protocol, String fileName){
		this.Fs=Fs;
		this.XFFT=XFFT;
		this.F2=F2;
		this.F1=F1;
		this.Fres=Fres;//Frequency of the expected response
		this.protocol=protocol;
		this.fileName=fileName;
		Fstep= (double) Fs/(2.0*(XFFT.length-1));
	}


	public DPOAEResults call() throws Exception {
		// This function calculates the "Audiogram" results that will be 
		//plotted and save as the final analysis of the data. 
		//All the analysis will be done in the fft domain 
		//using the AudioPulseDataAnalyzer interface to obtain the results
		if(XFFT == null)
			Log.e(TAG,"received null spectrum!");
		double[][] PXFFT= getSpectrum(XFFT,Fs);
		double F1SPL=getStimulusLevel(PXFFT,F1);
		double F2SPL=getStimulusLevel(PXFFT,F2);
		double respSPL=getResponseLevel(PXFFT,Fres);
		double noiseSPL=getNoiseLevel(PXFFT,Fres); //Also sets noiseRangeHz

		//This is a little messy...should make DPOAEResults parceable and pass it as an object.
		DPOAEResults dResults=new DPOAEResults(respSPL,noiseSPL,F1SPL,F2SPL,
				Fres,F1,F2,PXFFT[1],null,fileName,protocol);
		dResults.setNoiseRangeHz(noiseRangeHz);
		return dResults;
	}

	private double[][] getSpectrum(double[] xFFT2, double fs) {
		// Reformat data to two arrays where the first is the frequency index
		double[][] PFFT=new double[2][xFFT2.length];
		for(int n=0;n<xFFT2.length;n++){
			PFFT[0][n]=n*Fstep;
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

	public double getFreqNoiseAmplitude(double[][] XFFT, double desF, int Find){

		//Estimates noise by getting the average level of 3 frequency bins above and below
		//the desired response frequency (desF)
		double noiseLevel=0;	
		//Get the average from 3 bins below and 3 bins above after a 50Hz offset from the
		//response FFT bin
		int offset=(int) Math.round(50/Fstep)+1;
		Log.v(TAG,"Resp at " + XFFT[0][Find] + " Hz ( " +
				Find + " ) = "+ XFFT[1][Find]);
		for(int i=0;i<3;i++){ 
			noiseLevel+= XFFT[1][(Find-offset-2+i)];
			Log.v(TAG,"Adding noise at " + XFFT[0][(Find-offset-2+i)] + " Hz ( " +
					(Find-offset-2+i) + " ) = "+ XFFT[1][(Find-offset-2+i)]);
			noiseLevel+= XFFT[1][(Find+offset+i)];
			Log.v(TAG,"Adding noise at " + XFFT[0][(Find+offset+i)]+ " Hz ( " +
					(Find+offset+i) + " ) = "+ XFFT[1][(Find+offset+i)]);
		}
		noiseLevel= noiseLevel/6.0;
		noiseLevel= Math.round(noiseLevel*10)/10.0;
		noiseRangeHz=new double[2];
		noiseRangeHz[0]=XFFT[0][(Find-offset-2)];
		noiseRangeHz[1]=XFFT[0][(Find+offset+2)];
		//Log.v(TAG,"Average noise is=" + noiseLevel);
		return checkLimit(noiseLevel);
	}

	public double getResponseLevel(double[][] dataFFT, double frequency) {	
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		amp[1]=Math.round(amp[1]/10)*10;//Keep only one significant digit
		return checkLimit(amp[1]);
	}

	public double getNoiseLevel(double[][] dataFFT, double frequency) {
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		return getFreqNoiseAmplitude(dataFFT,frequency,ind);
	}

	public double getStimulusLevel(double[][] dataFFT, double frequency) {
		int ind=getFreqIndex(dataFFT,frequency);
		double[] amp={dataFFT[0][ind],dataFFT[1][ind]};
		return checkLimit(amp[1]);
	}

	public static double checkLimit(double x){
		if(Math.abs(x) > dBErrorWarningThreshold){
			Log.e(TAG,"Estimated level is too high (setting to inf): " + x);
			x=(x>0) ? Double.POSITIVE_INFINITY:Double.NEGATIVE_INFINITY;
		}
		return x;
	}

}
