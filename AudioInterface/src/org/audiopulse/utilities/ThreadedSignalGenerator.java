/* SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * ThreadedSignalGenerator.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Andrew Schwartz
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

 package org.audiopulse.utilities;

import java.util.Date;

import android.util.Log;

/*
 * ThreadedSignalGenerator: use a worker thread to generate a signal, one fixed-buffer-length at a time.
 * 
 * Use with AudioStreamer.java, calling the attachSource() method.
 * 
 * ThreadedSignalGenerator is abstract, and should be extended for a specific signal type.
 * Examples provided are ThreadedToneGenerator, ThreadedClickGenerator, and ThreadedNoiseGenerator
 * Subclasses must implement protected abstract double[] computeNextBuffer(); and are responsible for
 * keeping their own internal state variables, such as time or samples, sample frequency, amplitude, etc
 * 
 * The only parameter that ThreadedSignalGenerator requires is int bufferLength, which cannot be
 * changed once instantiated.
 * 
 * ThreadedSignalGenerator is optimized to return data as quickly as possible as needed. You can call initialize()
 * once the object is constructed to compute the first buffer. From then on, each time getBuffer() is
 * called, a new thread is spawned to compute the next buffer. If getBuffer() is called before initialize(),
 * getBuffer() will call initialize() and wait for it to complete before returning the buffer.
 */
public abstract class ThreadedSignalGenerator {
	private final String TAG = "ThreadedSignalGenerator";
	public final int bufferLength;

	private volatile double[] buffer;	//Not accessible by subclasses. Subclass must return new arrays and are not permitted to modify this buffer.
	private volatile boolean isBufferReady = false;	//is the buffer ready to be read by client?
	private volatile boolean initialized = false;	//has this object been initialized? (call initialize());

	private Thread computeThread;
	
	private Object readBufferLock = new Object(); 		//lock for read buffer processes, to allow updates to other vars
	private Object writeBufferLock = new Object();		//lock for write buffer processes
	
	//All subclasses will be required to call this constructor to set bufferLength.
	public ThreadedSignalGenerator(int bufferLength) {
		this.bufferLength = bufferLength;
	}
		
	//compute first buffer so we're ready to roll. We don't want this in the
	//constructor because subclasses (e.g. ToneGenerator) may set other parameters
	//in their constructor (e.g. frequency) that we need to wait for before
	//computing our first buffer. (Subclasses are required to call super(bufferLength) as a first statement)
	public void initialize() {
		if (initialized) return;
		threadedComputeSamples();
		initialized = true;
		Log.v(TAG,"Signal generator (" + this.getClass().getSimpleName() + ") initialized.");
	}
	//if source parameters (e.g. freq, amplitude) change, call recompute() to replace the existing
	//buffer so that the next getBuffer() call will return the correct signal
	public void recompute() {
		threadedComputeSamples();	//synchronized, so if a computeThread is active it will wait and replace it
	}
	

	// Return current buffer, mark as read, and begin computing next buffer.
	// Returns null of buffer is not ready. Use waitForBuffer(...) to wait.
	public final double[] getBuffer() {
		double[] returnBuffer;
		
		if (!initialized) initialize();
		if (!isBufferReady) {
			Log.e(TAG,"Next buffer not ready!");
			return null;
		}
		
		synchronized (readBufferLock) {	//prevent interleaved double-access to buffer; once we read it, it's gone.
			returnBuffer = buffer;		//save reference to current buffer as computeSamples() will overwrite it
			isBufferReady = false;				//mark buffer as not ready until computeThread finishes.
		}
		
		threadedComputeSamples();			//spawn new thread to compute next buffer
		return returnBuffer;
	}
		
	private void computeSamples() {
		synchronized (writeBufferLock) {				//recompute() can be called externally, so we must lock
			isBufferReady = false;
			buffer = computeNextBuffer();		//computeNextBuffer is implemented by subclasses

			// Check that subclass.computeNextBuffer returns something valid
			if (buffer == null || buffer.length!=bufferLength) {
				throw new RuntimeException();
			}
			
			isBufferReady = true;
		}
	}
	
	private void threadedComputeSamples() {
		computeThread = new Thread( new Runnable( ) {
			public void run( ) { computeSamples(); }
		} );

		computeThread.setPriority(Thread.MAX_PRIORITY);
		computeThread.start();

	}
	
	//is the next buffer ready to read?
	public boolean isBufferReady() {
		synchronized (readBufferLock) {
			return isBufferReady;
		}
	}
	public boolean isInitialized() {
		return initialized;
	}
	
	public void waitForBuffer() throws InterruptedException {
		if (!initialized) initialize();
		assert computeThread != null;
		if (!isBufferReady) {
			Log.d(TAG,"Waiting for buffer!");
			computeThread.join();	//wait for computing thread to finish
		}
	}
	public void waitForBuffer(long millis) throws InterruptedException {
		if (!initialized) initialize();
		assert computeThread != null;
		if (!isBufferReady) computeThread.join(millis);	//wait for computing thread to finish
	}
	public void waitForBuffer(long millis, int nanos) throws InterruptedException {
		if (!initialized) initialize();
		assert computeThread != null;
		if (!isBufferReady) computeThread.join(millis,nanos);	//wait for computing thread to finish
	}
	
	//nextSample will be implemented by subclasses. Subclasses are responsible for keeping
	//any internal state variables, such as currentSample, and parameters, such as sampleFrequency and amplitude.
	//protected abstract double nextSample();
	protected abstract double[] computeNextBuffer();
}
