/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * TestProcedure.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.tests;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.audiopulse.R;
import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.DPOAEGorgaAnalyzer;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.io.UsbAudioInterface;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TestProcedure implements Runnable{
	private static final String TAG="TestProcedure";
	private Handler uiThreadHandler;	//handler back to TestActivity
	private Thread workingThread;		//main worker thread to perform test
	protected final String testEar;
	Resources resources;
	private Bundle data;
	UsbAudioInterface audioInterface;
	private HashSet<String> fileNames=new HashSet<String>();
	private HashMap<String,String> fileNamestoDataMap=new HashMap<String,String>();

	public TestProcedure (TestActivity parent, String testEar, Resources resources) 
	{
		uiThreadHandler = new Handler(parent);
		this.testEar = testEar;
		parent.getApplicationContext();
		this.resources=resources;
	}

	//call from Activity to perform test in a new thread
	public final void start() {
		workingThread = new Thread( this , "TestMainThread");
		workingThread.start();
	}


	public void run(){

		//Initialize audio interface based on XML configuration settings
		audioInterface.initialize(resources.getInteger(R.integer.recordingSamplingFrequency),
				resources.getInteger(R.integer.playbackSamplingFrequency),
				resources.getInteger(R.integer.recordingBitDepth),
				resources.getInteger(R.integer.recordingBitDepth),
				resources.getInteger(R.integer.recordingChannelConfig),
				resources.getInteger(R.integer.playbackChannelConfig));
		
		HashMap<String, Double> localDPGRAM = new HashMap<String, Double>();
		
		int recFs=resources.getInteger(R.integer.recordingSamplingFrequency);
		//Loop through all the test frequencies, generating stimulus and collecting the results	
		String[] F1Hz = resources.getStringArray(R.array.TestFrequencyF1Hz);
		String[] F2Hz = resources.getStringArray(R.array.TestFrequencyF2Hz);
		String[] F1SPL = resources.getStringArray(R.array.TestFrequencyF1SPL);
		String[] F2SPL = resources.getStringArray(R.array.TestFrequencyF2SPL);
		String[] responseFrequency = resources.getStringArray(R.array.ResponseFrequencyHz);
		String[] fileSuffix= resources.getStringArray(R.array.FileName);
		
		double epochTime=Double.valueOf(resources.getString(R.string.epochTime));
		int numberOfSweeps=Integer.valueOf(resources.getString(R.string.numberOfSweeps));
		String testName=resources.getString(R.string.DPOAETestName);
		
		for (int i=0;i<F1Hz.length;i++){
			
			double F2=Double.valueOf(F2Hz[i]); //frequency of hearing being tested in Hz
			double[] multiToneFrequency={Double.valueOf(F1Hz[i]),F2};
			double[] multiToneLevel={Double.valueOf(F1SPL[i]),Double.valueOf(F2SPL[i])};
			double responseF=Double.valueOf(responseFrequency[i]);
			
			audioInterface.playMultiTone(multiToneLevel,multiToneLevel,epochTime,numberOfSweeps);
			int[] XFFT=audioInterface.getAveragedRecordedPowerSpectrum();
			int[] x=audioInterface.getAveragedRecordedWaveForm();
			File file=null;
			file= AudioPulseFileWriter.generateFileName(testName,F1Hz[i]+"Hz",testEar,Double.valueOf(F1SPL[i]));


			fileNames.add(file.getAbsolutePath());
			fileNamestoDataMap.put(file.getAbsolutePath(),file.getAbsolutePath());
			data.putSerializable(file.getAbsolutePath(),XFFT.clone());

			data.putIntArray("samples",XFFT);
			data.putFloat("recSampleRate",recFs);
			data.putDouble("expectedFrequency",responseF);
			data.putInt("fftSize",XFFT.length);


			sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true because we delegate writing of file to another thread...

			try {
				localDPGRAM = analyzeResults(XFFT,recFs,F2,localDPGRAM,resources);
			} catch (Exception e) {
				e.printStackTrace();
			}

			data.putSerializable(AudioPulseDataAnalyzer.Results_MAP,DPGRAM);

			//The file passed here is not used in the analysis (ie not opened and read)!!
			//It is used when the analysis gets accepted by the user: the app packages
			//the file with stored the data for transmission with timestamp on the file name
			data.putSerializable(AudioPulseDataAnalyzer.MetaData_RawFileNames,fileNames);
			data.putSerializable(AudioPulseDataAnalyzer.FileNameRawData_MAP,fileNamestoDataMap);
			for (String tmpkey: localDPGRAM.keySet()){
				DPGRAM.put(tmpkey,localDPGRAM.get(tmpkey));
			}
		}
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE,data);
	}

	//Analysis of the results on the raw data
	private HashMap<String, Double> analyzeResults(int[] data, 
			double Fs, double F1, 
			HashMap<String, Double> DPGRAM,Resources resources) throws Exception {
		AudioPulseDataAnalyzer dpoaeAnalysis=
				new DPOAEGorgaAnalyzer(data,Fs,F1,DPGRAM, Resources  resources);
		return dpoaeAnalysis.call();

	}
}

protected void sendMessage(int what) {
	Message m = this.uiThreadHandler.obtainMessage(what);
	this.uiThreadHandler.sendMessage(m);
}
protected void sendMessage(int what, Bundle data) {
	Message m = this.uiThreadHandler.obtainMessage(what);
	m.setData(data);
	this.uiThreadHandler.sendMessage(m);
}

//Print message to testLog TextView
protected void logToUI(String str)
{
	Log.i(TAG,str);
	Bundle data = new Bundle();
	data.putString("log", str);
	sendMessage(TestActivity.Messages.LOG,data);
}

protected void clearLog() {
	sendMessage(TestActivity.Messages.CLEAR_LOG);
}


}
