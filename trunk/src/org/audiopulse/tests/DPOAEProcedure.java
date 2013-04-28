package org.audiopulse.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.TEOAEKempAnalyzer;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.utilities.APAnnotations.UnderConstruction;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

import android.os.Bundle;
import android.util.Log;

public class DPOAEProcedure extends TestProcedure{

	private final String TAG = "DPOAEProcedure";
	private Bundle data;
	private final double stimulusDuration=0.5;//stimulus duration in seconds
	private short[] results;
	private HashMap<String, Double> DPGRAM;
	private HashSet<String> fileNames=new HashSet<String>();
	private HashMap<String,String> fileNamestoDataMap=new HashMap<String,String>();

	public DPOAEProcedure(TestActivity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		
		//Loop through all the test frequencies, generating stimulus and collecting the results
		ArrayList<Double> testFrequencies=new ArrayList<Double>();
		ArrayList<Short []> RESULTS = new ArrayList<Short []>();
		testFrequencies.add((double) 2000);
		testFrequencies.add((double) 3000);
		testFrequencies.add((double) 4000);
		clearLog();
		
		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		Log.v(TAG,"Starting DPOAE Recording- generating stimuli at Fs = " + super.playbackSampleFrequency);
		
		for (Double thisFrequency : testFrequencies){
			sendMessage(TestActivity.Messages.PROGRESS);
			logToUI("Running DPOAE frequency: " + thisFrequency + " kHz");
			
			double[][] probe = Signals.dpoaeGorgaMethod(super.playbackSampleFrequency, 
					stimulusDuration);
			Log.v(TAG,"setting probe old level probe[0]=" + probe[0]);
			probe[0] = hardware.setOutputLevel(probe[0], Signals.dpoaeGorgaAmplitude());
			probe[1] = hardware.setOutputLevel(probe[1], Signals.dpoaeGorgaAmplitude());
			Log.v(TAG," probe new level probe[0]=" + probe[0]);
			testIO.setPlaybackAndRecording(AudioSignal.convertStereoToShort(probe));
			double stTime= System.currentTimeMillis();
			results = testIO.acquire();
			double ndTime= System.currentTimeMillis();
			Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000 + " secs" );

			File file= AudioPulseFileWriter.generateFileName("DPOAE","");
			fileNames.add(file.getAbsolutePath());
			fileNamestoDataMap.put(file.getAbsolutePath(),
					AudioPulseDataAnalyzer.RAWDATA_CLICK);
			sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true because we delegate writing of file to another thread...

			try {
				DPGRAM = analyzeResults(results,super.recordingSampleFrequency);
			} catch (Exception e) {
				Log.v(TAG,"Could not generate analysis for results!!" + e.getMessage());
				e.printStackTrace();
			}

			//TODO: Send data back to Activity, the final saving of result will be done when the Activity returns from plotting the processed
			//data and the user accepts the results. 
			data=new Bundle();
			data.putSerializable(AudioPulseDataAnalyzer.Results_MAP,DPGRAM);
			data.putSerializable(AudioPulseDataAnalyzer.RAWDATA_CLICK,results);

			//The file passed here is not used in the analysis (ie not opened and read)!!
			//It is used when the analysis gets accepted by the user: the app packages
			//the file with stored the data for transmission with timestamp on the file name
			data.putSerializable(AudioPulseDataAnalyzer.MetaData_RawFileNames,fileNames);
			data.putSerializable(AudioPulseDataAnalyzer.FileNameRawData_MAP,fileNamestoDataMap);
		}
		
		Log.v(TAG,"Sending analyzed data to activity");
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE,data);
		Log.v(TAG,"donew with " + TAG);
	}

	private HashMap<String, Double> analyzeResults(short[] data, double Fs) throws Exception {
		int epochTime=512; //Number of sample in which to break the FFT analysis
		Log.v(TAG,"data.length= " + data.length);	
		AudioPulseDataAnalyzer teoaeAnalysis=new TEOAEKempAnalyzer(data,Fs,epochTime);
		return teoaeAnalysis.call();

	}

}
