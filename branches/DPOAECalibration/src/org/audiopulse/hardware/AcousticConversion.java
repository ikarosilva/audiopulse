package org.audiopulse.hardware;

import org.audiopulse.utilities.SignalProcessing;

public class AcousticConversion {
	//TODO: replace these parameters with a MobilePhone and a AcousticDevice object.
	private double VPerDU_output = 1;		//V for 1 amplitude at outpu
	private double VPerDU_input = 0.01;		//V for 1 amplitude at input
	private double SPL1V = 72;				//dB SPL for 1V rms electrical signal
	private double SPL1uV = 0;				//db SPL for 1uV rms micorphone electrical signal
	
	public AcousticConversion() {
		
	}
	
	public double getOutputLevel(double[] x) {
		double r = SignalProcessing.rms(x);		//digital signal rms
		if (r==0)
			return Double.MIN_VALUE;
		
		r *= VPerDU_output;						//electrical signal rms
		return 10*Math.log10(r) + SPL1V;		//convert to dB SPL
	}
	
	public double getInputLevel(double[] x) {
		double r = SignalProcessing.rms(x);		//digital signal rms
		if (r==0)
			return Double.MIN_VALUE;
		
		r *= VPerDU_input;						//electrical signal rms
		return 10*Math.log10(r*1e6) + SPL1uV;	//convert to dB SPL (using uV reference)
	}
}
