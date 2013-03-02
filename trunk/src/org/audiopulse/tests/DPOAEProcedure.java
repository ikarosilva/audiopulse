package org.audiopulse.tests;

import java.util.LinkedList;
import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

//TODO: put full DPOAE functionality here, fix things as needed
public class DPOAEProcedure extends TestProcedure {
	private LinkedList<DPOAEParameters> testList = new LinkedList<DPOAEProcedure.DPOAEParameters>();
	
	public DPOAEProcedure(TestActivity parent) {
		super(parent);
		TAG = "DPOAEProcedure";
		testList.add(SOAEParameters.spont);
		testList.add(BiologicParameters.F2k);
		testList.add(BiologicParameters.F3k);
		testList.add(BiologicParameters.F4k);
	}

	@Override
	public void run() {
		clearLog();
		
		//test:
		calibrateChrip(4000);
		
		
		int numTests = testList.size();

		double[][] recordedAudio = new double[numTests][];
		for (int test=0; test<numTests; test++) {
			DPOAEParameters params = testList.poll();
			logToUI("Running " + params.toString());
			double[][] probe = params.createStimulus(playbackSampleFrequency, hardware);
			testIO.setPlaybackAndRecording(AudioSignal.convertStereoToShort(probe));
			short[] data = testIO.acquire();
			recordedAudio[test] = AudioSignal.convertMonoToDouble(data);
			sendMessage(TestActivity.Messages.PROGRESS);
			
			//if it doesn't eat up too many resources, we can spawn threads here
			//to do analysis and saving. This way, the UI can update with results
			//as the test runs
			//Set these threads' priority to normal
		}
		sendMessage(TestActivity.Messages.IO_COMPLETE);
		
		analyzeResults();
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE);
		
		saveResults();
		sendMessage(TestActivity.Messages.PROCEDURE_COMPLETE);
	}

	
	private void analyzeResults() {
		//TODO: define a DPOAEAnalysis call that operates on input arguments
		//rather than a file location, call it from here
	}
	
	private void saveResults() {
		/**** Pseudocode outline for this function:
		 *	xmlData = generateXMLData()
		 *	zipFile = FileZipper.process(xmlData)
		 *	savefile(zipFile,getExternalStorageDirectory())
		 *	Sana.sendFile(zipFile)
		 * 
		 */
		
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
	
	//Hack the SOAE parameters in here
	public static final class SOAEParameters {
		private static final String protocol = "Spontaneous";
		private static final double duration = 4.3;
		public static final DPOAEParameters spont = new DPOAEParameters(protocol,0,duration,0,0,0,0);
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
		public DPOAEParameters(String protocol, int testFrequency, double duration,
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
			
			//hack for now to allow SOAE recording here
			if (testFrequency==0) {
				int N = (int) (durationInSeconds * sampleFrequency);
				double[][] stimulus = new double[2][N];
				return stimulus;
			}
			
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
