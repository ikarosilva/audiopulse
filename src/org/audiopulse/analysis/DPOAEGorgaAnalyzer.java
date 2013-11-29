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

public class DPOAEGorgaAnalyzer implements AudioPulseDataAnalyzer {

	private final static String TAG="DPOAEGorgaAnalyzer";
	private short[] data;
	private final static double spectralToleranceHz=50;
	private double Fs;
	private double F1;
	private double F2;
	private double F12;
	private static int epochSamples; //Size of each epoch from which to do spectral the averaging.
	HashMap<String, Double> resultMap;

	public synchronized static double dpoaeGorgaAmplitude(double Frequency){
		//FIXME: Gorga's test requires a stimulus at 65 dB SPL
		//but this seems to result in clipping for most phones.
		//we need to find an optimal maximum level that does not clip the sound
		
		//From calibration experiments with the ER10C set to 0 dB gain, the linear range of
		//response-to-stimulus is from 50-30 dB on an acoustic coupler (response will saturate
		// on either extremes).
		return 70;
	}

	public static double dpoeaGorgaEpochTime(){
		//return epoch time in seconds
		return 0.02048;
	}
	
	public DPOAEGorgaAnalyzer(short[] data, double Fs, double F2,
			HashMap<String, Double> resultMap){
		this.Fs=Fs;
		this.data=data;
		this.F2=F2;
		F1=F2/1.2;
		this.F12=(2*F1)-F2;//Frequency of the expected response
		resultMap= new HashMap<String, Double>();
		epochSamples=(int) Math.round(dpoeaGorgaEpochTime()*Fs);
		epochSamples=(int) Math.pow(2,Math.floor(Math.log((int) epochSamples)/Math.log(2)));
		this.resultMap=resultMap;
		if(Fs != 16000){
			Log.v(TAG,"Unexpected sampling frequency:" + Fs);
		}
	}


	public HashMap<String, Double> call() throws Exception {
		// TODO This function calculates the "Audiogram" results that will be 
		//plotted and save as the final analysis of the data. 
		//For now, using generic as return type to allow for flexibility,
		//but maybe we should consider creating a AP Data Type 

		//All the analysis will be done in the fft domain 
		//using the AudioPulseDataAnalyzer interface to obtain the results
		Log.v(TAG,"Analyzing frequency: " + F2);
		double[][] XFFT= new double [2][100];//getSpectrum(data,Fs,epochSamples);
		
		resultMap.put(TestType,(double) 2);//According to the interface, 2 =DPOAE
		String strSTIM = null, strSTIM2 = null,strResponse = null,strNoise = null;

		if(F2==2000){
			strSTIM=STIM_2KHZ;strSTIM2=STIM2_2KHZ; strResponse=RESPONSE_2KHZ;strNoise=NOISE_2KHZ;
		}else if(F2==3000){
			strSTIM=STIM_3KHZ;strSTIM2=STIM2_3KHZ; strResponse=RESPONSE_3KHZ;strNoise=NOISE_3KHZ;
		}else if(F2==4000){	
			strSTIM=STIM_4KHZ;strSTIM2=STIM2_4KHZ; strResponse=RESPONSE_4KHZ;strNoise=NOISE_4KHZ;
		}else{
			Log.v(TAG,"Unexpected F2=" + F2);
		}
		resultMap.put(strSTIM, getStimulusLevel(XFFT,F1));
		resultMap.put(strSTIM2, getStimulusLevel(XFFT,F2));
		resultMap.put(strResponse, getResponseLevel(XFFT,F12));
		resultMap.put(strNoise, getNoiseLevel(XFFT,F12));
		return resultMap;
	}

	public static double[] getFreqAmplitude(double[][] XFFT, double desF, double tolerance){

		//Search through the spectrum to get the closest bin 
		//to the respective frequencies
		double dminF=Short.MAX_VALUE;
		double dF; 
		//Results will be stored in a vector where first row is the closest
		//bin from the FFT wrt the frequency and second row is the power in that
		//bin. 
		double[] Amp={Double.NaN, Double.NaN};
		for(int n=0;n<XFFT[0].length;n++){
			dF=Math.abs(XFFT[0][n]-desF);
			if( dF < dminF ){
				dminF=dF;
				Amp[0]=XFFT[1][n];
				Amp[1]=(double) n;
			}		
		}
		return Amp;
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
		Log.v(TAG,"noise level= " + noiseLevel/6);
		return (noiseLevel/6);
		
	}


	public double getResponseLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getResponseLevel(double[][] dataFFT, double frequency) {	
		double [] amp=getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
		Log.v(TAG,"response level= " + amp[0]);
		return amp[0];
	}


	public double getNoiseLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getNoiseLevel(double[][] dataFFT, double frequency) {
		double[] amp=getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
		return getFreqNoiseAmplitude(dataFFT,frequency,(int) amp[1]);
	}


	public double getStimulusLevel(short[] rawdata, double frequency, int Fs) {
		// not implemented
		return Double.NaN;
	}


	public double getStimulusLevel(double[][] dataFFT, double frequency) {
		double [] amp=getFreqAmplitude(dataFFT,frequency,spectralToleranceHz);
		Log.v(TAG,"stimulus level= " + amp[0]);
		return amp[0];
	}
}
