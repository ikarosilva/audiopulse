package org.audiopulse.utilities;

import android.util.Log;

public class ThreadedClickGenerator extends ThreadedSignalGenerator {
	private final String TAG = "ThreadedClickGenerator";
	private final double sampleFrequency, samplePeriod;
	private double frequency=10, amplitude=0.1;
	
	private double t = 0;
	private double clickInterval, clickDuration = 50e-6;
	
	public ThreadedClickGenerator(int bufferLength, double frequency, double sampleFrequency) {
		super(bufferLength);
		this.sampleFrequency = sampleFrequency;
		samplePeriod = 1/sampleFrequency;
		this.setFrequency(frequency);
	}

	@Override
	protected double[] computeNextBuffer() {
		double[] samples = new double[this.bufferLength];
		for (int n=0; n<this.bufferLength; n++) {
			t += samplePeriod;
			samples[n] = (t%clickInterval)<clickDuration ? amplitude : 0;
		}
		return samples;
	}
	
	
	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
		clickInterval = 1/frequency;
		Log.v(TAG, "Set clickGenerator frequency to " + frequency);
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}	

}
