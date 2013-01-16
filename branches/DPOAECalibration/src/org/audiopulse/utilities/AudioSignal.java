/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */
package org.audiopulse.utilities;

import android.util.Log;

// Useful functions for converting audio signals between double (for math) and short (for playback)

//TODO: bi-directional! For recorded shorts, convert to doubles.
public class AudioSignal {
	public final static String TAG="AudioSignal";
	
	
	//get short vector for playback buffer from stereo input
	//mono or stereo interleaved output
	public static short[] getAudioTrackData(double[][] doubleData, boolean stereoOutput) {
		//TODO: check 2-channel, same length
		
		short[] bufferData;
		if (stereoOutput) { //return stereo signal: interleave
			bufferData = interleave(convertToShort(doubleData));
		} else { //return mono signal
			bufferData = convertToShort(convertToMono(doubleData));
		}
		
		return bufferData;
	}

	//get short vector for playback buffer from mono input
	//mono or stereo interleaved output
	public static short[] getAudioTrackData(double[] doubleData, boolean stereoOutput) {
		//TODO: check 2-channel, same length
		
		short[] bufferData;
		if (stereoOutput) { //return stereo signal: interleave duplicate
			short[] shortVector = convertToShort(doubleData);
			bufferData = interleave(shortVector,shortVector);
		} else { //return mono signal
			bufferData = convertToShort(doubleData);
		}
		
		return bufferData;
	}
	

	//convert double vector to short, scale appropriately.
	private static short[] convertToShort(double[] doubleVector) {
		short[] shortVector = new short [doubleVector.length];
		for (int n=0;n<doubleVector.length;n++) {
			shortVector[n] = convertToShort(doubleVector[n]);
		}
		return shortVector;
	}
	private static short[][] convertToShort(double[][] doubleVector) {
		//TODO: check 2-channel, same length
		short[][] shortVector = new short [doubleVector.length][doubleVector[0].length];
		for (int chan=0; chan<=doubleVector.length;chan++)
			for (int n=0;n<doubleVector[0].length;n++) {
				shortVector[chan][n] = convertToShort(doubleVector[chan][n]);
		}
		return shortVector;
	}
	
	//scale and convert a single sample to short.
	private static short convertToShort(double sample) {
		if(Math.abs(sample)>1){	
			Log.w(TAG,"Digital (short) audio signal is being clipped!!");
			return (short) (Short.MAX_VALUE * (Math.signum(sample)));
		}else{
			return (short) (Short.MAX_VALUE * (sample));
		}
	}
	
	//interleave 2xN data into 1x2N vector
	private static short[] interleave(short[][] data) {
		return interleave(data[0],data[1]);
	}
	
	//interleave left and right vectors into stereo interleaved
	//left and right should be equal length (truncates if not)
	private static short[] interleave(short[] left, short[] right) {
		int N = Math.min(left.length, right.length);  //safe handling: choose minimum length. Really they should be equal.
		short[] playBuffer = new short [2*N];
		for (int n=0; n<N; n++) {
			playBuffer[2*n] = left[n];
			playBuffer[2*n+1] = right[n];
		}
		return playBuffer;
	}
	
	public static double[] convertToMono(double[][] stereoData) {
		//TODO: check 2-channel, same length
		
		int N = stereoData[0].length;
		double[] monoSignal = new double [N];

		for (int n=0;n<N; n++) {
			monoSignal[n] += 1/2 * (stereoData[0][n] + stereoData[1][n]);
		}
		
		return monoSignal;
	
	}
	
}
