package org.audiopulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MonitorThread extends Thread
{
	Handler mainThreadHandler = null;
	int count = 0;
	public MonitorThread(Handler h)
	{
		mainThreadHandler = h;
	}
	public static String TAG = "MonitorThread";
	
	@Override
	public void run()
	{
		Log.v(TAG,"Starting thread task");
		Message m = this.mainThreadHandler.obtainMessage();
		Log.v(TAG,"obtained message");
		Bundle b = new Bundle();
		b.putString("logUI","started thread");
		m.setData(b);
		Log.v(TAG,"sending first update");
		this.mainThreadHandler.sendMessage(m);
		Log.v(TAG,"started execution");
		try {
			Thread.sleep(2000);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.v(TAG,"obtaining second message");
		Message m2 = this.mainThreadHandler.obtainMessage();
		Bundle b2 = new Bundle();
		b2.putString("logUI","thread finished sucessfuly");
		m2.setData(b2);
		Log.v(TAG,"sending second update");
		this.mainThreadHandler.sendMessage(m2);

	}
	
}
