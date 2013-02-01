package org.audiopulse.tests;

import org.audiopulse.activities.BasicTestActivity;
import org.audiopulse.io.PlayRecordManager;
import org.audiopulse.io.Utils;

import android.os.Handler;
import android.os.Message;

public abstract class TestProcedure implements Runnable {
	protected PlayRecordManager testIO = new PlayRecordManager();
	private Handler uiThreadHandler;
	
	public TestProcedure (BasicTestActivity parent) {
		this.uiThreadHandler = new Handler(parent);
	}
	
	public void informUI(String str)
	{
		Message m = this.uiThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.uiThreadHandler.sendMessage(m);
	}
	
	public abstract void run();	
	
}
