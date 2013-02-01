package org.audiopulse.utilities;

//class containing static methods to generate useful signals
public class Signals {

	public static double[] tone(double sampleFrequency, double frequency, double duration) {
		int N = (int) (duration * sampleFrequency);
		double[] x = new double[N];
		for (int n=0;n<N;n++) {
			x[n] = Math.sin(2*Math.PI*frequency*n/sampleFrequency);
		}
		return x;
	}
	
	//TODO: toneComplex
	//TODO: chirp
	//TODO: noise
	
}
