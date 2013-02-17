package org.audiopulse.tests;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.Signals;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class DPOAECalibrationProcedure extends TestProcedure{

	private final String TAG = "DPOAECalibration";
	
	private double f1 = 1000, f2 = 2000;
	private double fdp = 3000;
	private double duration = 0.5;
	
	private AcousticConverter converter = new AcousticConverter();
	
	public DPOAECalibrationProcedure(TestActivity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		//Since (if) we can't synchronize input & output samples, we must calibrate timing
		//ourselves by looking for the onset and offset of the recorded test signal
		
		//for now, let's just calibrate amplitude and forget about phase.
		double[] tone;
		double A0 = 65;		//test amplitude
		double A;			//rcvd amplitude
		
		//1. For f1, f2, dp; generate test tone
		//2. Prepare media playback & recording
		//3. Play & record
		//4. Look for onset & offset in recording
		//5. Determine level between onset & offset
		//6. Save result.
		
		tone = Signals.tone(playbackSampleFrequency, f1, duration);
		tone = converter.setOutputLevel(tone, A0);
		A = calibrateTone(tone);
		logToUI("f1 gain: " + String.format("%.1f dB",A-A0));

		tone = Signals.tone(playbackSampleFrequency, f2, duration);
		tone = converter.setOutputLevel(tone, A0);
		A = calibrateTone(tone);
		logToUI("f2 gain: " + String.format("%.1f dB",A-A0));

		tone = Signals.tone(playbackSampleFrequency, fdp, duration);
		tone = converter.setOutputLevel(tone, A0);
		A = calibrateTone(tone);
		logToUI("dp gain: " + String.format("%.1f dB",A-A0));

	}
	
	private double calibrateTone(double[] tone) {
		testIO.setPlaybackAndRecording(playbackSampleFrequency, AudioSignal.convertToStereo(tone), 
				recordingSampleFrequency, 100, 100);
		testIO.start();
		double[] x = testIO.getResult();
		double A = converter.getInputLevel(x);
		return A;
	}
	
}
