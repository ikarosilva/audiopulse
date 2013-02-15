package org.audiopulse.tests;

import org.audiopulse.activities.BasicTestActivity;
import org.audiopulse.activities.DPOAEActivity;
import org.audiopulse.hardware.MobilePhone;
import org.audiopulse.utilities.DPOAESignal;
import org.audiopulse.utilities.PeriodicSeries;
import org.audiopulse.utilities.Signals;
import org.audiopulse.utilities.DPOAESignal.protocolBioLogic;
import org.audiopulse.utilities.DPOAESignal.protocolHOAE;

//TODO: put full DPOAE functionality here, fix things as needed
public class DPOAEProcedure extends TestProcedure {

	public DPOAEProcedure(BasicTestActivity parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	//create stimulus given desired frequency band (e.g. 2000, 3000, 4000)
	public double[][] createStimulus(int f) {
		//TODO: generate appropriate DPOAE stimulus parameters
		
		//get DPOAE parameters for this frequency
		DPOAEParameters dpp = new DPOAEParameters(Protocol.BIOLOGIC, f);
		double duration = 0.5;
		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		double[][] stimulus = new double[2][];
		stimulus[0] = Signals.tone(playbackSampleFrequency, dpp.f1, duration);
		stimulus[0] = levels.setOutputLevel(stimulus[0], dpp.level1);
		stimulus[1] = Signals.tone(playbackSampleFrequency, dpp.f2, duration);
		stimulus[1] = levels.setOutputLevel(stimulus[1], dpp.level2);
		return stimulus;
	}
	
	public static enum Protocol {
		BIOLOGIC, HOAE
	}

	public class DPOAEParameters {
		public final double f1, f2;
		public final double level1, level2;
		public final double dpFreq;
		
		public DPOAEParameters(Protocol protocol, int f) {
			switch (protocol) {
			case BIOLOGIC:
				//DPOAE parameters from the Bio-Logic OAE Report (2012)
			case HOAE:
				//"Handbook of Otocoustic Emissions" J. Hall, Singular Publishing Group Copyright 2000
			default:
			
			}
			//TODO: do this in the switch
			f1 = 0; f2 = 0;
			level1 = 0; level2 = 0;
			dpFreq = 0;

		}
	}
	

}
