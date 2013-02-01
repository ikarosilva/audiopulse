package org.audiopulse.io;

import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

public class PlayRecordManager{
	private boolean recordEnabled;
	private boolean playbackEnabled;
	private AudioTrack player;
	private AudioRecord recorder;
	private int sampleRate;
	
	private int recordPreRoll = 100;			//approx time in ms between record start and play start
	private int recordPostRoll = 100;			//approx time in ms between play end and record end
	private int recordPollPeriod = 20;			//period in ms for polling record buffer
	private int recordBufferLength = 1000;		//record buffer length in ms
	
	private short[] recordedSamples;
	private int stimulusLength;
	
	private static final String TAG = "PlayRecordManager";
	
	public PlayRecordManager() {
		this(44100);
	}
	public PlayRecordManager(int sampleRate) {
		this.sampleRate = sampleRate;
		this.setPlaybackEnabled(false);
		this.setRecordEnabled(true);
	}
	
	//TODO: confirm player and recorder, as needed, are set up correctly
	public void play() {
		//TODO: listen for player finish? then call player.stop();, maybe player.release(); 
		player.play();
	}
	public double[] record() {
		setPlaybackEnabled(false);
		playRec();
		return AudioSignal.convertMonoToDouble(recordedSamples);
	}
	public double[] playAndRecord() {
		setPlaybackEnabled(true);
		playRec();
		return AudioSignal.convertMonoToDouble(recordedSamples);
	}
	
	//start playback & recording (if enabled)
	private void playRec() {
		if (!playbackEnabled && !recordEnabled) {
			return;
		} else if (playbackEnabled && !recordEnabled) {
			player.play();
			//TODO: suspend until play is done
		} else if (recordEnabled) {
			//determine sample lengths from parameters specified in ms
			int preRollSamples = recordPreRoll * sampleRate / 1000;
			int postRollSamples = recordPostRoll * sampleRate / 1000;
			int recordFrameSize = recordPollPeriod * sampleRate / 1000;
			recordedSamples = new short[stimulusLength + preRollSamples + postRollSamples];
			
			//define playback triggers: when we reach a certain sample, start / stop playback
			int startPlaybackTrigger = preRollSamples;
			boolean playbackTriggered = false;
			int stopPlaybackTrigger = preRollSamples + stimulusLength;
			boolean playbackStopped = false;
			
			//start recording loop: poll record buffer, write data to recordedSamples
			int n = 0;
			recorder.startRecording();
			while(n<recordedSamples.length){ 
				int remainingSamples = recordedSamples.length - n;
				int requestSize=(recordFrameSize<=remainingSamples) ? recordFrameSize: remainingSamples;
				int nRead=recorder.read(recordedSamples,n,requestSize);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					//TODO: send a useful message to main activity informing of failure
				}
				n += nRead;
				
				//determine if we should trigger playback start or stop
				if (playbackEnabled) {
					if (!playbackTriggered && n>=startPlaybackTrigger){
						player.play();
						playbackTriggered = true;
					}
					if (!playbackStopped && n>=stopPlaybackTrigger){
						player.stop();
						playbackStopped = true;
					}
				}
				
			}
	
			recorder.stop();
			initializeRecorder();		//release & re-initialize so we're ready to record again
		}
	}
	
	//define stimulus for playback.
	public int setStimulus(double[][] stimulus) {
		if (player!=null) player.release();
		
		stimulusLength = stimulus[0].length;
		if (stimulus[1].length != stimulusLength)
			throw new IllegalArgumentException("Invalid stereo stimulus: buffers must be the same length.");
		
		short[] writeData = AudioSignal.convertStereoToShort(stimulus);
		
		player = new AudioTrack(AudioManager.STREAM_MUSIC,
				  sampleRate,
				  AudioFormat.CHANNEL_OUT_STEREO,
				  AudioFormat.ENCODING_PCM_16BIT,
				  stimulusLength*4,
				  AudioTrack.MODE_STATIC);

		return player.write(writeData,0,writeData.length);
	}
	
	public void setRecordEnabled(boolean recordEnabled) {
		if (!this.recordEnabled && recordEnabled)
			initializeRecorder();
		this.recordEnabled = recordEnabled;
	}
	public void setPlaybackEnabled(boolean playbackEnabled) {
		this.playbackEnabled = playbackEnabled;
	}
	
	
	private void initializeRecorder() {
		if (recorder!=null) recorder.release();
		
		int minBuffer = AudioRecord.getMinBufferSize(
				sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT
				);
		int bufferSizeInBytes = recordBufferLength * sampleRate / 1000;
		if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufferSizeInBytes);
		//recorder.setPositionNotificationPeriod(recordPollPeriod);
	}
	
}
