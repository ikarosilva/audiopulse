package org.audiopulse.utilities;

import java.util.Random;


//class containing static methods to generate useful signals
public class Signals {

	//TODO: phase
	public static double[] tone(int sampleFrequency, double frequency, double durationInSeconds) {
		int N = (int) (durationInSeconds * sampleFrequency);
		double[] x = new double[N];
		for (int n=0;n<N;n++) {
			x[n] = Math.sin(2*Math.PI*frequency*n/sampleFrequency);
		}
		return x;
	}
	
	//TODO: phases
	public static double[] toneComplex(int sampleFrequency, double[] frequencies, double durationInSeconds) {
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
	
	public static double[] chirp(int sampleFrequency, double f0, double f1, double durationInSeconds) {
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
	public static double[] gaussianNoise(int sampleFrequency, double durationInSeconds) {
		Random rng = new Random();
		int N = (int) (durationInSeconds / sampleFrequency);
		double[] x = new double[N];
		for (int n=0; n<N; n++) {
			x[n] = rng.nextGaussian();
		}
		return x;
	}
	

	public static double[] fadeIn(int sampleFrequency, double fadeDuration, double[] x) {
		int F = (int) (fadeDuration / sampleFrequency);
		for (int n=0;n<F;n++) {
			x[n] *= (double)(n)/(double)(F); 
		}
		return x;
	}
	public static double[] fadeOut(int sampleFrequency, double fadeDuration, double[] x) {
		int N = (int) (x.length / sampleFrequency);
		int F = (int) (fadeDuration / sampleFrequency);
		for (int n=0;n<F;n++) {
			x[N-1-n] *= (double)(n)/(double)(F); 
		}
		return x;
	}
	public static double[] fade(int sampleFrequency, double fadeDuration, double[] x) {
		return fadeIn(sampleFrequency,fadeDuration,fadeOut(sampleFrequency,fadeDuration,x));
	}
	
	public synchronized static double getclickKempSweepDurationSeconds() {
		//Defining this method because it will be used by other classes to set the 
		//final experiment time based on the desired number of sweeps.
		final double sweepDurationInSeconds=0.02;
		return sweepDurationInSeconds;
		
	}
	public synchronized static double[] clickKempMethod(int sampleFrequency, double totalDurationInSeconds) {
		//Constructs click stimuli for non-linear reponse extraction using 
		//3 click at one level and a fourth click at 3x the level
		//The sweepDurationinSeconds is the duration of a single epoch (trial) *including* the click's duration
		
		//TODO: Add proper reference to the choice of stimulus parameters
		final double clickDurationInSeconds=0.001;
		final double sweepDurationInSeconds=getclickKempSweepDurationSeconds();
		
		final int N = (int) (totalDurationInSeconds * sampleFrequency);
		final int clickN= (int) (clickDurationInSeconds * sampleFrequency);
		final int sweepN= (int) (sweepDurationInSeconds * sampleFrequency);
		final int sweeps= (int) Math.floor(N/sweepN);	
		final double maxAmp=-1;
		final double minAmp=Math.abs(maxAmp)*Math.pow(10,-3/20);  //small clicks are 3 dB lower in amp
		double[] x = new double[N];
		int index0, index1, n, m;
		for (n=0;n<sweeps;n++) {
			//Sweep loop
			index0=(int) (n*sweepN);
			index1=index0+clickN-1;
			for( m=index0;m<index1;m++){
				//Click loop,  4th click is the large one
				if(((n+1)%4)==0){
					x[m] = minAmp;
				}else{
					x[m] = maxAmp;
				}	
			}
		}
		return x;
	}
}
