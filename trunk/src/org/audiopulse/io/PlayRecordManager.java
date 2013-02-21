package org.audiopulse.io;

import java.io.IOException;

import org.audiopulse.utilities.AudioSignal;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

//TODO: allow checking of threads' status, interrupting, etc.
public class PlayRecordManager {
	private static final String TAG = "PlayRecordManager";

	private AudioTrack player;
	private AudioRecord recorder;
	private boolean recordingEnabled = false;
	private boolean playbackEnabled = false;
	
	private int playbackSampleRate;
	private int recordingSampleRate;
	private int playerBufferLength = 4096;			//length of buffer to write in one chucnk
	private int recorderBufferLength = 4096;		//length of recording buffer
	private int recorderReadLength = 512;			//# samples to read at a time from recording buffer
	
	private int playerMode;						//MODE_STATIC or MODE_STREAM
	private volatile int numSamplesPlayed;				//# samples played so far
	private volatile int numSamplesRecorded;				//# sample recorded so far
	private int numSamplesToRecord;				//total # samples to record
	
	private short[] stimulusData;			//playback data
	private short[] recordedData;			//direct buffer to write recorded samples to
	private double[] recordedAudio;			//recorded audio converted to double
	private final int recordingPadInMillis = 1000;			//extra time (ms) for recording buffer to cover sync issues
	
	private volatile boolean stopRequest;
	private volatile boolean playbackStarted;
	private volatile boolean playbackCompleted;
	private volatile boolean recordingStarted;
	private volatile boolean recordingCompleted;

	private Thread playbackThread = new Thread();
	private Thread recordingThread = new Thread();
	private Object playbackLock = new Object();
	private Object recordingLock = new Object();
	private Object playRecordSyncLock = new Object();
	
	public AudioRecord.OnRecordPositionUpdateListener recordListener;
	
	//TODO: have public functions return success, or possibly throw exceptions
	
	//set to playback only, specify stimulus
	public synchronized void setPlaybackOnly(int playbackSampleFreq, double[][] stimulus) {
		
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = true;
				this.recordingEnabled = false;
				initializePlayback(playbackSampleFreq, stimulus);
				
			}
		}		
	}
	
	//set to playback and record, specify stimulus
	public synchronized void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq) {

		//sync recording length to playback length
		int recordingSampleLength = stimulus[0].length * recordingSampleFreq / playbackSampleFreq;
		//but add some padding to allow wiggle-room
		recordingSampleLength += recordingPadInMillis * recordingSampleFreq / 1000;
		
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = true;
				this.recordingEnabled = true;
				initializePlayback(playbackSampleFreq, stimulus);
				initializeRecording(recordingSampleFreq, recordingSampleLength);
			}
		}
	}
		
	//set to record only, specify recording time
	public synchronized void setRecordingOnly(int recordingSampleFrequency, int recordTimeInMillis) {
		
		int recordingSampleLength = (recordTimeInMillis * recordingSampleFrequency) / 1000;
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = false;
				this.recordingEnabled = true;
				initializeRecording(recordingSampleFrequency, recordingSampleLength);
			}
		}
	}

	//TODO: allow native write result to file?
	
	//start playback and/or recording
	public synchronized double[] acquire() {
		//start IO
		if (recordingEnabled) {
			recordingThread.start();
		}
		if (playbackEnabled) {
			playbackThread.start();
		}

		//wait for completion before returning control
		try {
			while (!isIoComplete()) {
				Log.d(TAG,"Waiting for IO completion...");
				this.wait();
				Log.d(TAG,"start() thread woken up, checking if we're done.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.e(TAG,"start thread interrupted");
			return null;
		}
		
		Log.d(TAG,"We're done, returning control.");
		return recordedAudio.clone();
		
	}
	
	//stop all I/O
	public void stop() {
		Log.i(TAG,"playRecordManager manually stopped!");
		stopRequest = true;
		//TODO: use interrupt to stop rogue read() or write() call? would that even work?
	}
	
	@Deprecated
	public synchronized double[] getResult() {
		Log.e(TAG,"Using deprecatecd, non-functional getResult() function");
		return null;
	}
	@Deprecated
	public void start() {
		Log.e(TAG,"Using deprecatecd, non-functional start() function");
	}
	@Deprecated
	public synchronized void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq, int prerollInMillis, int postRollInMillis) {
		Log.e(TAG,"Using deprecatecd, non-functional setPlaybackAndRecording(int,double[][],int,int,int) function");
	}

	
	
	
	//run within playbackThread to write samples to hardware output buffer
	private void playbackLoop() {
		synchronized (playbackLock) {
			stopRequest = false;
			int numSamplesToWrite = stimulusData.length;
			
			if (recordingEnabled) {
				//wait for recording to start
				try {
					synchronized (playRecordSyncLock) {
						while (!recordingStarted) {
							Log.d(TAG,"Playback loop waiting for record loop");
							playRecordSyncLock.wait();
						}
						Log.d(TAG,"Playback loop received the go!");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG,"Interrupted while waiting for record loop to start!");
					return;
				}
			}
			
			//start playback loop
			Log.d(TAG,"Starting play loop: " + numSamplesToWrite + " samples to write.");
			playbackStarted = true;
			for (numSamplesPlayed=0; numSamplesPlayed<numSamplesToWrite;) {
				if (stopRequest) {
					break;
				}
				
				int remainingSamples = numSamplesToWrite - numSamplesPlayed;
				int writeSize=(playerBufferLength<=remainingSamples) ? playerBufferLength : remainingSamples;
				int nWritten = player.write(stimulusData, numSamplesPlayed, writeSize);
				if (nWritten==AudioTrack.ERROR_BAD_VALUE || nWritten==AudioTrack.ERROR_INVALID_OPERATION) {
					Log.e(TAG, "Audio write failed: " + nWritten);
					//TODO: throw exception?
					//throw(new IOException("Audio write failed with code " + nWritten));
				}
				numSamplesPlayed+= nWritten;
			}
			player.pause();
			Log.d(TAG,"Done playbackLoop");
			playbackCompleted = true;
			notifyIOComplete();		//notify this if all IO is done
		}
	}

	//run within recordingThread to read samples from hardware buffer
	private void recordLoop() {
		synchronized (recordingLock) {
			Log.d(TAG,"Starting record loop: " + numSamplesToRecord + " samples to record.");
			stopRequest = false;
			numSamplesRecorded = 0;
			
			//first, request entire buffer to mark all samples as read
			int nRead = recorder.read(new short[recorderBufferLength],0,recorderBufferLength);
			
			//inform playback loop that recording has started
			recordingStarted = true;
			if (playbackEnabled) {
				synchronized (playRecordSyncLock) {
					playRecordSyncLock.notify();
					Log.d(TAG,"Telling playback loop to go");
				}
			}
			
			//start main recording loop
			while (!stopRequest) {
				int requestSize = recorderReadLength;
				
				//if playback is enabled & done, so are we.
				//so we will request final buffer (full length)
				if (playbackEnabled && playbackCompleted) {
					requestSize = recorderBufferLength;
					stopRequest = true;
					Log.d(TAG,"Playback loop finished, recording loop to terminate");
				}
				
				//make sure we don't overflow
				int remainingSamples = numSamplesToRecord - numSamplesRecorded;
				if (remainingSamples<recorderReadLength) {
					if (!playbackEnabled) {
						//in this case, we've just reached the end, and should stop
						requestSize = remainingSamples;
					} else {
						//in this case, something went wrong as we should have stopped already
						Log.e(TAG,"Ran out of recording buffer space, IO sync error?");
						//TODO: throw exception
//						throw(new IOException("Play/Record sync error: " +
//											  numSamplesPlayed + " samples played, " + 
//											  numSamplesRecorded + " samples recorded, " + 
//											  numSamplesToRecord + " total samples to record"));
					}
					
				}
			
				//perform read
				nRead=recorder.read(recordedData,numSamplesRecorded,requestSize);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					//TODO: throw exception?
					//throw(new IOException("Audio read failed with code " + nRead));

				}
				numSamplesRecorded += nRead;
			}
			
			recorder.stop();
			
			Log.d(TAG,"Done recordingLoop, recorded " + numSamplesRecorded + " samples.");
			
			//keep only the part of the data buffer that we wrote to
			short[] finishedAudio = new short[numSamplesRecorded];
			System.arraycopy(recordedData, 0, finishedAudio, 0, numSamplesRecorded);
			//convert to double
			recordedAudio = AudioSignal.convertMonoToDouble(finishedAudio);
			recordingCompleted = true;
			notifyIOComplete(); 	//notify this if all IO is done
		}
	}
	
	//Check if play&record are done, notify if so.
	private void notifyIOComplete() {
		if (isIoComplete()) {
			synchronized(this) {
				this.notifyAll();
			}
		}
	}
	
	//determine if play and record (if enabled) are completed
	private boolean isIoComplete() {
		return ((!playbackEnabled || playbackCompleted) &&
				(!recordingEnabled || recordingCompleted));
	}
	
	
	//initialize everything we need to trigger playback
	private void initializePlayback(int sampleFrequency, double[][] stimulus) {
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				
				this.playbackSampleRate = sampleFrequency;
				this.stimulusData = AudioSignal.convertStereoToShort(stimulus);;
				this.playbackCompleted = false;
				
				if (player!=null) player.release();
				
				//set up AudioTrack object (interface to playback hardware)
				playerMode = AudioTrack.MODE_STREAM;	//TODO: MODE_STATIC? Google bug?
				int minBuffer = AudioTrack.getMinBufferSize(
						playbackSampleRate, 
						AudioFormat.CHANNEL_OUT_STEREO,
						AudioFormat.ENCODING_PCM_16BIT);
				int bufferSizeInBytes = 2 * playerBufferLength;
				if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
				player = new AudioTrack(AudioManager.STREAM_MUSIC,
						  playbackSampleRate,
						  AudioFormat.CHANNEL_OUT_STEREO,
						  AudioFormat.ENCODING_PCM_16BIT,
						  bufferSizeInBytes,
						  playerMode);
				player.play();
				
				//set up Thread that will run playback loop
				playbackThread = new Thread( new Runnable() {
					public void run() {
						playbackLoop();
					}
				}, "PlaybackThread");
			}
		}
	}
	
	//initialize everything we need to trigger recording
	private void initializeRecording(int sampleFrequency, int recordingSampleLength) {
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				
				this.recordingSampleRate = sampleFrequency;
				this.numSamplesToRecord = recordingSampleLength;
				recordedData = new short[numSamplesToRecord];
				this.recordingCompleted = false;
				
				if (recorder!=null) recorder.release();
				
				//set up AudioRecord object (interface to recording hardware)
				int minBuffer = AudioRecord.getMinBufferSize(
						recordingSampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT
						);
				int bufferSizeInBytes = 2 * recorderBufferLength;
				if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
				recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
						recordingSampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT,
						bufferSizeInBytes);
				recorder.startRecording();

				//set up thread that will run recording loop
				recordingThread = new Thread( new Runnable() {
					public void run() {
						recordLoop();
					}
				}, "RecordingThread");
			}
		}
	}
	
		
}
