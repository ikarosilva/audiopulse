package org.audiopulse.tests;

import java.io.File;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.tests.DPOAEProcedure.DPOAEParameters;
import org.audiopulse.utilities.APAnnotations.UnderConstruction;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

import android.util.Log;

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

		clearLog();

		double[][] results = new double[1][];
		double sampleFrequency = 16000;
		logToUI("Running TEOAE");
		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		double[][] stimulus = new double[2][];
		Log.v(TAG,"Generating stimulus");
		double[] probe = Signals.clickKempMethod(sampleFrequency, 
				stimulusDuration);
		Log.v(TAG,"setting probe old leve probe[0]=" + probe[0]);
		probe = hardware.setOutputLevel(probe, 50);
		Log.v(TAG," probe new level probe[0]=" + probe[0]);
		testIO.setPlaybackAndRecording(AudioSignal.convertMonoToShort(probe));
		double stTime= System.currentTimeMillis();
		results[1] = testIO.acquire();
		double ndTime= System.currentTimeMillis();
		Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000 + " secs" );
		
		File file= AudioPulseFileWriter.generateFileName("TEOAE","");
		logToUI("Saving file at:" + file.getAbsolutePath());
		AudioPulseFileWriter writer= new AudioPulseFileWriter
				(file,AudioSignal.convertMonoToShort(results[1]));
		Log.v(TAG,"saving file" );
		writer.start();
	}

}
