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
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;
import org.audiopulse.tests.DPOAEProcedure;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;


//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.
//FIXME: for now implementing my debugging procedures here. FIgure out our structure, and make the appropriate structural changes.
public class TestDPOAEActivity extends TestActivity implements Handler.Callback
{
	public final String TAG="TestDPOAEActivity";

	//Begin test -- this function is called by the button in the default layout
	public void startTest(View callingView)
	{
		appendText("Starting Test DPOAE Procedure");
		if (testProcedure==null) {
			appendText("Starting Test DPOAE");
			testProcedure = new DPOAEProcedure(this);
			testProcedure.start();
		} else {
			testProcedure.start();
		}		
	}	

	//Overwriting from TestProcedure to deal with this specific test
	public boolean handleMessage(Message msg) {
		Log.v(TAG,"handling message " + msg.toString());
		Bundle data = msg.getData();
		switch (msg.what) {
		case Messages.CLEAR_LOG:
			emptyText();
			break;
		case Messages.LOG:
			String pm = data.getString("log");
			appendText(pm);
			break;
		case Messages.ANALYSIS_COMPLETE:
			Bundle results=msg.getData();
			//Start Plotting activity
			Log.v(TAG,"calling spectral plot activity ");
			plotSpectrum(results);
		}
		return true;
	}

}