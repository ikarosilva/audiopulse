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
import java.util.ArrayList;

import org.audiopulse.R;
import org.audiopulse.activities.TestActivity;
import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.DPOAEAnalyzer;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.io.UsbAudioEmulator;
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
	private ArrayList<DPOAEResults> DPGRAM = new ArrayList<DPOAEResults>();

	public TestProcedure (TestActivity testActivity, String testEar, Resources resources) 
	{
		this.testEar = testEar;
		Log.v(TAG,"Getting context");
		testActivity.getApplicationContext();
		Log.v(TAG,"Getting handler");
		uiThreadHandler = new Handler(testActivity);
		Log.v(TAG,"setting resources");
		this.resources=resources;
		data=new Bundle();
	}

	//call from Activity to perform test in a new thread
	public final void start() {
		workingThread = new Thread( this , "TestMainThread");
		workingThread.start();
	}


	public void run(){
		
		logToUI("Starting test...");
		if(resources.equals(null))
			Log.e(TAG,"resource is null!!");
	
		if(resources.getString(R.string.AudioInterfaceType).equals("emulator")){
			Log.v(TAG,"creating audio interface of type: emulator");
			audioInterface= new UsbAudioEmulator();
		}
		//Initialize audio interface based on XML configuration settings
		Log.v(TAG,"initializing interface");
		audioInterface.initialize(resources.getInteger(R.integer.recordingSamplingFrequency),
				resources.getInteger(R.integer.playbackSamplingFrequency),
				resources.getInteger(R.integer.recordingBitDepth),
				resources.getInteger(R.integer.recordingBitDepth),
				resources.getInteger(R.integer.recordingChannelConfig),
				resources.getInteger(R.integer.playbackChannelConfig));
		
		logToUI("Audio interface initiliazed");
		int recFs=resources.getInteger(R.integer.recordingSamplingFrequency);
		Log.v(TAG,"recording Fs set to:" + recFs);
		
		//Loop through all the test frequencies, generating stimulus and collecting the results	
		String[] F1Hz = resources.getStringArray(R.array.TestFrequencyF1Hz);
		String[] F2Hz = resources.getStringArray(R.array.TestFrequencyF2Hz);
		String[] F1SPL = resources.getStringArray(R.array.TestFrequencyF1SPL);
		String[] F2SPL = resources.getStringArray(R.array.TestFrequencyF2SPL);
		String[] FresHz = resources.getStringArray(R.array.ResponseFrequencyHz);

		logToUI("Testing " + F1Hz.length + " different frequencies");
		double epochTime=Double.valueOf(resources.getString(R.string.epochTime));
		Log.v(TAG,"epochTime:" + epochTime);
		
		int numberOfSweeps=Integer.valueOf(resources.getString(R.string.numberOfSweeps));
		Log.v(TAG,"Sweeps:" + numberOfSweeps);
		
		String testName=resources.getString(R.string.DPOAETestName);
		String testProtocolName=resources.getString(R.string.DPOAETestProtocolName);
		Log.v(TAG,"protocol= " + testProtocolName);
		double playTime=epochTime*numberOfSweeps;
		logToUI("Playing stimulus for: " + playTime + " seconds each.");
		
		for (int i=0;i<F1Hz.length;i++){

			double F2=Double.valueOf(F2Hz[i]); //frequency of hearing being tested in Hz
			double F1=Double.valueOf(F1Hz[i]);
			double[] multiToneFrequency={F1,F2};
			double[] multiToneLevel={Double.valueOf(F1SPL[i]),Double.valueOf(F2SPL[i])};
			double Fres=Double.valueOf(FresHz[i]);
			File file=null;
			DPOAEResults responseData = null;
			
			Log.v(TAG,"F1= " + F1 + " @ " + F1SPL[i] + " dB, F2= " + F2 + " @ " + F2SPL[i] + " dB Fres= " + Fres);
			logToUI("---Testinging frequency: F1= " + F1 + " kHz,  F2= " + F2 +  " kHz");
			
			//Call the USB interface with the stimulus parameters and obtain the data
			try {
				audioInterface.playMultiTone(multiToneFrequency,multiToneLevel,epochTime,numberOfSweeps);
			} catch (InterruptedException e1) {
				logToUI("*****Error: could not play stimulus!!");
			}
			int[] XFFT=audioInterface.getAveragedRecordedPowerSpectrum();

			//Get information that will generate the candidate file name for this specific stimulus if test gets accepted
			file= AudioPulseFileWriter.generateFileName(testName,F1Hz[i]+"Hz",testEar,Double.valueOf(F1SPL[i]));
			try {
				Log.v(TAG,"Estimating response of size:" + XFFT.length);
				DPOAEAnalyzer dpoaeAnalysis=new DPOAEAnalyzer(XFFT,recFs,F2,F1,Fres,testProtocolName,file.getAbsolutePath());
				responseData = dpoaeAnalysis.call();
				DPGRAM.add(responseData);
			} catch (Exception e) {
				logToUI("***Error: could not analyze data!!");
			}
			logToUI("     Response SPL: " + Math.round(responseData.getRespSPL()) + 
					" dB, Noise SPL: " + Math.round(responseData.getNoiseSPL()));
			data.putSerializable("DPGRAM",DPGRAM);
		}
		logToUI("---Plotting audiogram ...");
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE,data);
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
		Log.v(TAG,str);
		Bundle data = new Bundle();
		data.putString("log", str);
		sendMessage(TestActivity.Messages.LOG,data);
	}
	

	protected void clearLog() {
		sendMessage(TestActivity.Messages.CLEAR_LOG);
	}


}
