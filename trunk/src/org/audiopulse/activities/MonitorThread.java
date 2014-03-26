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
		sendMessage("f1.length: " + f1.length );
		for(int i=0;i<f1.length;i++){
			//Set frequency to test
			mainThreadHandler.setCurrentFrequency(f1[i],f2[i]);		
			//Start test and monitor until complete
			sendAction(MonitorHandler.Messages.TEST_FREQUENCY); //Test is run through the Handler
			//TODO: Figue out why we are stuck here in one loop only!
			monitorOneTest();
		}
	}

	private void monitorOneTest(){

		//Reset Driver and loop
		int init = apulse.getStatus().test_state;
		long maxWaitTime=10000;//Maximum waiting period to wait for the data (in milliseconds)
		switch (init) {
		case APulseIface.APulseStatus.TEST_CONFIGURING:
			sendMessage("Initial driver state is: CONFIGURING.Waiting for main activity to start test.\n");
			break;
		case APulseIface.APulseStatus.TEST_READY:
			sendMessage("Initial driver state is: READY. Waiting for main activity to start test...\n");
			break;
		case APulseIface.APulseStatus.TEST_RESET:
			sendMessage("Initial driver state is: RESET!\n");
			break;
		}
		sendMessage("\nWaiting for test to complete");
		int stat;
		while(true){
			stat=apulse.getStatus().test_state;
			if( stat== APulseIface.APulseStatus.TEST_DONE){
				sendMessage("\nTest completed, fetching data...");
				sendAction(MonitorHandler.Messages.RECORDING_COMPLETE);
				while(mainThreadHandler.dataIsReady == false){
					monitorSleep();
					sendMessage("\nWaiting for data...");
				}
				//Test complete and data has been collected. Exit loop
				sendMessage("\nexiting test...");
				break;
			}
		sendMessage("\nsetting thread to sleep");
		monitorSleep();
	}
		sendMessage("\nexiting monitor test");
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
