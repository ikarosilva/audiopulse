package org.audiopulse.utilities;

import java.util.Random;

//class containing static methods to generate useful signals
public class Signals {

	//TODO: phase
	public static double[] tone(double sampleFrequency, double frequency, double durationInSeconds) {
		int N = (int) (durationInSeconds * sampleFrequency);
		double[] x = new double[N];
		for (int n=0;n<N;n++) {
			x[n] = Math.sin(2*Math.PI*frequency*n/sampleFrequency);
		}
		return x;
	}
	
	//TODO: phases
	public static double[] toneComplex(double sampleFrequency, double[] frequencies, double durationInSeconds) {
		int N = (int) (durationInSeconds * sampleFrequency);
		double[] x = new double[N];
		for (int n=0;n<N;n++) {
			x[n] = 0;
			for (int ff=0;ff<frequencies.length;ff++) {
				x[n] += Math.sin(2*Math.PI*frequencies[ff]*n/sampleFrequency);
			}
		}
		return x;
	}
	
	public static double[] chirp(double sampleFrequency, double f0, double f1, double durationInSeconds) {
		int N = (int) (durationInSeconds * sampleFrequency);
		double[] x = new double[N];
		for (int n=0;n<N;n++) {
			//instantaneous freq of sin(th) is w(t) = d(th)/d(t)
			//d(th)/d(t) = w0+(w1-w0)*t/T
			//th = w0*t + 1/2*(w1-w0)*t^2/T
			x[n] = Math.sin(Math.PI*(2*f0+(f1-f0)*n*n/N/sampleFrequency));
		}
		return x;
	}
	
	//TODO: Random.nextGaussian uses Box-Muller alg, which 
	//some say is slow and innaccurate. Consider finding a invcdf lookup
	public static double[] gaussianNoise(double sampleFrequency, double durationInSeconds) {
		Random rng = new Random();
		int N = (int) (durationInSeconds / sampleFrequency);
		double[] x = new double[N];
		for (int n=0; n<N; n++) {
			x[n] = rng.nextGaussian();
		}
		return x;
	}
	

	public static double[] fadeIn(double sampleFrequency, double fadeDuration, double[] x) {
		int F = (int) (fadeDuration / sampleFrequency);
		for (int n=0;n<F;n++) {
			x[n] *= (double)(n)/(double)(F); 
		}
		return x;
	}
	public static double[] fadeOut(double sampleFrequency, double fadeDuration, double[] x) {
		int N = (int) (x.length / sampleFrequency);
		int F = (int) (fadeDuration / sampleFrequency);
		for (int n=0;n<F;n++) {
			x[N-1-n] *= (double)(n)/(double)(F); 
		}
		return x;
	}
	public static double[] fade(double sampleFrequency, double fadeDuration, double[] x) {
		return fadeIn(sampleFrequency,fadeDuration,fadeOut(sampleFrequency,fadeDuration,x));
	}
	
}
