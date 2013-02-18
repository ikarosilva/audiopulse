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
	private volatile boolean recordingCompleted;
	private volatile boolean playbackCompleted;
	
	private int playbackSampleRate;
	private int recordingSampleRate;
	private int playerBufferLength = 4096;			//length of buffer to write in one chucnk
	private int recorderBufferLength = 4096;		//length of recording buffer
	private int recorderReadLength = 512;			//# samples to read at a time from recording buffer
	
	private int numSamplesToPlay;					//total # samples in playback
	private int playerMode;						//MODE_STATIC or MODE_STREAM
	private int numSamplesToRecord;				//total # samples to record
	private int preroll;						//# samples between record start and play start
	private int postroll;						//# samples between play end and record end
	private volatile int numPlayedSamples;				//# samples played so far
	private volatile int numRecordedSamples;				//# sample recorded so far
	
	private short[] recordedData;
	private double[] recordedAudio;
	private short[] stimulusData;
	
	private volatile boolean stopRequest;
	private volatile boolean stopPlayback;
	
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
			initializePlayer();			
		}
		synchronized (recordingThread) {
			this.recordingEnabled = true;
			this.recordingCompleted = false;
			this.recordingSampleRate = recordingSampleFreq;
			this.numSamplesToRecord = preroll + numSamplesToPlay + postroll;
			
			initializeRecorder();
			
			//define listener for recorder that will trigger playback after preroll
			recordListener = new AudioRecord.OnRecordPositionUpdateListener() {
				public void onMarkerReached(AudioRecord recorder) {
					Log.v("recordLoop","Notification marker reached!" );
					if (playbackEnabled) {
						playbackThread.start();
					} 
				}
				public void onPeriodicNotification(AudioRecord recorder) {
					Log.v("recordLoop","Notification period marker reached!" );						
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
	
	

//	//determine how to trigger playback depending on player mode
//	private void triggerPlayback() {
//		if (playerMode==AudioTrack.MODE_STATIC){
//			player.play();
//			playbackCompleted = true; //FIXME
//		} else { //MODE_STREAM
//			playbackThread.start();
//		}
//	}

	private void recordLoop() {
		synchronized (recordingThread) {
			stopRequest = false;
			recorder.startRecording();
			//recording loop
			for (int n=0; n<numSamplesToRecord; ) {
				if (stopRequest) {	//TODO: should this be done via an interrupt?
					//TODO: message successful stop, cleanup?
					break;
				}
				
				int remainingSamples = numSamplesToRecord - n;
				int requestSize=(recorderReadLength<=remainingSamples) ? recorderReadLength : remainingSamples;
				int nRead=recorder.read(recordedData,n,requestSize);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					//TODO: send a useful message to main activity informing of failure
				}
				n += nRead;
				
			}
			recorder.stop();
			Log.d(TAG,"Done recordingLoop");
			recordedAudio = AudioSignal.convertMonoToDouble(recordedData);
			recordingCompleted = true;
			doneLoop();
		}
	}
	
	private void playbackLoop() {
		synchronized (playbackThread) {
			stopPlayback = false;
			player.play();
			for (int n=0; n<numSamplesToPlay;) {
				int remainingSamples = numSamplesToPlay - n;
				int writeSize=(playerBufferLength<=remainingSamples) ? playerBufferLength : remainingSamples;
				int nWritten = player.write(stimulusData, n, writeSize);
				if (nWritten==AudioTrack.ERROR_BAD_VALUE || nWritten==AudioTrack.ERROR_INVALID_OPERATION) {
					Log.e(TAG, "Audio write failed: " + nWritten);
					//TODO: send a useful message to main activity informing of failure
				}
				n+= nWritten;
			}
			player.pause();
			Log.d(TAG,"Done playbackLoop");
			playbackCompleted = true;
			doneLoop();
		}
	}
	
	//If all tasks are done, notify anyone waiting on this PlayRecordManager
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
		Log.d(TAG,"Playback " + 
				(playbackEnabled?"enabled, ":"disabled, ") +
				(playbackCompleted?"complete.":"not complete."));
		Log.d(TAG,"Recording " + 
				(recordingEnabled?"enabled, ":"disabled, ") +
				(recordingCompleted?"complete.":"not complete."));
		return ((!playbackEnabled || playbackCompleted) &&
				(!recordingEnabled || recordingCompleted));
	}
	
	//prepare the AudioPlayer with user-specified parameters
	private void initializePlayer() {
		
		synchronized (playbackThread) {
			if (player!=null) player.release();
			
			//check input stimulus
			numPlayedSamples = 0;
			numSamplesToPlay = stimulusData.length;
			
			//set up AudioTrack object (interface to playback hardware)
			playerMode = AudioTrack.MODE_STREAM;	//TODO: MODE_STATIC? Google bug?
			player = new AudioTrack(AudioManager.STREAM_MUSIC,
					  playbackSampleRate,
					  AudioFormat.CHANNEL_OUT_STEREO,
					  AudioFormat.ENCODING_PCM_16BIT,
					  numSamplesToPlay*2,		//*2 to convert to bytes
					  playerMode);
			int nRead = player.write(stimulusData,0,stimulusData.length);
			if (nRead == AudioTrack.ERROR_BAD_VALUE
					|| nRead == AudioTrack.ERROR_INVALID_OPERATION) {
				//TODO: figure it out
			}
			
			//set up Thread that will handle playback loop in MODE_STREAM
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
			
			numRecordedSamples = 0;
			recordedData = new short[numSamplesToRecord];
			
			//set up AudioRecord object (interface to recording hardware)
			int minBuffer = AudioRecord.getMinBufferSize(
					recordingSampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_STEREO,
					AudioFormat.ENCODING_PCM_16BIT
					);
			int bufferSizeInBytes = 2 * recorderBufferLength;
			if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					recordingSampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_STEREO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes);
			
			//set up thread that will run recording loop
			recordingThread = new Thread( new Runnable() {
				public void run() {
					recordLoop();
				}
			}, "RecordingThread");
		}
	}
		
}
