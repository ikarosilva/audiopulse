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

import java.util.LinkedList;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.PlayRecordManager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class TestProcedure implements Runnable{
	protected String TAG;
	
	private Handler uiThreadHandler;	//handler back to TestActivity
	private Thread workingThread;		//main worker thread to perform test
	
	protected PlayRecordManager testIO;
	protected AcousticConverter hardware;
	protected int playbackSampleFrequency = 44100;
	protected int recordingSampleFrequency = 44100;
	//TODO: get sample freqs from app data
	
	public TestProcedure (TestActivity parent) {
		TAG = "TestProcedure";
		this.uiThreadHandler = new Handler(parent);
		testIO = new PlayRecordManager(playbackSampleFrequency,recordingSampleFrequency);
		hardware = new AcousticConverter();
	}
	
	//call from Activity to perform test in a new thread
	public final void start() {
		if (!getAudioResources()) {
			Log.e(TAG,"Failed to get audio focus!");
			//TODO: treat this more seriously
		}
		workingThread = new Thread( this , "TestMainThread");
		workingThread.setPriority(Thread.MAX_PRIORITY);
		workingThread.start();
		releaseAudioResources();
	}
	
	//run() should implement entire test procedure, including calibration and analysis
	public abstract void run();

	
	protected boolean getAudioResources() {
		//TODO
//		 requestAudioFocus (AudioManager.OnAudioFocusChangeListener l, int streamType, int durationHint)
//		Context.getSystemService(Context.AUDIO_SERVICE)
//		if (isBluetoothA2dpOn()) {
//		    // Adjust output for Bluetooth.
//		} else if (isSpeakerphoneOn()) {
//		    // Adjust output for Speakerphone.
//		} else if (isWiredHeadsetOn()) {
//		    // Adjust output for headsets
//		} else { 
//		    // If audio plays and noone can hear it, is it still playing?
//		}
//		private class NoisyAudioStreamReceiver extends BroadcastReceiver {
//		    @Override
//		    public void onReceive(Context context, Intent intent) {
//		        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
//		            // Pause the playback
//		        }
//		    }
//		}
//
//		private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//
//		    registerReceiver(myNoisyAudioStreamReceiver(), intentFilter);
		return true;
	}
	protected void releaseAudioResources() {
		//TODO
//		abandonAudioFocus(AudioManager.OnAudioFocusChangeListener l)
//	    unregisterReceiver(myNoisyAudioStreamReceiver);
	}
	
	//useful calibration routines for any test
	protected void calibrateChrip() {
		//TODO
	}
	protected void calibrateNoise() {
		//TODO
	}
	protected void calibrateClicks(double peakLevel, double intervalInMillis) {
		//TODO
	}
	
	
	//this doesn't seem to add enough to justify putting here, costs readability & flexibility in subclasses.
//	private abstract interface TestParameters {
//		//implementations of this interface should have internal
//		//members such as tone frequency, level, etc.
//		public double[][] createStimulus(double sanmpleFrequency, AcousticConverter hardware);
//	}
//	private LinkedList<TestParameters> testList;
//	public boolean addTest(TestParameters params) {
//		return testList.add(params);
//	}
//	public void clearTests() {
//		testList.clear();
//	}
//	public TestParameters nextTest() {
//		return testList.poll();
//	}
//	
		
	//send a message to parent Activity
	@Deprecated
	protected void sendMessage(Bundle data) {
		Message m = this.uiThreadHandler.obtainMessage();
		m.setData(data);
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
		sendMessage(TestActivity.MESSAGES.LOG,data);
	}
	
	protected void clearLog() {
		sendMessage(TestActivity.MESSAGES.CLEAR_LOG,null);
	}
	
	
}
