/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
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
 * AudioPulseCalibrationActivity.java
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

package org.audiopulse.io;

import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.ThreadedSignalGenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioStreamer
{
	public static final String TAG = "AudioStreamer";
	public final int sampleRate;
	
	public static final int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
	public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	public static final int audioMode = AudioManager.STREAM_MUSIC;
	public static final int trackMode = AudioTrack.MODE_STREAM;
			
	private ThreadedSignalGenerator source = null;			//continuously generates samples to write to audio buffer
	private Thread playThread;								//contains main playback loop
	private boolean isPlaying = false;
	private volatile boolean requestStop;					//used to attempt to stop main playback loop
	private volatile AudioTrack track;

	private final int frameLength;		//num mono samples per audio frame
		
	private Object audioLock = new Object();
	
	//default buffer values constructor
	public AudioStreamer(int sampleRate) {
		this(sampleRate, 0);
	}
	//general constructor
	public AudioStreamer (int sampleRate, int frameLength)
	{
		//determine PCM buffer parameters
		int bytesPerFrame = frameLength * 2 * 2;		//2 bytes per sample, 2 buffer samples per stereo signal sample
		int minBufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		assert false : "Test assertion";
		if (bytesPerFrame < minBufferSizeInBytes) {
			if (frameLength != 0)		//use 0 for min buffer size
				Log.w(TAG, "Buffer size too short, using minBufferSize instead");
			assert minBufferSizeInBytes % 4==0 : "minBufferSize is either not in bytes or not for stereo buffers" ;
			frameLength = minBufferSizeInBytes / 4;
			bytesPerFrame = frameLength * 2 * 2;
		}
		
		this.sampleRate = sampleRate;
		this.frameLength = frameLength;
		
		//initialize AudioTrack object
		track = new AudioTrack(audioMode, sampleRate, channelConfig, audioFormat, bytesPerFrame, trackMode); 
		if(track.getState() != AudioTrack.STATE_INITIALIZED) {
			Log.e(TAG,"Error: Audio record was not properly initialized!!");
			throw new AssertionError("Invalid Audio State Configuration");
		}
		if(track.getChannelConfiguration() != AudioFormat.CHANNEL_OUT_STEREO) {
			Log.e(TAG,"Channel channel not stereo (" + 
		AudioFormat.CHANNEL_OUT_STEREO + " ) value is= " + track.getChannelConfiguration());
			throw new AssertionError("Invalid Audio  Channel Configuration");
		}
		
		if(track.getPlaybackRate() != sampleRate) {
			Log.e(TAG,"Channel rate not configure properly (" + 
		sampleRate + " ) playback value is= " + track.getPlaybackRate());
			throw new AssertionError("Invalid Plabyback Rate Configuration");
		}
		if(track.getSampleRate() != sampleRate) {
			Log.e(TAG,"Channel rate not configure properly (" + 
		sampleRate + " ) value is= " + track.getSampleRate());
			throw new AssertionError("Invalid Sample Rate Configuration");
		}
		
		Log.v(TAG,"Audio Track configuration: sampleRate= " + track.getSampleRate()
				+ " ChannelConfiguration= " + track.getChannelConfiguration() 
				+ " PlaybackRate" + track.getPlaybackRate());
	}
	
	//connect AudioStreamer object with a ThreadedSignalGenerator object that will give it a signal
	public void attachSource(ThreadedSignalGenerator source) {
		if (source.bufferLength != frameLength)
			throw new IllegalArgumentException("Source bufferLength must match AudioStreamer frameLength");
		
		this.source = source;
	}
	
	public boolean hasSource() {
		return (source!=null);
	}

	//begin playback
	public synchronized void start()
	{
		Log.d(TAG,"Stream start");
		
		if (source==null) {
			Log.e(TAG, "No source, cannot start AudioStreamer");
			return;
		}
		
		//create thread to handle playback loop
		playThread = new Thread( new Runnable( ) {
			public void run( ) {
				synchronized (audioLock) {		//block access if we're in the middle of a stop()
					track.pause();
					track.flush();
					track.play();
					isPlaying = true;
					requestStop = false;
				}
				
				while(!requestStop) {		//break out of loop if someone sets reqeustStop
					double[] frame = null;
					waitForSourceReady();		//wait if buffer hasn't been computed yet
					frame = source.getBuffer();	//gets current buffer, tells source to generate next buffer
					
					//write frame to audio out
					Log.v(TAG,"Writing audio frame");
					short[] writeData = AudioSignal.convertStereoToShort(AudioSignal.convertToStereo(frame));
					int nWritten = track.write(writeData,0,writeData.length);

					//discover if error
					if (nWritten == AudioTrack.ERROR_INVALID_OPERATION || nWritten == AudioTrack.ERROR_BAD_VALUE) {
						if (requestStop) Log.e(TAG, "Audio write failed: " + nWritten);
						else Log.v(TAG, "Audio write aborted due to stop request: " + nWritten);
						break;
					} else {
						Log.v(TAG,"Frame written successfully");
					}
				}
			}
		} );
		playThread.setPriority(Thread.MAX_PRIORITY);
		playThread.start();
		
	}
	public synchronized void stop() {
		Log.d(TAG,"AudioStreamer Stop");
		
		//send stop request, wait for success, then flush audio track
		new Thread( new Runnable() {
			public void run() {
				synchronized (audioLock) {	//block access if we're in the middle of a start()
					requestStop = true;
					if (isPlaying) {
						try {
							playThread.join();		//stop requested, now wait until playback loops exits
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					track.pause();
					track.flush();
					isPlaying = false;
				}
			}
			
		}).start();
		
		//track.release();	//don't really know if this is necessary (or sufficient)
	}
//	public void destroy() {
//		Log.d(TAG,"AudioStreamer Destroy");
//		stop();
//		track.release();
//	}
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public int getFrameLength() {
		return frameLength;
	}
	
	//suspend until source is ready with another buffer
	private void waitForSourceReady() {		//blocking call: do not call from UI thread!
		if (!source.isBufferReady()) Log.i(TAG,"Waiting for buffer");
		try {
			source.waitForBuffer();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG,"Wait for source ready interrupted!");
		}

	}

}