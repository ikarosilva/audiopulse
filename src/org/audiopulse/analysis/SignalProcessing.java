package org.audiopulse.analysis;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import android.util.Log;

//Noinstantiable utility class
public class SignalProcessing {

	public static final String TAG="SignalProcessing";

	private SignalProcessing(){
		//Suppress default constructor for noninstantiability
		throw new AssertionError();
	}

	public static double rms(short[] x){
		double r = 0;
		double N = (double) x.length;
		//Calculate mean squared
		for(int i=0;i<x.length;i++){
			r += (x[i]*x[i])/N;
		}
		return Math.sqrt(r);
}

	public static double rms(double[] x) {
		double rms = 0;
		double N = (double) x.length;
		for (int n=0; n<N; n++) {
			rms += x[n]*x[n]/N; 		//warning: not rms yet!
		}
		rms = Math.sqrt(rms);
		return rms;
	}

	@Deprecated 	// as written, this is linear scaling in power (not rms) to dB (not dBu)
	public static double rms2dBU(double x){
		return 10*Math.log10(x);
	}

	//convert linear scaling to dB
	public static double lin2dB(double rms) {
		return 20*Math.log10(rms);
	}

	//convert dB scaling to linear
	public static double dB2lin(int a) {
		return dB2lin((double)a);
	}
	public static double dB2lin(double a) {
		return Math.pow(10, a/20.0);
	}

	@Deprecated //use double[][] getSpectrum(short[] x) instead (the first column is frequency indices
	public static double[] getSpectrum(short[] x){
		FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);
		//Calculate the size of averaged waveform
		//based on the maximum desired frequency for FFT analysis
		int N=x.length;
		int SPEC_N=(int) Math.pow(2,Math.floor(Math.log((int) N)/Math.log(2)));
		double[] winData=new double[SPEC_N];
		Complex[] tmpFFT=new Complex[SPEC_N];
		double[] Pxx = new double[SPEC_N];
		double tmpPxx;
		//Break FFT averaging into SPEC_N segments for averaging
		//Calculate spectrum, variation based on
		//http://www.mathworks.com/support/tech-notes/1700/1702.html

		//Perform windowing and averaging on the power spectrum
		Log.v(TAG,"Calculating FFT");
		for (int i=0; i < N; i++){
			if(i*SPEC_N+SPEC_N > N)
				break;
			for (int k=0;k<SPEC_N;k++){
				winData[k]= (double) x[i*SPEC_N + k]*SpectralWindows.hamming(k,SPEC_N);
			}
			tmpFFT=FFT.transform(winData,TransformType.FORWARD);
			for(int k=0;k<(SPEC_N/2);k++){
				tmpPxx = tmpFFT[k].abs();
				tmpPxx=2*(tmpPxx*tmpPxx)/(double)SPEC_N; //Not accurate for the DC & Nyquist, but we are not using it!
				Pxx[k]=( (i*Pxx[k]) + tmpPxx )/((double) i+1);
			}
		}

		return Pxx;
	}

	public static double[][] getSpectrum(short[] x, double Fs, int SPEC_N){
		double[] y= new double[x.length];
		for(int i=0;i<x.length;i++){
			y[i]=(double) x[i]/(Short.MAX_VALUE+1);
		}	
		return getSpectrum(y,Fs,SPEC_N);
	}

	public static double[][] getSpectrum(double[] x, double Fs, int SPEC_N){
		FastFourierTransformer FFT = new 
				FastFourierTransformer(DftNormalization.STANDARD);
		//Calculate the size of averaged waveform
		//based on the maximum desired frequency for FFT analysis

		//Calculate the number of sweeps given the epoch time
		int sweeps=Math.round(x.length/SPEC_N);
		double[] winData=new double[SPEC_N];
		Complex[] tmpFFT=new Complex[SPEC_N];
		double[][] Axx = new double[2][SPEC_N/2];
		double SpectrumResolution = Fs/SPEC_N;
		double scaleFactor=2.0/Axx[0].length; //2.0 for negative frequencies
		//Break FFT averaging into SPEC_N segments for averaging
		//Perform windowing and running average on the amplitude spectrum
		//averaging is done by filling a buffer (windData) of size SPECN_N at offset i*SPEC_N
		//until the end of the data.
		for (int i=0; i < sweeps; i++){
			if(i*SPEC_N+SPEC_N > x.length)
				break;
			for (int k=0;k<SPEC_N;k++){
				winData[k]= ((double)x[i*SPEC_N + k])*SpectralWindows.hamming(k,SPEC_N);
			}
			tmpFFT=FFT.transform(winData,TransformType.FORWARD);
			for(int k=0;k<(SPEC_N/2);k++){
				Axx[1][k]=( (i*Axx[1][k]) + tmpFFT[k].abs()*scaleFactor )
						/((double) i+1.0); //averaging
			}
		}

		//Convert to dB
		for(int i=0;i<Axx[0].length;i++){
			Axx[0][i]=SpectrumResolution*i;
			//Convert to dB relative to 32 bit word length 
			Axx[1][i]=Math.log10((double)Axx[1][i]/Long.MAX_VALUE);
		}
		return Axx;
	}

	public static boolean isclipped(short[] rawData, double Fs) {
		// TODO: Crude method to detect clipping of the waveform by 
		//using a moving window and checking if all samples in that window
		//are the same value. We are essentially checking if the signal 
		//has any "flat-regions" of any sort (the playback can be clipped 
		//while the recording can still be ok).

		double winSize=0.01; //window size in milliseconds
		int window=(int) Math.round(Fs*winSize);
		double sum=0;
		double lastSample=0;
		double currentSample=0;
		boolean clipped=false;
		for(int i=0;i<rawData.length;i++){
			currentSample=Math.abs(rawData[i]);
			if(i> (window-1)){
				lastSample=Math.abs(rawData[i-window]);
				sum-=lastSample;
				sum+=currentSample;
				//TODO : Maybe allow for some uncertainty around 1 because
				//play back can be clipped but rec noise may mask some of it.
				if(sum/(currentSample*window) == 1){
					clipped=true;
					break;
				}
			}else {
				//Initial transient stage, filling the filter
				sum+=currentSample;
			}
		}
		return clipped;
	}

}
