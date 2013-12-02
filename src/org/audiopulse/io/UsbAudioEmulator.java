package org.audiopulse.io;

public class UsbAudioEmulator implements UsbAudioInterface{

	private static int rFs;
	private static int pFs;
	private static int rBit;
	private static int pBit;
	private static int rCh;
	private static int pCh;
	
	public int initialize(int recfs, int playfs, int recBit,
			int playBit, int recCh, int playCh) {
		// TODO Auto-generated method stub
		rFs=recfs;
		pFs=playfs;
		rBit=recBit;
		pBit=playBit;
		rCh=recCh;
		pCh=playCh;
		return 0;
	}

	public int playMultiTone(double[] Frequency, double[] SPL,
			double milliseconds) throws InterruptedException {
		// Dummy driver for now just wait a few seconds and return when done
		this.wait((long) milliseconds);
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

	public double gtePlayChConfig() {
		return pCh;
	}

}
