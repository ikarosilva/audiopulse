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

import org.audiopulse.R;
import org.audiopulse.tests.DPOAECalibrationProcedure;
import org.audiopulse.tests.TestProcedure;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.
//FIXME: for now implementing my debugging procedures here. FIgure out our structure, and make the appropriate structural changes.
public class TestActivity extends AudioPulseActivity implements Handler.Callback
{
	public static final String TAG="BasicTestActivity";
	
	protected TextView testLog;
	protected TestProcedure testProcedure = null;		//test procedure to execute
	//use: testProcedure = new TestProcedure(this)
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_test_layout);
		testLog = (TextView)this.findViewById(R.id.testLog);
	}

	//Begin test -- this function is called by the button in the default layout
	public void startTest(View callingView)
	{
		appendText("Starting Test Procedure");
		if (testProcedure==null) {
//			appendText("No TestProecdure set!");
			//FIXME: don't do this. Just doing it now for convenience.
			//maybe define a DebuggingTestActivity?
			appendText("Starting DPOAE Calibration");
			testProcedure = new DPOAECalibrationProcedure(this);
			testProcedure.start();
		} else {
			testProcedure.start();
		}

	}

	public void appendText(String str){
		if (testLog != null) {
			testLog.setText(testLog.getText() + "\n" + str);
		} else {
			Log.e(TAG, "No test log element!");
		}
	}

	public void emptyText(){
		if (testLog != null) {
			testLog.setText("");
		} else {
			Log.e(TAG, "No test log element!");
		}
	}

	//plot recorded signal spectrum
	public void plotSpectrum(Bundle audioResultsBundle) {
		//TODO
	}

	//plot recorded waveform
	public void plotWaveform(Bundle audioResultsBundle) {
		//TODO
	}

	//TODO: expand on this for other message types. Implement nested class NativeMessageHandler? 
	//default implementation for handling messages from TestProecdure objects
	public boolean handleMessage(Message msg) {
		Bundle data = msg.getData();
		String pm = data.getString("log");
		appendText(pm);
		return true;
	}

}