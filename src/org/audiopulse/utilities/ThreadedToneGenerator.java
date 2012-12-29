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
	protected double nextSample() {
		angle += angleIncrement;
		return amplitude * Math.sin(angle);
	}
	
	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
		angleIncrement = 2*Math.PI*frequency/sampleFrequency;
		Log.v(TAG, "Set toneGenerator frequency to " + frequency);
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}	

}
