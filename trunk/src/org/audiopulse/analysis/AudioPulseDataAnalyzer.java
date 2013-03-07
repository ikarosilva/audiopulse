package org.audiopulse.analysis;

public interface AudioPulseDataAnalyzer {

	
	    /// -- Define Mapping of Keys to HashMap 
	    //The purpose of these keys are to define constants that will be accessible by
		//different packages and clients analyzing the data in the Maps returned 
		// by the AudioPulseDataAnalyer interface.
		//Not sure if this is yet the best way, expect changes...	
		public static final String RESPONSE_2KHZ="resp2kHz";
		public static final String RESPONSE_3KHZ="resp3kHz";
		public static final String RESPONSE_4KHZ="resp4kHz";
		public static final String NOISE_2KHZ="noise2kHz";
		public static final String NOISE_3KHZ="noise3kHz";
		public static final String NOISE_4KHZ="noise4kHz";
		public static final String STIM_2KHZ="stim2kHz";
		public static final String STIM_3KHZ="stim3kHz";
		public static final String STIm_4KHZ="stim4kHz";
		
	//Some methods have the option to do analysis in either time or spectrum
	//domain. Return NaN for methods that wont implement
	//The spetrum is a 2D array where the first dimension is the index of frequency
	//in Hz
	
	//Get the response for a specific frequency
	double getResponseLevel(short rawdata[],double frequency, double Fs);
	double getResponseLevel(double dataFFT[][],double frequency);
	
	//Get the noise floor for a specific frequency
	double getNoiseLevel(short rawdata[],double frequency, double Fs);
	double getNoiseLevel(double dataFFT[][],double frequency);
	
	//Get estimated stimulus level for a specific frequency
	double getStimulusLevel(short rawdata[],double frequency, double Fs);
	double getStimulusLevel(double dataFFT[][],double frequency);
	
}
