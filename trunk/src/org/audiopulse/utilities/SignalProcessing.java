package org.audiopulse.utilities;

public class SignalProcessing {
	
	public static double rms(short[] x){
		double y=0;
		for(int i=0;i<x.length;i++){
			y= (i*y + (x[i]*x[i]))/(i+1);
		}
		return Math.sqrt(y);
	}
	
	public static double rms2dBU(double x){
		return 10*Math.log10(x);
	}

}
