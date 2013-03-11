package org.audiopulse.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public interface AudioPulseDataAnalyzer extends Callable<HashMap<String,Double>> {
	
	/// -- Define Mapping of Keys to HashMap 
	//The purpose of these keys are to define constants that will be accessible by
	//different packages and clients analyzing the data in the Maps returned 
	// by the AudioPulseDataAnalyer interface.
	//Not sure if this is yet the best way, expect changes...
	public static final String TestType="TestType"; //Encodes Test Type
	public static final String RESPONSE_2KHZ="resp2kHz";
	public static final String RESPONSE_3KHZ="resp3kHz";
	public static final String RESPONSE_4KHZ="resp4kHz";
	public static final String NOISE_2KHZ="noise2kHz";
	public static final String NOISE_3KHZ="noise3kHz";
	public static final String NOISE_4KHZ="noise4kHz";
	public static final String STIM_2KHZ="stim2kHz";
	public static final String STIM_3KHZ="stim3kHz";
	public static final String STIM_4KHZ="stim4kHz";
	public static final String Results_MAP="AudioPulseDataAnalyzerMap";

	
	//Implementations of the interface should store all the data file names  
	//used in the analysis and that should be packaged in this Set
	public Set<String> getRawDataFileNames();
	
	//This map is provided for convenience to methods wishing to iterator through
	//all the keys
	public static final Set<String> responseKeys = new HashSet<String>() {{  
		add(RESPONSE_2KHZ); add(RESPONSE_3KHZ); add(RESPONSE_4KHZ);  
	}}; 
	
	public static final Set<String> noiseKeys = new HashSet<String>() {{  
		add(NOISE_2KHZ); add(NOISE_3KHZ); add(NOISE_4KHZ);  
	}};	

	public static final Set<String> stimKeys = new HashSet<String>() {{  
		add(STIM_2KHZ); add(STIM_3KHZ); add(STIM_4KHZ);  
	}};
	
	//Use for decoding Test Type- Test name is encoded by the sequential order
	//(ie, TEOAE=1 , DPOAE=2, etc...because the result map is <String,Double>
	//So to encode the test type in from a procedure in the results hashmap use:
	// map.put(TestType,1) //for TEOAE Types
	public static final ArrayList<String> testName= new ArrayList<String>(){{  
		add("TEOAE"); add("DPOAE"); add("SEOAE");  
	}};
	
	public static final HashMap<String,Double>  frequencyMapping =new HashMap<String,Double>(){{  
		put(RESPONSE_2KHZ,(double) 2); put(RESPONSE_3KHZ,(double) 3); put(RESPONSE_4KHZ,(double) 4); 
		put(NOISE_2KHZ,(double) 2); put(NOISE_3KHZ,(double) 3); put(NOISE_4KHZ,(double) 4);
		put(STIM_2KHZ,(double) 2); put(STIM_3KHZ,(double) 3); put(STIM_4KHZ,(double) 4);
	}};
	
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
