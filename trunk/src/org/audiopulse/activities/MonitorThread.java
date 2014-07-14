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
		sendMessage("Number of tests (f1.length): " + f1.length );
		for(int i=0;i<f1.length;i++){
			//Reset driver in between tests
			resetDriver();
			//Set frequency to test
			mainThreadHandler.setCurrentFrequency(f1[i],f2[i]);		
			//Start test and monitor until complete
			sendAction(MonitorHandler.Messages.TEST_FREQUENCY); //Test is run through the Handler
			//This looks like there is some thread contigency issues here
			monitorOneTest();
			sendAction(MonitorHandler.Messages.RECORDING_COMPLETE);
			while(mainThreadHandler.dataIsReady == false){
				monitorSleep();
				sendMessage("\nWaiting for data...");
			}
			apulse.reset();
			sendMessage("\nAnalysis for test:" + f1[i] + " finished.\n\n");
		}
		sendMessage("\n\n***Finished all - " + f1.length + " - tests!");
	}

	public void resetDriver(){
		//Reset driver to make sure it is in an original state
		apulse.reset();
		while(true){
			int stat=apulse.getStatus().test_state;
			if( stat== APulseIface.APulseStatus.TEST_RESET){
				break;
			}
			sendMessage("\nWaiting for test to be reset...");
			monitorSleep();
		}
	}


	private void monitorOneTest(){
		//TODO: Make wait time dependent on recording parameter time!
		int stat;
		while(true){
			stat=apulse.getStatus().test_state;
			if( stat== APulseIface.APulseStatus.TEST_DONE){
				break;
			}
			monitorSleep();
		}
	}


	private synchronized void monitorSleep(){
		try {
			Thread.sleep(500);
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
