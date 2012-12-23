package org.audiopulse.utilities;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.audiopulse.io.RecordThreadRunnable;

import android.os.Bundle;

public class SignalProcessing {
	
	public static double rms(short[] x){
		double y=0;
		double rms;
		//Calculate mean squared
		for(int i=0;i<x.length;i++){
			y= (i*y + (x[i]*x[i]))/(i+1);
		}
		//Return RMS in decibels wrt to 1 gain
		//And Round RMS value to nearest 
		rms=20*Math.log10(Math.sqrt(y)/Short.MAX_VALUE);
		return Math.round(rms*10)/10;
	}
	
	@Deprecated 	// as written, this is power (not rms) to dB (not dBu)
	public static double rms2dBU(double x){
		return 10*Math.log10(x);
	}
	
	public static double rms2dB(double rms) {
		return 20*Math.log10(rms);
	}
	
	public static double dB2lin(int a) {
		return dB2lin((double)a);
	}
	public static double dB2lin(double a) {
		return Math.pow(10, a/20);
	}

	public static double[] getSpectrum(short[] x){
		FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);
		//Calculate the size of averaged waveform
		//based on the maximum desired frequency for FFT analysis
		int N=x.length;
		int SPEC_N=(int) Math.pow(2,Math.floor(Math.log((int) N)/Math.log(2)));
		double[] winData=new double[SPEC_N];
		Complex[] tmpFFT=new Complex[SPEC_N];
		double[] Pxx = new double[SPEC_N];
		double tmpPxx;
		//Break FFT averaging into SPEC_N segments for averaging
		//Calculate spectrum, variation based on
		//http://www.mathworks.com/support/tech-notes/1700/1702.html

		//Perform windowing and averaging on the power spectrum
		for (int i=0; i < N; i++){
			if(i*SPEC_N+SPEC_N > N)
				break;
			for (int k=0;k<SPEC_N;k++){
				winData[k]= (double) x[i*SPEC_N + k]*SpectralWindows.hamming(k,SPEC_N);
			}
			tmpFFT=FFT.transform(winData,TransformType.FORWARD);
			for(int k=0;k<(SPEC_N/2);k++){
				tmpPxx = tmpFFT[k].abs()/(double)SPEC_N;
				tmpPxx*=tmpPxx; //Not accurate for the DC & Nyquist, but we are not using it!
				Pxx[k]=( (i*Pxx[k]) + tmpPxx )/((double) i+1);
			}
		}
		
		return Pxx;
	}
	
	/*
	public static double[] getDPOAEResults(Bundle audioBundle, RecordThreadRunnable rRun){
		short[] audioBuffer = audioBundle.getShortArray("samples");
		double[] Pxx=SignalProcessing.getSpectrum(audioBuffer);
		//TODO: set noise estimation to mid-frequency between desired stimulus and F1 for now
		//double Fnoise=rRun
		}
		*/
	
}
