package org.audiopulse.utilities;

import android.util.Log;

public abstract class ThreadedSignalGenerator {
	protected final String TAG = "ThreadedSignalGenerator";
	protected double[] buffer, nextBuffer;
	private final int bufferLength;

	private boolean isBufferRead = true;	//has the previously generated buffer been read by the client?
	private boolean initialized = false;	//has this object been initialized? (call initialize());
	
	private Thread computer;
	
	public ThreadedSignalGenerator(int bufferLength) {
		this.bufferLength = bufferLength;
	}
	
	public int getBufferLength() {
		return bufferLength;
	}
	
	public void initialize() {
		if (initialized) return;
		computeSamples();
		initialized = true;
	}

	//return current buffer, mark as read, and begin computing next buffer
	public final double[] getBuffer() {
		if (!initialized) initialize();
		if (isBufferRead) {
			Log.e(TAG,"Next buffer not ready!");
			return null;
		}
		
		double[] returnBuffer = buffer;
		isBufferRead = true;
		threadedComputeSamples();
		return returnBuffer;
	}
	
	private synchronized void computeSamples() {
		
		nextBuffer = new double[bufferLength];
		
		for (int n=0; n<bufferLength; n++) {
			nextBuffer[n] = nextSample();
		}
		
		if (!isBufferRead)
			Log.e(TAG,"SignalGenerator buffer dropped!");
		buffer = nextBuffer;	//replace old buffer with computed one 
		//nextBuffer = null;		//make sure no one accidentally writes over buffer using nextBuffer
		isBufferRead = false;
		
		Log.v(TAG,"Next buffer computed, rms = " + SignalProcessing.rms(buffer));

	}
	
	private synchronized void threadedComputeSamples() {
		
		computer = new Thread( new Runnable( ) {
			public void run( ) { computeSamples(); }
		} );
		computer.setPriority(Thread.MAX_PRIORITY);
		computer.start();

	}
	
	//nextSample will be implemented by subclasses. Subclasses are responsible for keeping
	//any internal state variables, such as currentSample, and parameters, such as sampleFrequency and amplitude.
	protected abstract double nextSample();
}
