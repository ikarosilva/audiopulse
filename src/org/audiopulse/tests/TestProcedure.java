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
import java.util.HashMap;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.DPOAEGorgaAnalyzer;
import org.audiopulse.io.AudioPulseFileWriter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class TestProcedure implements Runnable{
	private static final String TAG="TestProcedure";
	private Handler uiThreadHandler;	//handler back to TestActivity
	private Thread workingThread;		//main worker thread to perform test
	protected final String testEar;
	
	public TestProcedure (TestActivity parent, String testEar) 
	{
		uiThreadHandler = new Handler(parent);
		this.testEar = testEar;
		parent.getApplicationContext();
	}
	
	//call from Activity to perform test in a new thread
	public final void start() {
		workingThread = new Thread( this , "TestMainThread");
		workingThread.start();
	}
	
	
	public void run(){
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
				data=new Bundle();
				short[] results;
				short[] stimulus;
				double splLevel=0;
				for (Double thisFrequency : testFrequencies){
					sendMessage(TestActivity.Messages.PROGRESS);
					logToUI("Running DPOAE frequency: " + thisFrequency + " kHz");

					
					att=att+attStep;
					splLevel=dpoaeGorgaAmplitude(thisFrequency);

				   //TODO: Implement USB connection testIO.setPlaybackAndRecording(stimulus);

					double stTime= System.currentTimeMillis();
					//TODO: Implement USB results = testIO.acquire();
					results=new short[100];
					File file=null;
					file= AudioPulseFileWriter.generateFileName("DPOAE",thisFrequency.toString()+"Hz",super.testEar,splLevel);
					

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
					

					sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true because we delegate writing of file to another thread...

					try {
						localDPGRAM = analyzeResults(results,
								super.recordingSampleFrequency,
								thisFrequency,localDPGRAM);
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
			private HashMap<String, Double> analyzeResults(short[] data, 
					double Fs, double F1, 
					HashMap<String, Double> DPGRAM) throws Exception {
				AudioPulseDataAnalyzer dpoaeAnalysis=
						new DPOAEGorgaAnalyzer(data,Fs,F1,DPGRAM);
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
