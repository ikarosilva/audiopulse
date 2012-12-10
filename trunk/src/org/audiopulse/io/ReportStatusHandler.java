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

import org.audiopulse.activities.TestActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReportStatusHandler extends Handler
{
	public static final String TAG = "ReportStatusHandler";
	private TestActivity parentActivity = null;

	public ReportStatusHandler(TestActivity inParentActivity)
	{
		//Registering handler in parent activity 
		parentActivity = inParentActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		Log.v(TAG,"Handling received message");
		String pm = Utils.getStringFromABundle(msg.getData());		
		Bundle b=msg.getData();
		if(b.getLong("N") == 0L){
			this.printMessage(pm);
		}else{
			if(b.getBoolean("showSpectrum") ==true){
				this.plotAudioSpectrum(b);
			}
		}


	}

	private void printMessage(String str)
	{
		//Printing status
		Log.v(TAG,"printing message");
		parentActivity.appendText(str);
	}

	private void plotAudioSpectrum(Bundle b)
	{
		//Printing status
		Log.v(TAG,"Plotting data from bundle");
		//parentActivity.audioResultsBundle=b;
		parentActivity.plotSpectrum(b);
	}
}
