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
import org.audiopulse.io.Utils;
import org.audiopulse.tests.DPOAECalibrationProcedure;
import org.audiopulse.tests.TestProcedure;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.
//FIXME: for now implementing my debugging procedures here. FIgure out our structure, and make the appropriate structural changes.
public class BasicTestActivity extends AudioPulseActivity implements Callback
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

	//begin test. Generally, this function is called by a ButtonView in the layout.
	public void startTest(View callingView)
	{
		appendText("Starting Test Procedure");
		if (testProcedure==null) {
//			appendText("No TestProecdure set!");
			//FIXME: don't do this. Just doing it now for convenience.
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
//		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
//		intent.putExtras(audioResultsBundle);
//		this.audioResultsBundle=audioResultsBundle;
//		startActivity(intent);
		//FIXME
	}

	//plot recorded waveform
	public void plotWaveform() {
//		//TODO: Add check for not null audioResultsBundle (notify user that to run stimulus if they press this option before running anything).
//		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
//		intent.putExtras(audioResultsBundle);
//		startActivity(intent);
		//FIXME
	}

	public void appendData(Bundle b) {
		//TODO: something?
	}

	public boolean handleMessage(Message msg) 
	{
		//FIXME: make this a more general test message handler
		Log.v(TAG,"Handling received message");
		String pm = Utils.getStringFromABundle(msg.getData());		
		Bundle b=msg.getData();
		if(b.getLong("N") == 0L){
			appendText(pm);
		}else{
			// Thread should be done so we are sending data back to
			// parent
			// Need this so parent has bundled file uri to return to Sana.
			appendData(b);
			if(b.getBoolean("showSpectrum") ==true){
				plotSpectrum(b);
			}
		}
		return true;	//TODO: make return value appropriate
	}

}