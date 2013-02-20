package org.audiopulse.io;

import org.audiopulse.utilities.AudioSignal;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

//TODO: allow checking of threads' status, interrupting, etc.
//TODO: make sure we can set STATIC or STREAM
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
	
	//set to playback only, specify stimulus
	public synchronized void setPlaybackOnly(int playbackSampleFreq, double[][] stimulus) {
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				enablePlayback(playbackSampleFreq, stimulus);
				this.recordingEnabled = false;
			}
		}		
	}
	
	//set to playback and record, specify stimulus and recording pre/post roll
	public synchronized void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq) {

		int recordingSampleLength = stimulus[0].length * recordingSampleFreq / playbackSampleFreq;
		recordingSampleLength += recordingPadInMillis * recordingSampleFreq / 1000;
		synchronized(playbackLock) {
			synchronized (recordingLock) {		
				enablePlayback(playbackSampleFreq, stimulus);
				enableRecording(recordingSampleFreq, recordingSampleLength);
			}
		}
	}
	
	@Deprecated
	public synchronized void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq, int prerollInMillis, int postRollInMillis) {
		setPlaybackAndRecording(playbackSampleFreq, stimulus, recordingSampleFreq);
	}
	
	//set to record only, specify recording time
	public synchronized void setRecordingOnly(int recordingSampleFrequency, int recordTimeInMillis) {
		int recordingSampleLength = (recordTimeInMillis * recordingSampleFrequency) / 1000;
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = false;
				enableRecording(recordingSampleFrequency, recordingSampleLength);
			}
		}
	}
	
	//private methods used by the public methods above
	private void enablePlayback(int sampleFrequency, double[][] stimulus) {
		//copy/convert data before potentially blocking on sync locks
		short[] stimulusDataToWrite = AudioSignal.convertStereoToShort(stimulus);
		
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = true;
				this.playbackCompleted = false;
				this.playbackSampleRate = sampleFrequency;
				this.stimulusData = stimulusDataToWrite;
				initializePlayer();				
			}
		}
	}
	
	private void enableRecording(int sampleFrequency, int recordingSampleLength) {
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				this.recordingEnabled = true;
				this.recordingCompleted = false;
				this.recordingSampleRate = sampleFrequency;
				this.numSamplesToRecord = recordingSampleLength;
				initializeRecorder();
			}
		}
	}
	
	//start playback and/or recording
	//TODO - confirm thread safety
	public synchronized void start() {
		//start IO
		if (recordingEnabled) {
			recordingThread.start();
		}
		if (playbackEnabled) {
			playbackThread.start();
		}
//		if (recordingEnabled) {
//			recordingThread.start();	//recorder will trigger playback if enabled.
//		} else if (playbackEnabled) {
//			playbackThread.start();
//		} else {
//			Log.w(TAG,"No recording or playback set, doing nothing!");
//			return;
//		}

		//wait for completion before returning control
		//FIXME - how do we set playbackComplete using MODE_STATIC?
		try {
			while (!isIoComplete()) {
				Log.d(TAG,"Waiting for IO completion...");
				this.wait();
				Log.d(TAG,"start() thread woken up, checking if we're done.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.w(TAG,"start thread interrupted");
		}
		
		Log.d(TAG,"We're done, returning control.");
			
	}
	
	//stop all I/O
	public void stop() {
		//TODO
	}
	
	//get recorded waveform
	public synchronized double[] getResult() {
		//wait until IO completes or interrupted
		try {
			while (!isIoComplete()) {
				this.wait();
				Log.d(TAG,"getRestuls thread woken up, checking if we're done.");
			}
			return recordedAudio;
		} catch (InterruptedException e) {
			Log.d(TAG,"Interrupted at getResults");
			e.printStackTrace();
			return null;
		}
		
	}
	
	//run within playbackThread to write samples to hardware output buffer
	private void playbackLoop() {
		synchronized (playbackLock) {
			stopRequest = false;
			int numSamplesToWrite = stimulusData.length;
			
			if (recordingEnabled) {
				//wait for recording loop to tell us its ready
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
			
			Log.d(TAG,"Starting play loop: " + numSamplesToWrite + " samples to write.");
			for (numSamplesPlayed=0; numSamplesPlayed<numSamplesToWrite;) {
				if (stopRequest) {
					break;
				}
				
				int remainingSamples = numSamplesToWrite - numSamplesPlayed;
				int writeSize=(playerBufferLength<=remainingSamples) ? playerBufferLength : remainingSamples;
				int nWritten = player.write(stimulusData, numSamplesPlayed, writeSize);
				if (nWritten==AudioTrack.ERROR_BAD_VALUE || nWritten==AudioTrack.ERROR_INVALID_OPERATION) {
					Log.e(TAG, "Audio write failed: " + nWritten);
					//TODO: send a useful message to main activity informing of failure
				}
				numSamplesPlayed+= nWritten;
			}
			player.pause();
			Log.d(TAG,"Done playbackLoop");
			playbackCompleted = true;
			doneLoop();
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
			
			//let playback loop know we're ready and recording.
			recordingStarted = true;
			synchronized (playRecordSyncLock) {
				playRecordSyncLock.notify();
				Log.d(TAG,"Telling playback loop to go");
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
					requestSize = remainingSamples;
				}
			
				//perform read
				nRead=recorder.read(recordedData,numSamplesRecorded,requestSize);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					//TODO: send a useful message to main activity informing of failure
				}
				numSamplesRecorded += nRead;
			}
			
			recorder.stop();
			Log.d(TAG,"Done recordingLoop");
			
			recordedAudio = AudioSignal.convertMonoToDouble(recordedData);
			recordingCompleted = true;
			doneLoop();
		}
	}
	
	//Check if play&record are done, notify if so.
	private void doneLoop() {
		Log.d(TAG,"Done a loop, checking conditions...");
		if (isIoComplete()) {
			Log.d(TAG,"IO Complete!");
			synchronized(this) {
				this.notifyAll();
			}
		} else Log.d(TAG,"Nope, still waiting...");
	}
	
	//determine if play and record (if enabled) are completed
	private boolean isIoComplete() {
		return ((!playbackEnabled || playbackCompleted) &&
				(!recordingEnabled || recordingCompleted));
	}
	
	//prepare the AudioPlayer with user-specified parameters
	private void initializePlayer() {
		
		synchronized (playbackLock) {
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
	
	//prepare the AudioRecorder with user-specified parameters
	private void initializeRecorder() {
		synchronized (recordingLock) {
			if (recorder!=null) recorder.release();
			
			recordedData = new short[numSamplesToRecord];
			
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
