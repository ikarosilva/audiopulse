package org.audiopulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MonitorThread extends Thread
{
	static final String TAG = "MonitorThread";
	Handler mainThreadHandler = null;
	int count = 0;
	public MonitorThread(Handler h)
	{
		mainThreadHandler = h;
	}
	
	@Override
	public void run()
	{
		Log.v(TAG,"Starting thread task");
		sendMessage("Monitor started");
		Log.v(TAG,"started execution");
		try {
			Thread.sleep(2000);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendMessage("Monitor finished");
		

	}
	
	private synchronized void sendMessage(String str){
		Message msg = this.mainThreadHandler.obtainMessage(MonitorHandler.Messages.LOG);
		Bundle b = new Bundle();
		b.putString(MonitorHandler.LOGUI,str);
		msg.setData(b);
		this.mainThreadHandler.sendMessage(msg);
	}
	
}
