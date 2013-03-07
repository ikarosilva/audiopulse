package org.audiopulse.tests;

public interface AudioPulseDataAnalyzer {

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
