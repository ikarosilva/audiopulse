package org.audiopulse.utilities;

import java.util.Date;

import android.util.Log;

public abstract class ThreadedSignalGenerator {
	private final String TAG = "ThreadedSignalGenerator";
	private double[] buffer;			//Intentionally private. Subclass can only return new buffers and are not allowed to overwrite the existing buffer themselves.
	public final int bufferLength;

	private boolean isBufferRead = true;	//has the previously generated buffer been read by the client?
	private boolean initialized = false;	//has this object been initialized? (call initialize());

	private Thread computer;
	
	
	public ThreadedSignalGenerator(int bufferLength) {
		this.bufferLength = bufferLength;
	}
	
	public int getBufferLength() {
		return bufferLength;
	}
	
	//compute first buffer so we're ready to roll. We don't want this in the
	//constructor because subclasses (e.g. ToneGenerator) may set other parameters
	//in their constructor (e.g. frequency) that we need to wait for befor
	//computing our first buffer.
	public void initialize() {
		if (initialized) return;
		computeSamples();
		initialized = true;
	}

	//return current buffer, mark as read, and begin computing next buffer
	public final double[] getBuffer() {
		Log.d(TAG,"Buffer requested");
		
		if (!initialized) initialize();
		if (isBufferRead) {
			Log.e(TAG,"Next buffer not ready!");
			return null;
		}
		
		double[] returnBuffer = buffer;		//save reference to buffer before we tell computeSamples() to replace it
		isBufferRead = true;
		threadedComputeSamples();
		return returnBuffer;
	}
		
	private synchronized void computeSamples() {
		if (!isBufferRead)
			Log.e(TAG,"SignalGenerator buffer dropped!");

		long t = new Date().getTime();
		
		buffer = computeNextBuffer();		//getSamples is implemented by subclasses
		
		t = new Date().getTime() - t;
		Log.v(TAG, this.getClass().getSimpleName() + " buffer computed in " + t + "ms, rms = " + SignalProcessing.rms(buffer));
		
		assert buffer != null && buffer.length==bufferLength : "Signal generator returned invalid buffer";
		
		isBufferRead = false;
		
	}
	
	private synchronized void threadedComputeSamples() {
		Log.d(TAG,"Spawning thread to compute next buffer");
		
		computer = new Thread( new Runnable( ) {
			public void run( ) { computeSamples(); }
		} );
		computer.setPriority(Thread.MAX_PRIORITY);
		computer.start();

	}
	
	//is the next buffer ready to read?
	public boolean isReady() {
		return !isBufferRead;
	}
	public boolean isInitialized() {
		return initialized;
	}
	
	//nextSample will be implemented by subclasses. Subclasses are responsible for keeping
	//any internal state variables, such as currentSample, and parameters, such as sampleFrequency and amplitude.
	//protected abstract double nextSample();
	protected abstract double[] computeNextBuffer();
}
