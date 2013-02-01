package org.audiopulse.hardware;

import org.audiopulse.utilities.SignalProcessing;

public class AcousticConversion {
	//TODO: replace these parameters with a MobilePhone and a AcousticDevice object.
	private double VPerDU_output = 1;		//V for 1 amplitude at outpu
	private double VPerDU_input = 0.01;		//V for 1 amplitude at input
	private double SPL1V = 72;				//dB SPL for 1V rms electrical signal
	private double SPL1uV = 0;				//db SPL for 1uV rms micorphone electrical signal
	
	public AcousticConversion() {
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
		r /= N;										//convert to mean-squared
		r *= (VPerDU_input*VPerDU_input);			//convert to volts^2
		return 10*Math.log10(r*1e6) + SPL1uV;		//convert to dB SPL (using uV reference)
	}
}
