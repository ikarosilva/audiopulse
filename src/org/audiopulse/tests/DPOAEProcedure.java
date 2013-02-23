package org.audiopulse.tests;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.activities.DPOAEActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.hardware.MobilePhone;
import org.audiopulse.utilities.DPOAESignal;
import org.audiopulse.utilities.PeriodicSeries;
import org.audiopulse.utilities.Signals;
import org.audiopulse.utilities.DPOAESignal.protocolBioLogic;
import org.audiopulse.utilities.DPOAESignal.protocolHOAE;

//TODO: put full DPOAE functionality here, fix things as needed
public class DPOAEProcedure extends TestProcedure {

	public DPOAEProcedure(TestActivity parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	//one object to contain all necessary info to analyze, calibrate, graph
	//use static facotry methods:
	//e.g. DPOAEParameters p = DPOAEParameters.createBiologicParameters(DPOAEParameters.Tests.F2k)
	public static class DPOAEParameters {
		public final double testFrequency;		//Frequency label (e.g. 2000) (Hz)
		public final double f1, f2;				//test tone frequencies (Hz)
		public final double level1, level2;		//test tone levels (dB SPL)
		public final double dpFreq;				//expected DP frequency (Hz)
		public final String protocol;
		
		//private to force client to use factory methods
		//e.g. DPOAEParameters params = DPOAEParameters.getBiologicParameters(DPOAEParameters.Tests.F2k)
		private DPOAEParameters(String protocol, int testFrequency,
				double f1, double f2, double level1, double level2) {
			this.protocol = protocol;
			this.testFrequency = testFrequency;
			this.f1 = f1;
			this.f2 = f2;
			this.level1 = level1;
			this.level2 = level2;
			this.dpFreq = 2*f1-f2;;
		}
		
		//create stimulus from parameters
		public double[][] createStimulus(double sampleFrequency, AcousticConverter hardware, double durationInSeconds) {
			//create {f1, f2} tones in {left, right} channel of stereo stimulus
			double[][] stimulus = new double[2][];
			stimulus[0] = createF1Tone(sampleFrequency,hardware,durationInSeconds);
			stimulus[1] = createF2Tone(sampleFrequency,hardware,durationInSeconds);
			return stimulus;
		}
		public double[] createF1Tone(double sampleFrequency, AcousticConverter hardware, double durationInSeconds) {
			double[] tone;
			tone = Signals.tone(sampleFrequency, f1, durationInSeconds);
			tone = hardware.setOutputLevel(tone, level1);
			tone = Signals.fade(sampleFrequency, 0.01, tone);
			//TODO: is fade uncalled for?
			return tone;
		}
		public double[] createF2Tone(double sampleFrequency, AcousticConverter hardware, double durationInSeconds) {
			double[] tone;
			tone = Signals.tone(sampleFrequency, f2, durationInSeconds);
			tone = hardware.setOutputLevel(tone, level2);
			tone = Signals.fade(sampleFrequency, 0.01, tone);
			return tone;
		}
		
		
		//If future protocols define other test frequencies, we can define 
		//different enums for each test protocol to ensure that incorrect
		//frequencies are caught before compiling
		//e.g., enum BiologicTests, enum HOAETests
		public static enum Tests {
			F2k(2000),
			F3k(3000),
			F4k(4000),
			F6k(6000),
			F8k(8000);
			public final int frequency;
			private Tests(int f) {
				frequency = f;
			}
		}
		
		//DPOAE parameters from the Bio-Logic OAE Report (2012)
		public static DPOAEParameters createBiologicParameters(Tests test){
			final String protocol = "Biologic";
			switch (test) {
			case F2k:
				return new DPOAEParameters(protocol,test.frequency,6516,7969,64.8,54.9);
			case F3k: 
				return new DPOAEParameters(protocol,test.frequency,2297,2813,64.6,55.1);
			case F4k:
				return new DPOAEParameters(protocol,test.frequency,3281,3984,64.8,55.6);
			case F6k:
				return new DPOAEParameters(protocol,test.frequency,4594,5625,64.8,56.6);
			case F8k:
				return new DPOAEParameters(protocol,test.frequency,6516,7969,64.8,54.9);
			}
			return null;
		}
		
		//"Handbook of Otocoustic Emissions" J. Hall, Singular Publishing Group Copyright 2000
		public static DPOAEParameters createHOAEParameters(Tests test){
			final String protocol = "HOAE";
			final double f2 = (double)test.frequency;
			final double f1=f2/1.2;
			final double spl1 = 65; 
			final double spl2 = 50;

			return new DPOAEParameters(protocol,test.frequency,f1,f2,spl1,spl2);

		}
		
		/*
		//DPOAE parameters from the Bio-Logic OAE Report (2012)
		public static enum ProtocolBioLogic {
			//{test freq, f1, f2, spl1, spl2}
			F8k(8000,6516,7969,64.8,54.9),
			F6k(6000,4594,5625,64.8,56.6),
			F4k(4000,3281,3984,64.8,55.6),
			F3k(3000,2297,2813,64.6,55.1),
			F2k(2000,1641,2016,64.4,53.4);
			public final DPOAEParameters parameters;
			
			ProtocolBioLogic(int frequency, double f1,double f2, double spl1, double spl2) {
				parameters = new DPOAEParameters("Biologic",frequency,f1,f2,spl1,spl2);
			}
		}
		
		//"Handbook of Otocoustic Emissions" J. Hall, Singular Publishing Group Copyright 2000
		public static enum ProtocolHOAE {
			F8k(8000),
			F6k(6000),
			F4k(4000),
			F3k(3000),
			F2k(2000);
			DPOAEParameters parameters;
			
			ProtocolHOAE(int frequency) {
				final double f2 = (double)frequency;
				final double f1=f2/1.2;
				final double spl1 = 65; 
				final double spl2 = 50;
				parameters = new DPOAEParameters("Biologic",frequency,f1,f2,spl1,spl2);
			}
		}
	*/
	}

}
