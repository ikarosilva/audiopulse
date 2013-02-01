package org.audiopulse.tests;

import org.audiopulse.io.PlayRecordManager;
import org.audiopulse.io.Utils;

import android.os.Handler;
import android.os.Message;

public abstract class TestProcedure implements Runnable {
	protected PlayRecordManager testIO;
	private Handler uiThreadHandler;
	
	public TestProcedure (Handler handler) {
		this.uiThreadHandler = handler;
	}
	
	public void informUI(String str)
	{
		Message m = this.uiThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.uiThreadHandler.sendMessage(m);
	}
	
	public abstract void run();	
	
}
