package org.audiopulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MonitorHandler extends Handler
{
	public static final String TAG = "MonitorHandler";
	private TestEarActivity parentActivity = null; 

	public MonitorHandler(TestEarActivity testActivity)
	{
		parentActivity = testActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		Bundle b=msg.getData();
		String pm = b.getString("logUI");			
		Log.v(TAG,pm);
		this.logUI(pm);
	}

	private void logUI(String str)
	{
		parentActivity.app_out.append(str);
	}
}
