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
		this.wait((long) epochTime*numberOfSweeps);
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

	public void finish() {
		
	}

	public int[] getAveragedRecordedPowerSpectrum() {
		//Simulate returning of spectrum
		int[] spec=new int[rFs];
		for(int i=0;i<rFs;i++)
			spec[i]= (int) (Math.random()*100);
				
		return spec;
	}

	public int[] getAveragedRecordedWaveForm() {
		int[] spec=new int[rFs];
		for(int i=0;i<rFs;i++)
			spec[i]= (int) (Math.random()*100);	
		return spec;
	}

}
