package org.audiopulse.io;

public interface UsbAudioInterface {

	//Initialization should return  0 if sucessful and >0 if error!
	//Parameters are fetched from the XLM resource file
	public abstract int initialize(int recfs1,int playfs2,
			int recBit, int playBit,int recCh, int playCh);

	public abstract void finish();
	
	//Should we have callbacks?
	public abstract int playMultiTone(double[] Frequency,
			double[] SPL, double epochTime, int numberOfSweeps) throws InterruptedException;
	
	public abstract int[] getAveragedRecordedPowerSpectrum();
	public abstract int[] getAveragedRecordedWaveForm();

	public abstract double getRecFs();
	public abstract double getPlayFs();
	public abstract double getRecBitLength();
	public abstract double getPlayBitLength();
	public abstract double getRecChConfig();
	public abstract double getPlayChConfig();


}
