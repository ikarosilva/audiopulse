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
	
	private int numSamplesToPlay;					//total # samples in playback
	private int playerMode;						//MODE_STATIC or MODE_STREAM
	private int numSamplesToRecord;				//total # samples to record
	private volatile int numSamplesPlayed;				//# samples played so far
	private volatile int numSamplesRecorded;				//# sample recorded so far
	
	private int preroll, postroll;
	private short[] recordedData;
	private double[] recordedAudio;
	private short[] stimulusData;
	
	private volatile boolean stopRequest;
	private volatile boolean playbackStarted;
	private volatile boolean playbackCompleted;
	private volatile boolean recordingStarted;
	private volatile boolean recordingCompleted;

	private Thread recordingThread = new Thread();
	private Thread playbackThread = new Thread();
	
	public AudioRecord.OnRecordPositionUpdateListener recordListener;
	
	//set to playback only, specify stimulus
	public void setPlaybackOnly(int playbackSampleFreq, double[][] stimulus) {
		synchronized (playbackThread) {
			this.playbackEnabled = true;
			this.playbackCompleted = false;
			this.playbackSampleRate = playbackSampleFreq;
			this.stimulusData = AudioSignal.convertStereoToShort(stimulus);
			this.numSamplesToPlay = stimulus[0].length;
			initializePlayer();
		}
		synchronized (recordingThread) {
			this.recordingEnabled = false;
		}
		
	}
	
	//set to playback and record, specify stimulus and recording pre/post roll
	public void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq, int prerollInMillis, int postrollInMillis) {

		//figure out time intervals in samples
		this.preroll = prerollInMillis * recordingSampleFreq / 1000;
		this.postroll = postrollInMillis * recordingSampleFreq / 1000;

		synchronized (playbackThread) {
			this.playbackEnabled = true;
			this.playbackCompleted = false;
			this.playbackSampleRate = playbackSampleFreq;
			this.stimulusData = AudioSignal.convertStereoToShort(stimulus);
			this.numSamplesToPlay = stimulus[0].length;
			initializePlayer();
		}
		synchronized (recordingThread) {
			this.recordingEnabled = true;
			this.recordingCompleted = false;
			this.recordingSampleRate = recordingSampleFreq;
			this.numSamplesToRecord = preroll + stimulus[0].length + postroll;
			//FIXME - don't assume sample rates are the same! numSamplesToRecord should be based on timing, not sample numbers
			initializeRecorder();
			
			//define listener for recorder that will trigger playback after preroll
			recordListener = new AudioRecord.OnRecordPositionUpdateListener() {
				public void onMarkerReached(AudioRecord recorder) {
					if (playbackEnabled) {
						Log.d(TAG,"Playback trigged at " + numSamplesRecorded + " recorded samples");
						playbackThread.start();
					} 
				}
				public void onPeriodicNotification(AudioRecord recorder) {
					//unused
				}
			};
			recorder.setRecordPositionUpdateListener(recordListener);
			recorder.setNotificationMarkerPosition(preroll);
			
		}
		
	}
	
	//set to record only, specify recording time
	public void setRecordingOnly(int recordingSampleFrequency, int recordTimeInMillis) {
		synchronized (playbackThread) {
			this.playbackEnabled = false;
		}
		synchronized (recordingThread) {
			this.recordingEnabled = true;
			this.recordingCompleted = false;
			this.recordingSampleRate = recordingSampleFrequency;
			this.numSamplesToRecord = recordTimeInMillis * recordingSampleFrequency / 1000;

			initializeRecorder();
		}
	}
	
	private void enablePlayback() {
		
	}
	private void enableRecording() {
		
	}
	
	//start playback and/or recording
	public synchronized void start() {
		//start IO
		if (recordingEnabled) {
			recordingThread.start();	//recorder will trigger playback if enabled.
		} else if (playbackEnabled) {
			playbackThread.start();
		} else {
			Log.w(TAG,"No recording or playback set, doing nothing!");
			return;
		}

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

	//run within recordingThread to read samples from hardware buffer
	private void recordLoop() {
		synchronized (recordingThread) {
			Log.d(TAG,"Starting record loop: " + numSamplesToRecord + " samples to record.");
			stopRequest = false;
			//recording loop
			for (numSamplesRecorded=0; numSamplesRecorded<numSamplesToRecord; ) {
				if (stopRequest) {	//TODO: should this be done via an interrupt?
					//TODO: message successful stop, cleanup?
					break;
				}
				
				int remainingSamples = numSamplesToRecord - numSamplesRecorded;
				int requestSize=(recorderReadLength<=remainingSamples) ? recorderReadLength : remainingSamples;
				int nRead=recorder.read(recordedData,numSamplesRecorded,requestSize);
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
	
	//run within playbackThread to write samples to hardware output buffer
	private void playbackLoop() {
		synchronized (playbackThread) {
			stopRequest = false;
			int numSamplesToWrite = stimulusData.length;
			Log.d(TAG,"Starting play loop: " + numSamplesToWrite + " samples to write.");
			for (numSamplesPlayed=0; numSamplesPlayed<numSamplesToWrite;) {
				if (stopRequest) {	//TODO: should this be done via an interrupt?
					//TODO: message successful stop, cleanup?
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
		
		synchronized (playbackThread) {
			if (player!=null) player.release();
			
			//check input stimulus
			numSamplesPlayed = 0;
			numSamplesToPlay = stimulusData.length;
			
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
		synchronized (recordingThread) {
			if (recorder!=null) recorder.release();
			
			numSamplesRecorded = 0;
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
