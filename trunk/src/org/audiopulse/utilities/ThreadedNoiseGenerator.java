package org.audiopulse.utilities;

import java.util.Date;
import java.util.Random;
import android.util.Log;

public class ThreadedNoiseGenerator extends ThreadedSignalGenerator {
	private final String TAG = "ThreadedNoiseGenerator";
	private final double sampleFrequency;
	private double amplitude=0.1;
	private Random RNG;
	
	private final int numBuffers = 64;
	private double[][] buffers;
	
	
	public ThreadedNoiseGenerator(int bufferLength) {
		this(bufferLength,1);
	}
	public ThreadedNoiseGenerator(int bufferLength, double sampleFrequency) {
		super(bufferLength);
		this.sampleFrequency = sampleFrequency;
		this.RNG = new Random();
		
		//Math.random and java.util.Random are for some reason very slow, too slow for 44.1 kHz streaming
		//So instead I will create several buffers a priori and just return them.
		buffers = new double[numBuffers][bufferLength];
		for (int ii=0; ii<numBuffers; ii++) {
			for (int n=0; n<bufferLength; n++) {
				buffers[ii][n] = (2*RNG.nextDouble() - 1);
			}
		}
	}

	protected double[] computeNextBuffer() {
//		double[] samples = new double[numSamples];
//		for (int n=0; n<numSamples; n++) {
//			samples[n] = amplitude * (2*RNG.nextDouble() - 1);
//		}
//		
//		return samples;
		
		//select a random pre-computed buffer, scale it by amplitude.
		double[] samples = buffers[(int)(Math.random()*numBuffers)].clone();
		for (int n=0; n<this.bufferLength; n++) {
			samples[n] *= amplitude;
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
