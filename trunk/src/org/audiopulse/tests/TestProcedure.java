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

import org.audiopulse.activities.TestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.PlayRecordManager;
import org.audiopulse.io.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

public abstract class TestProcedure implements Runnable{
	//run() should implement entire test procedure, including calibration and analysis
	public abstract void run();
	
	private static final String TAG = "TestProcedure";
	
	private Handler uiThreadHandler;	//handler back to TestActivity
	private Thread workingThread;		//main worker thread to perform test
	
	protected PlayRecordManager testIO;
	protected AcousticConverter levels;
	protected int playbackSampleFrequency = 44100;
	protected int recordingSampleFrequency = 44100;
	//TODO: get sample freqs from app data
	
	public TestProcedure (TestActivity parent) {
		this.uiThreadHandler = new Handler(parent);
		testIO = new PlayRecordManager();
		levels = new AcousticConverter();
	}
	
	//call from Activity to perform test in a new thread
	public final void start() {
		//TODO: lock resources, set volume, turn on airplane mode, etc
		workingThread = new Thread( this , "TestMainThread");
		workingThread.setPriority(Thread.MAX_PRIORITY);
		workingThread.start();
//		try {
//			workingThread.join();
//		} catch (InterruptedException e) {
//			Log.e(TAG,"Test interrupted!");
//			e.printStackTrace();
//			
//			//TODO: something!!
//		}
		
		//TODO: release resources
	}
	
		
	//TODO: other messaging utilities?
	
	//send arbitrary message to parent Activity
	protected void sendMessage(Bundle data) {
		Message m = this.uiThreadHandler.obtainMessage();
		m.setData(data);
		this.uiThreadHandler.sendMessage(m);
	}
	
	//Print message to testLog TextView
	protected void logToUI(String str)
	{
		Bundle data = new Bundle();
		data.putString("log", str);
		sendMessage(data);
	}
	
	
}
