package org.audiopulse.tests;

import java.io.File;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.utilities.APAnnotations.UnderConstruction;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

import android.util.Log;

@UnderConstruction(owner="Ikaro Silva")
public class TEOAEProcedure extends TestProcedure{

	private final String TAG = "TEOAECalibration";

	private final double stimulusDuration=1;//stimulus duration
	public TEOAEProcedure(TestActivity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		short[] results;
		double[] DPGRAM;
		
		clearLog();
		sendMessage(TestActivity.Messages.PROGRESS);
		
		double sampleFrequency = 44100;
		logToUI("Running TEOAE");
		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		Log.v(TAG,"Generating stimulus");
		double[] probe = Signals.clickKempMethod(sampleFrequency, 
				stimulusDuration);
		Log.v(TAG,"setting probe old level probe[0]=" + probe[0]);
		probe = hardware.setOutputLevel(probe, 55);
		Log.v(TAG," probe new level probe[0]=" + probe[0]);
		testIO.setPlaybackAndRecording(AudioSignal.convertMonoToShort(probe));
		double stTime= System.currentTimeMillis();
		results = testIO.acquire();
		double ndTime= System.currentTimeMillis();
		Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000 + " secs" );
		
		File file= AudioPulseFileWriter.generateFileName("TEOAE","");
		logToUI("Saving file at:" + file.getAbsolutePath());
		AudioPulseFileWriter writer= new AudioPulseFileWriter
				(file,results);
		Log.v(TAG,"saving raw data" );
		writer.start();
		
        sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true becauase we delegate writing of file to another thread...
		
        DPGRAM=analyzeResults();
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE);
		
		//TODO: Send data back to Activity, the final saving of result will be done when the Activity returns from plotting the processed
		//data and the user accepts the results. So we need to delete the binary files if the user decides to reject/redo the test
		sendDPGRAMData(DPGRAM);
		sendMessage(TestActivity.Messages.PROCEDURE_COMPLETE);
	}
	
	private double[] analyzeResults() {
		return null;
		//TODO: define a TEOAEAnalysis call that operates on input arguments
		//rather than a file location, call it from here
	}

}
