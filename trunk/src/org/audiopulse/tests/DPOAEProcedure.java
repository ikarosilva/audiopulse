package org.audiopulse.tests;

import java.util.LinkedList;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.hardware.MobilePhone;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

//TODO: put full DPOAE functionality here, fix things as needed
public class DPOAEProcedure extends TestProcedure {
	private LinkedList<DPOAEParameters> testList = new LinkedList<DPOAEProcedure.DPOAEParameters>();
	
	public DPOAEProcedure(TestActivity parent) {
		super(parent);
		TAG = "DPOAEProcedure";
		testList.add(BiologicParameters.F2k);
		//testList.add(BiologicParameters.F3k);
		//testList.add(BiologicParameters.F4k);
	}

	@Override
	public void run() {
		clearLog();
		
		//test:
		calibrateChrip(4000);
		
		
		int numTests = testList.size();
		CalibrationParameters[] cal = new CalibrationParameters[numTests];
		for (int test=0; test<numTests; test++) {
			DPOAEParameters params = testList.get(test);
			cal[test] = calibrate(params);
		}
		
		double[][] results = new double[numTests][];
		for (int test=0; test<numTests; test++) {
			DPOAEParameters params = testList.poll();
			logToUI("Running " + params.toString());
			double[][] probe = params.createStimulus(playbackSampleFrequency, hardware);
			testIO.setPlaybackAndRecording(probe);
			results[test] = testIO.acquire();
		}
		//TODO: analyze results, store results & analysis
	}
	
	//calibrate one set of DPOAE tones (f1,f2,dp)
	private CalibrationParameters calibrate(DPOAEParameters params) {
		double[] tone;
		//1. For f1, f2, dp: generate test tone
		//2. Prepare media playback & recording
		//3. Play & record
		//4. Look for onset & offset in recording
		//5. Determine level between onset & offset
		//6. Save result.
		
		double level = 65;		//linear calibration level (dB SPL)
		double duration = 0.4;	//calibration tone duration (s)

		//get in-ear gain for f1 tone
		tone = Signals.tone(playbackSampleFrequency, params.f1, duration);
		tone = hardware.setOutputLevel(tone, level);
		double[][] f1Tone = AudioSignal.monoToStereoLeft(tone);
		double f1Gain = calibrateTone(f1Tone, level);
		logToUI("f1 ("+ String.format("%.0f Hz",params.f1) +") gain: "
				+ String.format("%.1f dB",f1Gain));
		
		//get in-ear gain for f2 tone
		tone = Signals.tone(playbackSampleFrequency, params.f2, duration);
		tone = hardware.setOutputLevel(tone, level);
		double[][] f2Tone = AudioSignal.monoToStereoRight(tone);
		double f2Gain = calibrateTone(f2Tone, level);
		logToUI("f2 ("+ String.format("%.0f Hz",params.f2) +") gain: "
				+ String.format("%.1f dB",f2Gain));
		
		//get in-ear gain for dp tone
		tone = Signals.tone(playbackSampleFrequency, params.dpFreq, duration);
		tone = hardware.setOutputLevel(tone, level);
		double[][] dpTone = AudioSignal.convertToStereo(tone);
		double dpGain = calibrateTone(dpTone, level);
		logToUI("DP ("+ String.format("%.0f Hz",params.dpFreq) +") gain: "
				+ String.format("%.1f dB",dpGain));
		
		return new CalibrationParameters(f1Gain, f2Gain, dpGain);

	}
	
	//get in-ear gain relative to given, expected level
	private double calibrateTone(double[][] signal, double spl) {
		testIO.setPlaybackAndRecording(signal);
		double[] input = testIO.acquire();
		double splIn = hardware.getInputLevel(input);
		return splIn - spl;		
	}
	
	//data for calibration results for a single test frequency
	public static class CalibrationParameters {
		public final double f1Gain, f2Gain, dpGain;
		public CalibrationParameters(double f1Gain, double f2Gain, double dpGain) {
			this.f1Gain = f1Gain;
			this.f2Gain = f2Gain;
			this.dpGain = dpGain;
		}
		//TODO: delay / phase if needed (are they?)
	}
	
	//DPOAE parameters from the Bio-Logic OAE Report (2012)
	public static final class BiologicParameters {
		private static final String protocol = "Biologic";
		private static final double duration = 0.5;
		
		public static final DPOAEParameters F2k =
			new DPOAEParameters(protocol,2000,duration,1641,2016,64.4,53.4);
		public static final DPOAEParameters F3k =
			new DPOAEParameters(protocol,3000,duration,2297,2813,64.6,55.1);
		public static final DPOAEParameters F4k =
			new DPOAEParameters(protocol,4000,duration,3281,3984,64.8,55.6);
		public static final DPOAEParameters F6k =
			new DPOAEParameters(protocol,6000,duration,4594,5625,64.8,56.6);
		public static final DPOAEParameters F8k =
			new DPOAEParameters(protocol,8000,duration,6516,7969,64.8,54.9);
	}
	
	//"Handbook of Otocoustic Emissions" J. Hall, Singular Publishing Group Copyright 2000
	public static final class HOAEParmaeters {
		private static final String protocol = "HOAE";
		private static final double duration = 0.5;
		private static final DPOAEParameters createHOAEParams(int freq) {
			return new DPOAEParameters(protocol,freq,duration,freq/1.2,freq,65,50);
		}
		
		public static final DPOAEParameters F2k = createHOAEParams(2000);
		public static final DPOAEParameters F3k = createHOAEParams(3000);
		public static final DPOAEParameters F4k = createHOAEParams(4000);
		public static final DPOAEParameters F6k = createHOAEParams(6000);
		public static final DPOAEParameters F8k = createHOAEParams(8000);
	}
	
	//one object to contain all necessary info to analyze, calibrate, graph
	//e.g. DPOAEParameters p = DPOAEParameters.BiologicParameters.F2k
	public static class DPOAEParameters {
		public final double testFrequency;		//Frequency label (e.g. 2000) (Hz)
		public final double durationInSeconds;	//DP tones duration (s)
		public final double f1, f2;				//test tone frequencies (Hz)
		public final double level1, level2;		//test tone levels (dB SPL)
		public final double dpFreq;				//expected DP frequency (Hz)
		public final String protocol;
		
		//private to force client to use factory methods
		//e.g. DPOAEParameters params = DPOAEParameters.getBiologicParameters(DPOAEParameters.Tests.F2k)
		private DPOAEParameters(String protocol, int testFrequency, double duration,
				double f1, double f2, double level1, double level2) {
			this.protocol = protocol;
			this.durationInSeconds = duration;
			this.testFrequency = testFrequency;
			this.f1 = f1;
			this.f2 = f2;
			this.level1 = level1;
			this.level2 = level2;
			this.dpFreq = 2*f1-f2;;
		}
		
		//create stimulus from parameters
		public double[][] createStimulus(double sampleFrequency, AcousticConverter hardware) {
			//create {f1, f2} tones in {left, right} channel of stereo stimulus
			double[][] stimulus = new double[2][];
			stimulus[0] = createF1Tone(sampleFrequency,hardware);
			stimulus[1] = createF2Tone(sampleFrequency,hardware);
			return stimulus;
		}
		public double[] createF1Tone(double sampleFrequency, AcousticConverter hardware) {
			double[] tone;
			tone = Signals.tone(sampleFrequency, f1, durationInSeconds);
			tone = hardware.setOutputLevel(tone, level1);
			tone = Signals.fade(sampleFrequency, 0.01, tone);
			//TODO: is fade uncalled for?
			return tone;
		}
		public double[] createF2Tone(double sampleFrequency, AcousticConverter hardware) {
			double[] tone;
			tone = Signals.tone(sampleFrequency, f2, durationInSeconds);
			tone = hardware.setOutputLevel(tone, level2);
			tone = Signals.fade(sampleFrequency, 0.01, tone);
			return tone;
		}		
		
		@Override
		public String toString() {
			return protocol + ", " + testFrequency + " Hz";
		}
		
	}

}
