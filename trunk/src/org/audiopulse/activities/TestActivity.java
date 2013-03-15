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
import org.audiopulse.tests.TestProcedure;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//TestActivity is a template for all tests.
public class TestActivity extends AudioPulseActivity implements Handler.Callback
{
	public final String TAG="BasicTestActivity";
	
	protected TextView testLog;
	protected TestProcedure testProcedure;
	protected int recordingSamplingFrequency;
	protected int playbackSamplingFrequency;
	private boolean calledBySana;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_test_layout);
		recordingSamplingFrequency=this.getResources().getInteger(R.integer.samplingFrequency);
		playbackSamplingFrequency=this.getResources().getInteger(R.integer.samplingFrequency);
		testLog = (TextView)this.findViewById(R.id.testLog);

		//TODO: can we put a TestProcedure into a bundle? E.g. by implementing Parcelable, but is that worth it?
//		Bundle request = getIntent().getExtras();
//		testProcedure = (TestProcedure) request.get("test");
		
		String caller = this.getCallingPackage();
		if (caller != null && getCallingPackage().compareToIgnoreCase("org.moca") == 0){
			// Sana observation meta data - from Sana API ObservationActivity
			initMetaData();
			calledBySana = true;
		} else {
			Log.v(TAG,"Running AudioPulse in standalone mode");
			calledBySana = false;
		}
	}

	//Begin test -- this function is called by the button in the default layout
	public void startTest(View callingView)
	{
		appendText("Starting Test Procedure");
		if (testProcedure==null) {
			appendText("No TestProecdure set!");
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

	//plot audiogram
	public void plotAudiogram(Bundle resultsBundle ) {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		intent.putExtras(resultsBundle);
		startActivity(intent);
	}
	
	//TODO: expand on this for other message types. Implement nested class NativeMessageHandler? 
	//default implementation for handling messages from TestProecdure objects
	public boolean handleMessage(Message msg) {
		Bundle data = msg.getData();
		switch (msg.what) {
		case Messages.CLEAR_LOG:
			emptyText();
			break;
		case Messages.LOG:
			String pm = data.getString("log");
			appendText(pm);
			break;
		}
		return true;
	}

	public static class Messages {
		public static final int CLEAR_LOG = 1;				//clear the test log
		public static final int LOG = 2;					//add to the test log
		public static final int PROGRESS = 3;				//progress has been made
		public static final int IO_COMPLETE = 4;			//io phase is complete
		public static final int ANALYSIS_COMPLETE = 5;		//analysis block complete
		public static final int PROCEDURE_COMPLETE = 6;		//entire test procedure is complete
	}

	public int getRecordingSampleFrequency() {
		return recordingSamplingFrequency;
	}
	public int getPlaybackSampleFrequency() {
		return playbackSamplingFrequency;
	}
}