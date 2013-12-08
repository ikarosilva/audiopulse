package org.audiopulse.analysis;

public class DPOAEResults {

	//AudioPulseDataAnalyzer  interface
	public final double respSPL;
	public final double noiseSPL;
	public final double stim1SPL;
	public final double stim2SPL;
	
	public final double respHz;
	public final double stim1Hz;
	public final double stim2Hz;
	
	public final double[][] dataFFT;
	public final String fileName;
	public final String protocol;
	
	public DPOAEResults(double respSPL,double noiseSPL,double stim1SPL, double stim2SPL, double respHz, 
						double stim1Hz,double stim2Hz,double[][] dataFFT, String fileName,String protocol){
		
		this.respSPL=respSPL;
		this.noiseSPL=noiseSPL;
		this.stim1SPL=stim1SPL;
		this.stim2SPL=stim2SPL;
		
		this.respHz=respHz;
		this.stim1Hz=stim1Hz;
		this.stim2Hz=stim2Hz;
		
		this.dataFFT=dataFFT;
		this.fileName=fileName;
		this.protocol=protocol;
	
	}

}
