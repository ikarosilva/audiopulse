package org.audiopulse.tests;

import org.audiopulse.activities.BasicTestActivity;
import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.io.PlayRecordManager;
import org.audiopulse.io.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class TestProcedure implements Runnable {
	
	private Handler uiThreadHandler;
	private Thread testThread;
	
	protected PlayRecordManager testIO = new PlayRecordManager();
	protected AcousticConverter levels = new AcousticConverter();
	protected int playbackSampleFrequency = 44100;
	protected int recordingSampleFrequency = 44100;
	
	
	public TestProcedure (BasicTestActivity parent) {
		this.uiThreadHandler = new Handler(parent);
	}
	
	public void start() {
		testThread = new Thread(this);
		testThread.setPriority(Thread.MAX_PRIORITY);
		testThread.start();
	}
	
	public abstract void run();	
	
	//TODO: other messaging utilities
	public void logToUI(String str)
	{
		this.sendMessage(Utils.getStringAsABundle(str));
	}
	
	public void sendMessage(Bundle data) {
		Message m = this.uiThreadHandler.obtainMessage();
		m.setData(data);
		this.uiThreadHandler.sendMessage(m);
	}
	
	public Thread getThread() {
		return testThread;
	}
}
