package org.audiopulse.activities;

import org.audiopulse.hardware.APulseIface;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MonitorThread extends Thread
{
	static final String TAG = "MonitorThread";
	private final APulseIface apulse;
	MonitorHandler mainThreadHandler = null;
	int count = 0;
	public MonitorThread(MonitorHandler h, APulseIface ap)
	{
		mainThreadHandler = h;
		apulse=ap;
	}

	@Override
	public void run()
	{
		int init = apulse.getStatus().test_state;
		boolean isRunning=true;
		switch (init) {
		case APulseIface.APulseStatus.TEST_CONFIGURING:
			sendMessage("Initial driver state is CONFIGURING, waiting for main activity to start test.\n");
			break;
		case APulseIface.APulseStatus.TEST_READY:
			sendMessage("Initial driver state is READY, waiting for main activity to start test...\n");
			break;
		case APulseIface.APulseStatus.TEST_RESET:
			//Not sure what to do in a RESET state. For now expect the worse and assume it is 
			//not possible to monitor the test
			sendMessage("Initial driver state is in unexpected state: RESET, exiting monitor!\n");
			isRunning=false;
			break;
		}

		while(isRunning){
			if(apulse.getStatus().test_state == APulseIface.APulseStatus.TEST_DONE){
				isRunning=false;
				sendMessage("Frequency test finished, getting data...\n");
				sendAction(MonitorHandler.Messages.RECORDING_COMPLETE);
				while(mainThreadHandler.dataIsReady == false){
					try {
						Thread.sleep(200);
					}catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				sendMessage("Analyzing data...");
			}
			try {
				Thread.sleep(200);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		
	}

	private synchronized void sendMessage(String str){
		Message msg = this.mainThreadHandler.obtainMessage(MonitorHandler.Messages.LOG);
		Bundle b = new Bundle();
		b.putString(MonitorHandler.LOGUI,str);
		msg.setData(b);
		this.mainThreadHandler.sendMessage(msg);
	}
	
	private synchronized void sendAction(int action){
		Message msg = this.mainThreadHandler.obtainMessage(action);
		this.mainThreadHandler.sendMessage(msg);
	}

}
