package org.audiopulse.utilities;

import android.util.Log;

public class ThreadedToneGenerator extends ThreadedSignalGenerator {
	private final String TAG = "ThreadedToneGenerator";
	private final double sampleFrequency;
	private double frequency=1000, amplitude=0.1;
	
	private double angle = 0, angleIncrement;
	
	public ThreadedToneGenerator(int bufferLength, double frequency, double sampleFrequency) {
		super(bufferLength);
		this.sampleFrequency = sampleFrequency;
		this.setFrequency(frequency);
	}

	@Override
	protected double[] computeNextBuffer() {
		double[] samples = new double[this.bufferLength];
		for (int n=0; n<this.bufferLength; n++) {
			angle += angleIncrement;
			samples[n] = amplitude * Math.sin(angle);
		}
		return samples;
	}
	
	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
		angleIncrement = 2*Math.PI*frequency/sampleFrequency;
		Log.d(TAG, "Set toneGenerator frequency to " + frequency);
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}	

}
