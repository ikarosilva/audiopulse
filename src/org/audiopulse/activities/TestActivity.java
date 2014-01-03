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
import org.sana.android.app.ObservationActivity;
import org.audiopulse.R;
import org.audiopulse.tests.TestProcedure;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

//TestActivity is a template for all tests.
public class TestActivity extends ObservationActivity implements Handler.Callback
{
	public static final String TAG="TestActivity";
	public static final int CONFIRM_PLOT_CODE = 1;
	protected TextView testLog;
	protected TestProcedure testProcedure;
	private static boolean calledBySana;
	public Resources resources = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"setting content view");
		setContentView(R.layout.basic_test_layout);
		Log.v(TAG,"finding test log");
		testLog = (TextView)this.findViewById(R.id.testLog);
		Log.v(TAG,"getting extras");
		Bundle request = getIntent().getExtras();
		String testEar = null;
		Log.v(TAG,"getting package name");
		String caller = this.getCallingPackage();
		Log.v(TAG,"Calling package is: " + getCallingPackage());
		resources=getResources();
		Log.v(TAG,"got resources");		
		
		if (caller != null && getCallingPackage().compareToIgnoreCase("org.moca") == 0){
			
			// Sana observation meta data - from Sana API ObservationActivity interface
			initMetaData();
			calledBySana = true;
			Log.v(TAG,"Processing through Sana");
			String test = getIntent().getAction();
			if(test.equals("org.audiopulse.TestDPOAERightEarActivity")){
				testEar=getResources().getString(R.string.RigtEarKey);
			}else if (test.equals("org.audiopulse.TestDPOAELeftEarActivity")){
				testEar=getResources().getString(R.string.LeftEarKey);
			}else{
				Log.e(TAG,"Test ear type not identified. setting to null. Testear= " + test);
				testEar=null;
			}	
		} else {
			Log.v(TAG,"Running AudioPulse in standalone mode");
			testEar=request.getString(getResources().getString(R.string.testEarKey));
			calledBySana = false;
		}
		testProcedure = new TestProcedure(this,testEar,resources);
        testProcedure.start();
        
	}

	//Begin test -- this function is called by the button in the default layout
	public void startTest(View callingView)
	{
		
		appendText(this.getResources().getString(R.string.startingTest).toString());
		if (testProcedure==null) {
			appendText(this.getResources().getString(R.string.noProcedure).toString());
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

	//plot audiogram
	public void plotAudiogram(Bundle resultsBundle ) {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		intent.putExtras(resultsBundle);
		// Added conditional to see if we want confirmation that plot is accepted
		// such as when running headless - EW
		if(calledBySana)
			startActivityForResult(intent, CONFIRM_PLOT_CODE);
		else
			startActivity(intent);
	}
	
	// Return back the intent to PltAudiogramActivity, to be bundled
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK && requestCode == CONFIRM_PLOT_CODE && calledBySana)
		{
			// Get the URI name from the global variable
			data.getStringExtra("ZIP_URI");
			Log.i(TAG,"Obtained URI from  PlotAudiogramActivity:" + data.toString());
			Log.i(TAG,"Exiting from AP back to Sana...");
		}
	}  

	//TODO: expand on this for other message types. Implement nested class NativeMessageHandler? 
	//default implementation for handling messages from TestProecdure objects
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
			plotAudiogram(results);
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


}