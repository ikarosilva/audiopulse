package org.audiopulse.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.audiopulse.R;
import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.DPOAEGorgaAnalyzer;
import org.audiopulse.io.AudioPulseFileWriter;

import android.os.Bundle;
import android.util.Log;

public class DPOAEProcedure extends TestProcedure{

	private final String TAG = "DPOAEProcedure";
	private Bundle data;
	private HashSet<String> fileNames=new HashSet<String>();
	private HashMap<String,String> fileNamestoDataMap=new HashMap<String,String>();
	private HashMap<String, Double> DPGRAM= new HashMap<String, Double>();

	public DPOAEProcedure(TestActivity parentActivity, String testEar) {
		super(parentActivity, testEar);
	}

	public synchronized static double dpoaeGorgaAmplitude(double Frequency){
		//FIXME: Gorga's test requires a stimulus at 65 dB SPL
		//but this seems to result in clipping for most phones.
		//we need to find an optimal maximum level that does not clip the sound

		//From calibration experiments with the ER10C set to 0 dB gain, the linear range of
		//response-to-stimulus is from 50-30 dB on an acoustic coupler (response will saturate
		// on either extremes).
		return 70;
	}
	public static double dpoeaGorgaEpochTime(){
		//return epoch time in seconds
		return 0.02048;
	}

	@Override
	public void run() {

		//Loop through all the test frequencies, generating stimulus and collecting the results
		ArrayList<Double> testFrequencies=new ArrayList<Double>();

		//use this to debug by generating a sweep across levels
		boolean sweepTrial=true;
		double f =4000.0;
		Double attStep=10.0;
		double oldFrequency=f;	
		Double att=-attStep;

		//Testing across 3 major frequencies sweep to search for distortion
		testFrequencies.add(2000.0);
		testFrequencies.add(3000.0);
		testFrequencies.add(4000.0);

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
			if(oldFrequency != thisFrequency.doubleValue()){
				oldFrequency=thisFrequency.doubleValue();
				att=-attStep;
			}

			att=att+attStep;
			splLevel=dpoaeGorgaAmplitude(thisFrequency)-att;

			//short[] stimulus=AudioSignal.convertMonoToShort(
			//		AudioSignal.convertToMono(probe));
			//TODO: Implement USB connection testIO.setPlaybackAndRecording(stimulus);

			double stTime= System.currentTimeMillis();
			//TODO: Implement USB results = testIO.acquire();
			results=new short[100];
			double ndTime= System.currentTimeMillis();
			Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000 + " secs" );
			File file=null;


			if(thisFrequency == 2000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","2",super.testEar,splLevel);
				/*
				//Save stimulus
				file= AudioPulseFileWriter.generateFileName("DPOAE-Stim","2",super.testEar,splLevel);
				fileNames.add(file.getAbsolutePath());
				fileNamestoDataMap.put(file.getAbsolutePath(),AudioPulseDataAnalyzer.STIM_2KHZ);
				data.putSerializable(AudioPulseDataAnalyzer.STIM_2KHZ,stimulus.clone());
				 */
			} else if(thisFrequency == 3000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","3",super.testEar,splLevel);
			}else if(thisFrequency == 4000){
				file= AudioPulseFileWriter.generateFileName("DPOAE","4",super.testEar,splLevel);
			}

			fileNames.add(file.getAbsolutePath());
			fileNamestoDataMap.put(file.getAbsolutePath(),file.getAbsolutePath());
			data.putSerializable(file.getAbsolutePath(),results.clone());

			//TODO: For now hard-code value instead of dynamically get from Resources...
			//Extra parameters added for TestDPOAEActivity Only!
			double F1= thisFrequency/1.2;
			double expected=2*F1 - thisFrequency;
			int fftSize=(int) Math.round(
					dpoeaGorgaEpochTime()*super.recordingSampleFrequency);
			fftSize=(int) Math.pow(2,Math.floor(Math.log((int) fftSize)/Math.log(2)));
			data.putLong("N",results.length);
			data.putShortArray("samples",results);
			data.putFloat("recSampleRate",super.recordingSampleFrequency);
			data.putDouble("expectedFrequency",expected);
			data.putInt("fftSize",fftSize);
			

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
