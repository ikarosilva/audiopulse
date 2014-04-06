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
			sendMessage("\n\n*** Running f1=" + f1[i] + " f2= " + f2[i]);
			mainThreadHandler.setCurrentFrequency(f1[i],f2[i]);		
			//Start test and monitor until complete
			sendAction(MonitorHandler.Messages.TEST_FREQUENCY); //Test is run through the Handler
			monitorOneTest();
			sendMessage("\nTest for :" + f1[i] + " completed!");
		}
		sendMessage("\n\nFinished all - " + f1.length + " - tests!");
	}

	private void monitorOneTest(){

		//Reset Driver and loop
		int init = apulse.getStatus().test_state;
		//TODO: Make wait time dependent on recording parameters!
		long maxWaitTime=10000;//Maximum waiting period to wait for the data (in milliseconds)
		switch (init) {
		case APulseIface.APulseStatus.TEST_CONFIGURING:
			sendMessage("\nInitial driver state is: CONFIGURING.Waiting for main activity to start test.");
			break;
		case APulseIface.APulseStatus.TEST_READY:
			sendMessage("\nInitial driver state is: READY. Waiting for main activity to start test...");
			break;
		case APulseIface.APulseStatus.TEST_RESET:
			sendMessage("\nInitial driver state is: RESET!");
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
				break;
			}
		monitorSleep();
	}
		sendMessage("\nExiting monitor test");
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
