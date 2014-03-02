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
	
	public MonitorHandler(TestEarActivity testActivity)
	{
		parentActivity = testActivity;
	}

	public static class Messages {
		public static final int CLEAR_LOG = 1;				//clear the test log
		public static final int LOG = 2;					//add to the test log
		public static final int PROGRESS = 3;				//progress has been made
		public static final int IO_COMPLETE = 4;			//io phase is complete
		public static final int ANALYSIS_COMPLETE = 5;		//analysis block complete
		public static final int PROCEDURE_COMPLETE = 6;		//entire test procedure is complete
	}
	
	@Override
	public void handleMessage(Message msg) {
		Log.v(TAG,"handling message " + msg.toString());
		Bundle data = msg.getData();
		switch (msg.what) {
		case Messages.CLEAR_LOG:
			parentActivity.app_out.setText("");
			break;
		case Messages.LOG:
			String pm = data.getString(LOGUI);
			Log.v(TAG,pm);
			parentActivity.app_out.append(pm);
			break;
		case Messages.ANALYSIS_COMPLETE:
			break;
		}
	}

}
