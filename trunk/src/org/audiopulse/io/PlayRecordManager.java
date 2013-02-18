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
	private int playerBufferLength = 512;
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
	
	private volatile boolean stopRecording;
	private volatile boolean stopPlayback;
	
	private Thread recordingThread = new Thread();
	private Thread playbackThread = new Thread();
	
	public AudioRecord.OnRecordPositionUpdateListener recordListener;
	
	//set to playback only, specify stimulus
	public void setPlaybackOnly(int playbackSampleFreq, double[][] stimulus) {
		synchronized (playbackThread) {
			playbackEnabled = true;
			playbackCompleted = false;
			this.playbackSampleRate = playbackSampleFreq;		
			initializePlayer(stimulus);
		}
		synchronized (recordingThread) {
			recordingEnabled = false;
		}
		
	}
	
	//set to playback and record, specify stimulus and recording pre/post roll
	public void setPlaybackAndRecording(
			int playbackSampleFreq, double[][] stimulus,
			int recordingSampleFreq, int prerollInMillis, int postrollInMillis) {

		//figure out time intervals in samples
		preroll = prerollInMillis * recordingSampleFreq / 1000;
		postroll = postrollInMillis * recordingSampleFreq / 1000;

		synchronized (playbackThread) {
			playbackEnabled = true;
			playbackCompleted = false;
			this.playbackSampleRate = playbackSampleFreq;
			initializePlayer(stimulus, prerollInMillis);			
		}
		synchronized (recordingThread) {
			recordingEnabled = true;
			recordingCompleted = false;
			this.recordingSampleRate = recordingSampleFreq;
			
			numSamplesToRecord = preroll + numSamplesToPlay + postroll;
			initializeRecorder(numSamplesToRecord);
			
			//FIXME - this callback is only getting evaluated after the
			// activity is it queuing on a busy thread?
			//set up recorder to trigger playback after preroll
			recordListener = new AudioRecord.OnRecordPositionUpdateListener() {
				public void onMarkerReached(AudioRecord recorder) {
					Log.v("recordLoop","Notification marker reached!" );
					if (playbackEnabled) {
						triggerPlayback();
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
			playbackEnabled = false;
		}
		synchronized (recordingThread) {
			recordingEnabled = true;
			recordingCompleted = false;
			this.recordingSampleRate = recordingSampleFrequency;
			
			//determine recording time in samples
			numSamplesToRecord = recordTimeInMillis * recordingSampleFrequency / 1000;
			initializeRecorder(numSamplesToRecord);
		}
	}
	
	//start playback and/or recording
	public synchronized void start() {
		if (!recordingEnabled && !playbackEnabled) {
			Log.w(TAG,"No recording or playback set, doing nothing!");
			return;
		}
		
//		if (recordingEnabled) {
//			//start recording in new thread.
//			//if playback is enabled, recorder will trigger it.
//			recordingThread.start();
//		} else {
//			//no recorder to trigger playback, so start playback ourselves
//			triggerPlayback();
//		}

		//FIXME - can't get recorder callback to be called in time, why not?
		//trying to use that as the way to trigger the playback thread.
		//maybe it's just as good to do this with a built-in delay to playbackThread

		recordingThread.start();
		triggerPlayback();
		
		//FIXME - how do we set playbackComplete using MODE_STATIC?
		//maybe use a callback, but that's not working out so well for the recorder.
		//TODO - do we want to wait on this thread, or just trigger and exit here,
		//letting the TestProcedure decide how to wait
		// -- I think it's a good idea to limit what can be done, and not risk
		//    concurrency issues or CPU limitations during a testIO run, so
		//    maybe waiting in this thread is good
		//wait until IO completes or interrupted
		try {
			while (!isIoComplete()) {
				Log.d(TAG,"Waiting for IO completion...");
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.w(TAG,"IO interrupted");
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
			}
			return recordedAudio;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	

	//determine how to trigger playback depending on player mode
	private void triggerPlayback() {
		if (playerMode==AudioTrack.MODE_STATIC){
			player.play();
			playbackCompleted = true; //FIXME
		} else { //MODE_STREAM
			playbackThread.start();
		}
	}

	private void recordLoop() {
		synchronized (recordingThread) {
			stopRecording = false;
			recorder.startRecording();
			//recording loop
			for (int n=0; n<numSamplesToRecord; ) {
				if (stopRecording) {
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
			recordingCompleted = true;
			recordedAudio = AudioSignal.convertMonoToDouble(recordedData);
			doneLoop();
		}
	}
	
	private void playbackLoop() {
		synchronized (playbackThread) {
			stopPlayback = false;
			player.play();
			for (int n=0; n>numSamplesToPlay;) {
				//TODO: implement this for MODE_STREAM
				n+= numSamplesToPlay;		//TODO: remove
			}
			player.stop();
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
		return ((!playbackEnabled || playbackCompleted) &&
				(!recordingEnabled || recordingCompleted));
	}
	
	//prepare the AudioPlayer with user-specified parameters
	private void initializePlayer(double[][] stimulus) {
		initializePlayer(stimulus, 0);
	} //TODO: clean this up
	private void initializePlayer(double[][] stimulus, final int prerollInMillis) {
		
		//create a local copy of stimulus before potentially waiting for lock
		short[] writeData = AudioSignal.convertStereoToShort(stimulus);
		
		synchronized (playbackThread) {
			if (player!=null) player.release();
			
			//check input stimulus
			numPlayedSamples = 0;
			numSamplesToPlay = stimulus[0].length;
			if (stimulus[1].length != numSamplesToPlay)
				throw new IllegalArgumentException("Invalid stereo stimulus: buffers must be the same length.");
			
			//set up AudioTrack object (interface to playback hardware)
			playerMode = AudioTrack.MODE_STATIC;	//TODO: determine this at runtime
			//TODO: allow mode_stream, implement playback buffer loop
			player = new AudioTrack(AudioManager.STREAM_MUSIC,
					  playbackSampleRate,
					  AudioFormat.CHANNEL_OUT_STEREO,
					  AudioFormat.ENCODING_PCM_16BIT,
					  numSamplesToPlay*4,
					  playerMode);
			int nRead = player.write(writeData,0,writeData.length);
			if (nRead == AudioTrack.ERROR_BAD_VALUE
					|| nRead == AudioTrack.ERROR_INVALID_OPERATION) {
				//TODO: figure it out
			}
			
			//set up Thread that will handle playback loop in MODE_STREAM
			//TODO: do this only in MODE_STREAM
			playbackThread = new Thread( new Runnable() {
				public void run() {
					try {
						Thread.sleep(prerollInMillis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					playbackLoop();
				}
			}, "PlaybackThread");
		}
	}
	
	//prepare the AudioRecorder with user-specified parameters
	private void initializeRecorder(int numSamplesToRecord) {
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
