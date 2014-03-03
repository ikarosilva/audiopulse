package org.audiopulse.activities;

import org.audiopulse.hardware.APulseIface;

import android.os.Bundle;
import android.os.Message;

public class MonitorThread extends Thread
{
	static final String TAG = "MonitorThread";
	private final APulseIface apulse;
	MonitorHandler mainThreadHandler = null;
	int count = 0;
	private final short[] f1;
	private final short[] f2;
	public MonitorThread(MonitorHandler h, APulseIface ap, short[] f1, short[] f2)
	{
		mainThreadHandler = h;
		apulse=ap;
		this.f1=f1;
		this.f2=f2;
	}

	@Override
	public void run()
	{
		for(int i=0;i<f1.length;i++){
			//Set frequency to test
			mainThreadHandler.setCurrentFrequency(f1[i],f2[i]);		
			//Start test and monitor until complete
			sendAction(MonitorHandler.Messages.TEST_FREQUENCY); //Test is run through the Handler
			monitorOneTest();
		}
	}

	private void monitorOneTest(){
		
		//Reset Driver and loop
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
			sendMessage("Initial driver state is  in RESET!\n");
			break;
		}
		while(isRunning){
			if(apulse.getStatus().test_state == APulseIface.APulseStatus.TEST_DONE){
				isRunning=false;
				sendAction(MonitorHandler.Messages.RECORDING_COMPLETE);
				while(mainThreadHandler.dataIsReady == false){
					monitorSleep();
				}
			}
			monitorSleep();
		}
	}


	private synchronized void monitorSleep(){
		try {
			Thread.sleep(200);
		}catch (InterruptedException e) {
			e.printStackTrace();
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
