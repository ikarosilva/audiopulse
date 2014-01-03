package org.audiopulse.io;

public interface UsbAudioInterface {

	/*
	 * Parameters and their units
	 * 
	 * recfs1 - Recording sampling frequency in kHz
	 * playfs2 - playback sampling frequency in kHz
	 * recBit - recording bit resolution 
	 * playBit - playback bit resolytion 
	 * recCh - recording channel configuration 0 - mono, 1 -stereo
	 * playCh - playback channel configuration 0- mono 1-stereo
	 * 
	 * numberOfSweep - number of stimulus presentation (epochs) to be averaged (integer)
	 * epochTime - stimulus duration in seconds
	 */
	
	
	
	//Initialization should return  0 if successful and >0 if error!
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
