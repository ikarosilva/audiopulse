package org.audiopulse.io;

import android.util.Log;

public class UsbAudioEmulator implements UsbAudioInterface{

	private static final String TAG="UsbAudioEmulator";
	private static int rFs;
	private static int pFs;
	private static int rBit;
	private static int pBit;
	private static int rCh;
	private static int pCh;
	
	public int initialize(int recfs, int playfs, int recBit,
			int playBit, int recCh, int playCh) {
		rFs=recfs;
		pFs=playfs;
		rBit=recBit;
		pBit=playBit;
		rCh=recCh;
		pCh=playCh;
		return 0;
	}

	public int playMultiTone(double[] Frequency, double[] SPL,
			double epochTime,int numberOfSweeps) throws InterruptedException {
		// Dummy driver for now just wait a few seconds and return when done
		Log.v(TAG,"playing sound for " + epochTime*numberOfSweeps + " seconds");
		Thread.sleep((long) (epochTime*numberOfSweeps*100));
		return 0;
	}

	public double getRecFs() {
		return rFs;
	}

	public double getPlayFs() {
		return pFs;
	}

	public double getRecBitLength() {
		return rBit;
	}

	public double getPlayBitLength() {
		return pBit;
	}

	public double getRecChConfig() {
		return rCh;
	}

	public double getPlayChConfig() {
		return pCh;
	}

	public void finish() {
		
	}

	public double[] getAveragedRecordedPowerSpectrum() {
		//Simulate returning of spectrum
		double[] spec=new double[rFs];
		for(int i=0;i<rFs;i++)
			spec[i]= (65 + Math.round(Math.random()*20));
		return spec;
	}

	public double[] getAveragedRecordedWaveForm() {
		double[] wave=new double[rFs];
		for(int i=0;i<rFs;i++)
			wave[i]= (Math.random()*100);	
		return wave;
	}

}
