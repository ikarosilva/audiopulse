package org.audiopulse.tests;

import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.Signals;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class DPOAECalibration extends TestProcedure{

	private final String TAG = "DPOAECalibration";
	
	private double f1 = 1000, f2 = 2000;
	private int fs = 44100;
	private double duration = 0.5;
	
	public DPOAECalibration(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}
	public void run() {
		//Since (if) we can't synchronize input & output samples, we must calibrate timing
		//ourselves by looking for the onset and offset of the recorded test signal
		
		//for now, let's just calibrate amplitude and forget about phase.
		
		//1. For f1, f2; generate test tone
		//2. Prepare media playback & recording
		//3. Play & record
		//4. Look for onset & offset in recording
		//5. Determine level between onset & offset
		//6. Save result.
		double[] tone1 = Signals.tone(fs, f1, duration);
		double[] tone2 = Signals.tone(fs, f2, duration);
		
		double A1 = calibrateTone(tone1);
		informUI("f1 amplitude: " + A1);
		double A2 = calibrateTone(tone2);
		informUI("f2 amplitude: " + A2);
	}
	
	private double calibrateTone(double[] tone) {
		testIO.setStimulus(AudioSignal.convertToStereo(tone));
		double[] x = testIO.playAndRecord();
		double A = SignalProcessing.rms(x);
		return A;
	}
	
	public double getF1() {
		return f1;
	}
	public double getF2() {
		return f2;
	}

	public void setF1(double f) {
		this.f1 = f;
	}
	public void setF2(double f) {
		this.f2 = f;
	}

	public int getFs() {
		return fs;
	}

	public void setFs(int fs) {
		this.fs = fs;
	}
}
