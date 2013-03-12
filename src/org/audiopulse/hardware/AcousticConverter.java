package org.audiopulse.hardware;

import org.audiopulse.utilities.SignalProcessing;

public class AcousticConverter {
	//TODO: put this into a resource file
	private double VPerDU_output = 1;		//V for 1 amplitude at outpu
	private double VPerDU_input = 0.020;		//V for 1 amplitude at input
	private double SPL1V = 72;				//dB SPL for 1V rms electrical signal
	private double SPL1uV = 0;				//db SPL for 1uV rms micorphone electrical signal
	
	public AcousticConverter() {
		//TODO: determine that mic & headphone jack are connected to something
		
	}
	
	// get signal as output level in dB SPL
	public double getOutputLevel(double[] x) {
		return getOutputLevel(x,0,x.length-1);
	}
	public double getOutputLevel(double[] x, int fromSample, int toSample) {
		//compute sum of squares
		double r = 0;
		int N = toSample-fromSample;
		for (int n=fromSample; n<=toSample; n++) {
			r+=x[n]*x[n];
		}
		if (r==0)								//avoid log(0), return min value instead
			return Double.MIN_VALUE;
		r /= N;										//convert to mean-squared
		r *= (VPerDU_output*VPerDU_output);			//convert mean-squared value to volts^2
		return 10*Math.log10(r) + SPL1V;			//convert to dB SPL
	}
	
	//set signal as output level in dB SPL
	public double[] setOutputLevel(double[] x, double spl) {
		double a = getOutputLevel(x);
		double gain = spl - a;
		for (int n=0; n<x.length; n++) {
			x[n] *= Math.pow(10, gain/20);
		}
		return x;
	}
	
	//compute input signal level in dB SPL
	public double getInputLevel(double[] x) {
		return getInputLevel(x,0,x.length-1);
	}
	public double getInputLevel(double[] x, int fromSample, int toSample) {
		//compute sum of squares
		double r = 0;
		int N = toSample-fromSample;
		for (int n=fromSample; n<=toSample; n++) {
			r+=x[n]*x[n];
		}
		if (r==0)								//avoid log(0), return min value instead
			return Double.MIN_VALUE;
		r = Math.sqrt(r/N);		//convert to rms DU
		return getInputLevel(r);
	}
	public double getInputLevel(double rms) {
		return 20*Math.log10(rms*VPerDU_input*1e6) + SPL1uV;		//SPL = dBuV + SPL1uV
	}
	
	//return dB offset: dB SPL = 10*log10(A^2) + dBOffset
	public double getDBOffset_output() {
		return 10*Math.log10(VPerDU_output) + SPL1V;
	}
	//return dB offset: dB SPL = 10*log10(A^2) + dBOffset
	public double getDBOffset_input() {
		return 10*Math.log10(VPerDU_input*1e6) + SPL1uV;
	}
	
	
	//convert output vector to expect input vector for flat, 1 acoustic response 
	public double[] outputToInput(double[] output) {
		int N = output.length;
		double[] input = new double[N];
		for (int n=0; n<N; n++) {
			input[n] = output[n] * VPerDU_output / VPerDU_input * Math.pow(10, (SPL1V-(SPL1uV+120))/20);
		}
		return input;
	}
}
