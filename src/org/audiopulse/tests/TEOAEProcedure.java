package org.audiopulse.tests;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.utilities.APAnnotations.UnderConstruction;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

@UnderConstruction(owner="Ikaro Silva")
public class TEOAEProcedure extends TestProcedure{

	private final String TAG = "TEOAECalibration";
	
	private final double stimulusDuration=4;//stimulus duration
	
	private AcousticConverter converter = new AcousticConverter();
	
	
	public TEOAEProcedure(TestActivity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		//Since (if) we can't synchronize input & output samples, we must calibrate timing
		//ourselves by looking for the onset and offset of the recorded test signal
		
		//for now, let's just calibrate amplitude and forget about phase.
		double[] click;
		double A0 = 65;		//test amplitude
		String file;			//rcvd amplitude
		
		//1. For f1, f2, dp; generate test tone
		//2. Prepare media playback & recording
		//3. Play & record
		//4. Look for onset & offset in recording
		//5. Determine level between onset & offset
		//6. Save result.
		
		click = Signals.clickKempMethod(playbackSampleFrequency,stimulusDuration);
		click = converter.setOutputLevel(click, A0);
		file = playStimulus(click);
		logToUI("Generated click stimulus of " + stimulusDuration  + " seconds.");
		logToUI("File being saved at:" + file);
	}
	
	private String playStimulus(double[] click) {
		testIO.setPlaybackAndRecording(playbackSampleFrequency, AudioSignal.convertToStereo(click), 
				recordingSampleFrequency, 100, 100);
		testIO.start();
		double[] x = testIO.getResult();
		
		//TODO: save data x to file and return URI
		return null;
	}
	
}
