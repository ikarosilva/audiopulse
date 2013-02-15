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
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.io;

import org.audiopulse.activities.GeneralAudioTestActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@Deprecated //AudioPulseActivity now implements Handler.Callback, use a regular Handler object instead
public class ReportStatusHandler extends Handler
{
	public static final String TAG = "ReportStatusHandler";
	private GeneralAudioTestActivity parentActivity = null;
	private boolean isBuzy;
	public ReportStatusHandler(GeneralAudioTestActivity inParentActivity)
	{
		//Registering handler in parent activity 
		parentActivity = inParentActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		String pm = Utils.getStringFromABundle(msg.getData());		
		Bundle b=msg.getData();
		isBuzy=(GeneralAudioTestActivity.getRecordingState() == GeneralAudioTestActivity.threadState.ACTIVE) &&
				(GeneralAudioTestActivity.getPlaybackState() == GeneralAudioTestActivity.threadState.ACTIVE);
		if(isBuzy){
			this.printLine("*");
			this.sendEmptyMessageDelayed(0,100);
		}else if (pm != null){
			this.printMessage(pm);
			if (GeneralAudioTestActivity.getRecordingState() == GeneralAudioTestActivity.threadState.COMPLETE){
				//If it is the end of a recording, get the file name for the data and append to meta information object
				Uri outfile=b.getParcelable(RecordThreadRunnable.RECFILEKEY);
				if(outfile != null){
					parentActivity.addFileToPackage("DPOAE",outfile.toString());
					//Run more test if there are any on the queue
					if(parentActivity.hasNextTestFrequency()){
						parentActivity.setRecordingState(GeneralAudioTestActivity.threadState.INITIALIZED);
						parentActivity.setPlaybackState(GeneralAudioTestActivity.threadState.INITIALIZED);
						Log.v(TAG,"Running frequecy test");
						parentActivity.selectAndRunThread();
					}
				}
			}				
			if(GeneralAudioTestActivity.getPackedDataState() == GeneralAudioTestActivity.threadState.INITIALIZED &&
					GeneralAudioTestActivity.getRecordingState() == GeneralAudioTestActivity.threadState.COMPLETE){
				//Call the package thread to compress and package the data, this method, though defined in the
				// parentActivity (GeneralAudioPulseTesting) is actually being implemented/called from a subclass (i.e., ThreadedRecPlayActivity). 
				Log.v(TAG,"Packing data");
				parentActivity.packageThread();
			}
			
			if(b.getBoolean("showSpectrum") ==true){
				this.plotAudioSpectrum(b);
			}
		} else if (GeneralAudioTestActivity.getPackedDataState() == GeneralAudioTestActivity.threadState.COMPLETE){
			Log.v(TAG,"plotting results from analysis");
			parentActivity.AnalyzeData(b);
			//Return data to Sana
			Log.v(TAG,"AP data sent to Sana:" + b.describeContents());
			parentActivity.appendData(b);
		}
	}

	private void printMessage(String str)
	{
		parentActivity.appendText(str);
	}

	private void printLine(String str)
	{
		parentActivity.appendLine(str);
	}

	private void plotAudioSpectrum(Bundle b)
	{
		//Printing status
		Log.v(TAG,"Plotting data from bundle");
		//parentActivity.audioResultsBundle=b;
		parentActivity.plotSpectrum(b);
	}
}
