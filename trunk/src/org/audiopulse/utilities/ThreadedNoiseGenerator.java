package org.audiopulse.utilities;

import java.util.Date;
import java.util.Random;
import android.util.Log;

public class ThreadedNoiseGenerator extends ThreadedSignalGenerator {
	private final String TAG = "ThreadedNoiseGenerator";
	private final double sampleFrequency;
	private double amplitude=0.1;
	private Random RNG;
	
	private final int bigBufferMultipler = 4;
	private double[] bigBuffer;
	
	public ThreadedNoiseGenerator(int bufferLength) {
		this(bufferLength,1);
	}
	public ThreadedNoiseGenerator(int bufferLength, double sampleFrequency) {
		super(bufferLength);
		this.sampleFrequency = sampleFrequency;
		this.RNG = new Random();
		
		//Math.random and java.util.Random are for some reason very slow, too slow for 44.1 kHz streaming
		//So instead we create a long buffer, and return a random sample from within this buffer
		long t = new Date().getTime();
		bigBuffer = new double[bufferLength * bigBufferMultipler];
		for (int n=0; n<bigBuffer.length; n++) {
			bigBuffer[n] = (2*RNG.nextDouble() - 1);
		}
		t = new Date().getTime() - t;
		Log.d(TAG,"Noise samples created in " + t + "ms");
	}

	protected double[] computeNextBuffer() {
//		double[] samples = new double[numSamples];
//		for (int n=0; n<numSamples; n++) {
//			samples[n] = amplitude * (2*RNG.nextDouble() - 1);
//		}
//		
//		return samples;
		
		//select a random sample from bigBuffer
		int index = RNG.nextInt(bigBuffer.length);		//starting point of sample
		int step = RNG.nextInt(bigBufferMultipler)+1;		//increase sample space by allowing multiple sample stepping values
		boolean dir = RNG.nextBoolean();				//increase sample space by allowing samples to go up or down
		double[] samples = new double[bufferLength];
		for (int n=0; n<this.bufferLength; n++) {
			int bn = (index+(dir?step:-step)*n)%(bigBuffer.length);
			if (bn<0) bn+=bigBuffer.length;
			samples[n] = amplitude * bigBuffer[bn];
		}
		return samples;
	}
	
	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}	

}
