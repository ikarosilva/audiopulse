package org.audiopulse.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.DPOAEGorgaAnalyzer;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.Signals;

import android.os.Bundle;
import android.util.Log;

public class DPOAEProcedure extends TestProcedure{

	private final String TAG = "DPOAEProcedure";
	private Bundle data;
	private HashSet<String> fileNames=new HashSet<String>();
	private HashMap<String,String> fileNamestoDataMap=new HashMap<String,String>();
	private HashMap<String, Double> DPGRAM= new HashMap<String, Double>();

	public DPOAEProcedure(TestActivity parentActivity) {
		super(parentActivity);
	}

	@Override
	public void run() {

		//Loop through all the test frequencies, generating stimulus and collecting the results
		ArrayList<Double> testFrequencies=new ArrayList<Double>();
		
		//use this to debug by generating a sweep across levels
		boolean sweepTrial=true;
		double f =2000.0;
		Double attStep=10.0;
		Double att=-attStep;
		if(super.testName.contentEquals("TEST DPOAE")){
			testFrequencies.add((double) 2000);
			sweepTrial=false;
		}else{
			/*
			testFrequencies.add((double) 2000);
			testFrequencies.add((double) 3000);
			testFrequencies.add((double) 4000);
			*/
			//Testing SPL sweep to search for distortion
			testFrequencies.add(f);
			testFrequencies.add(f);
			testFrequencies.add(f);
			testFrequencies.add(f);
			testFrequencies.add(f);
			
		}
		clearLog();
		HashMap<String, Double> localDPGRAM = new HashMap<String, Double>();

		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		Log.v(TAG,"Starting DPOAE Recording- generating stimuli at Fs = " + super.playbackSampleFrequency);
		data=new Bundle();
		short[] results;
		short[] stimulus;
		double splLevel=0;
		for (Double thisFrequency : testFrequencies){
			sendMessage(TestActivity.Messages.PROGRESS);
			logToUI("Running DPOAE frequency: " + thisFrequency + " kHz");

			//Stimulus is presented at F2
			att=att+attStep;
			splLevel=Signals.dpoaeGorgaAmplitude(thisFrequency)-att;
			double[][] probe = Signals.dpoaeGorgaMethod(super.playbackSampleFrequency, 
					thisFrequency);
			probe[0] = hardware.setOutputLevel(probe[0],splLevel);
			probe[1] = hardware.setOutputLevel(probe[1],splLevel);
			
			
			stimulus=AudioSignal.convertStereoToShort(probe);
			//short[] stimulus=AudioSignal.convertMonoToShort(
			//		AudioSignal.convertToMono(probe));
			testIO.setPlaybackAndRecording(stimulus);

			double stTime= System.currentTimeMillis();
			results = testIO.acquire();
			double ndTime= System.currentTimeMillis();
			Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000 + " secs" );
			File file=null;
			Log.v("RMS","stimulus spl =" + AcousticConverter.getOutputLevel(stimulus));
			Log.v("RMS","response spl =" + AcousticConverter.getInputLevel(results));
			
			if(sweepTrial == true){
				file= AudioPulseFileWriter.generateFileName("DPOAE",Double.toString(f),super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),file.getAbsolutePath());
				data.putSerializable(file.getAbsolutePath(),results.clone());
			}else if(thisFrequency == 2000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","2",super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),AudioPulseDataAnalyzer.RAWDATA_2KHZ);
				data.putSerializable(AudioPulseDataAnalyzer.RAWDATA_2KHZ,results.clone());
				/*
				//Save stimulus
				file= AudioPulseFileWriter.generateFileName("DPOAE-Stim","2",super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),AudioPulseDataAnalyzer.STIM_2KHZ);
				data.putSerializable(AudioPulseDataAnalyzer.STIM_2KHZ,stimulus.clone());
				*/
			} else if(thisFrequency == 3000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","3",super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),AudioPulseDataAnalyzer.RAWDATA_3KHZ);
				data.putSerializable(AudioPulseDataAnalyzer.RAWDATA_3KHZ,results.clone());
			}else if(thisFrequency == 4000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","4",super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),AudioPulseDataAnalyzer.RAWDATA_4KHZ);
				data.putSerializable(AudioPulseDataAnalyzer.RAWDATA_4KHZ,results.clone());
			}
			
			//TODO: For now hard-code value instead of dynamically get from Resources...
			if(super.testName.contentEquals("TEST DPOAE")){
				//Extra parameters added for TestDPOAEActivity Only!
				double F1= thisFrequency/1.2;
				double expected=2*F1 - thisFrequency;
				int fftSize=(int) Math.round(
						Signals.dpoeaGorgaEpochTime()*super.recordingSampleFrequency);
				fftSize=(int) Math.pow(2,Math.floor(Math.log((int) fftSize)/Math.log(2)));
				data.putLong("N",results.length);
				data.putShortArray("samples",results);
				data.putFloat("recSampleRate",super.recordingSampleFrequency);
				data.putDouble("expectedFrequency",expected);
				data.putInt("fftSize",fftSize);
			}

			Log.v(TAG,"Holding data of size: " + results.length + " in class memory until return of PlotAudioGramActivity.");
			Log.v(TAG,"If data gets accepted by user, will be stored at: " + file.getAbsolutePath());
			sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true because we delegate writing of file to another thread...

			try {
				localDPGRAM = analyzeResults(results,
						super.recordingSampleFrequency,
						thisFrequency,localDPGRAM);
			} catch (Exception e) {
				Log.v(TAG,"Could not generate analysis for results!!" + e.getMessage());
				e.printStackTrace();
			}

			Log.v(TAG,"adding key: " + AudioPulseDataAnalyzer.Results_MAP);
			data.putSerializable(AudioPulseDataAnalyzer.Results_MAP,DPGRAM);

			//The file passed here is not used in the analysis (ie not opened and read)!!
			//It is used when the analysis gets accepted by the user: the app packages
			//the file with stored the data for transmission with timestamp on the file name
			data.putSerializable(AudioPulseDataAnalyzer.MetaData_RawFileNames,fileNames);
			data.putSerializable(AudioPulseDataAnalyzer.FileNameRawData_MAP,fileNamestoDataMap);
			for (String tmpkey: localDPGRAM.keySet()){
				Log.v(TAG,"local " + tmpkey + " = " + localDPGRAM.get(tmpkey));
				DPGRAM.put(tmpkey,localDPGRAM.get(tmpkey));
			}
		}

		Log.v(TAG,"Sending analyzed data to activity, keys in bundle are:");
		for (String tmpkey: DPGRAM.keySet()){
			Log.v(TAG,"global= " + tmpkey + " = " + DPGRAM.get(tmpkey));
		}
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE,data);
		Log.v(TAG,"donew with " + TAG);
	}

	//Analysis of the results on the raw data
	private HashMap<String, Double> analyzeResults(short[] data, 
			double Fs, double F1, 
			HashMap<String, Double> DPGRAM) throws Exception {
		Log.v(TAG,"data.length= " + data.length);	
		AudioPulseDataAnalyzer dpoaeAnalysis=
				new DPOAEGorgaAnalyzer(data,Fs,F1,DPGRAM);
		return dpoaeAnalysis.call();

	}

}
