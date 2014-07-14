package org.audiopulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MonitorHandler extends Handler
{
	public static final String TAG = "MonitorHandler";
	private TestEarActivity parentActivity = null; 
	public static final String LOGUI="logUI";
	public volatile boolean dataIsReady;
	public MonitorHandler(TestEarActivity testActivity)
	{
		parentActivity = testActivity;
	}

	public static class Messages {
		public static final int CLEAR_LOG = 1;				//clear the test log
		public static final int LOG = 2;					//add to the test log
		public static final int PROGRESS = 3;				//progress has been made
		public static final int IO_COMPLETE = 4;			//io phase is complete
		public static final int RECORDING_COMPLETE = 5;		//analysis block complete
		public static final int PROCEDURE_COMPLETE = 6;		//entire test procedure is complete
		public static final int ANALYZE_DATA = 7;
		public static final int TEST_FREQUENCY = 8;
	}
	
	@Override
	public void handleMessage(Message msg) {
		
		Bundle data = msg.getData();
		switch (msg.what) {
		case Messages.CLEAR_LOG:
			parentActivity.app_out.setText("");
			break;
		case Messages.LOG:
			String pm = data.getString(LOGUI);
			parentActivity.app_out.append(pm);
			break;
		case Messages.TEST_FREQUENCY:
			parentActivity.doOneTest();//Starts recording for one frequency
			break;
		case Messages.RECORDING_COMPLETE:
			dataIsReady=false;
			try{
			parentActivity.getData();
			parentActivity.app_out.append("Recording data length=" + parentActivity.getPSDSize());
			if(parentActivity.psd != null){
				//The analysis should be quick enough to do on the UI without getting ANR error.
				//In the case we do get it, we may want to push this to the MonitorThread class.
				parentActivity.analyzePSD();
				double diff=Math.round((parentActivity.respSPL-parentActivity.noiseSPL)*10)/10.0;
				parentActivity.app_out.append("\nResp: " + parentActivity.respSPL
						+ " dB, noise= " + parentActivity.noiseSPL + " dB , diff: " +
						diff + " SPL");
				parentActivity.plotdata_button.setEnabled(true);
				dataIsReady=true;
			}
			}catch(Exception e) {
				//In this case the recording failed. Should we try again ??
				parentActivity.app_out.append(e.getMessage());
			}
			break;
		}
	}

	public synchronized void setCurrentFrequency(short f1, short f2) {
		parentActivity.setCurrentTestFrequencies(f1, f2);
	}
	
	public synchronized short getCurrentF1() {
		return parentActivity.getCurrentF1();
	}
	
	public synchronized short getCurrentF2() {
		return parentActivity.getCurrentF2();
	}

}
