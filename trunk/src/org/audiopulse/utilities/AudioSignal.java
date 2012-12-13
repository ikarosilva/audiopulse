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
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */
package org.audiopulse.utilities;

import android.util.Log;

//AudioSignal: provides a wrapper to to double precision operations on audio data that are accessed as shorts.
//use audiosignal.getDataAsShort() to get an array of shorts for playback
//double values between +/-1 are mapped to shorts, values outside this range are clipped.
//
// Operates like an array of primitives. The size is declared upon intiialization
// and cannot be changed (instead, create a new AudioSignal).

public class AudioSignal implements Cloneable {
	private final double[][] data;
	
	public final int numChannels;
	public final int length;
	public final boolean isStereo;
	public final static String TAG="AudioSignal";
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	
	private final int LEFT_CHANNEL = 0;
	private final int RIGHT_CHANNEL;
	
	
	
	//default constructor: stereo signal
	public AudioSignal(int length) {
		this.length = length;
		this.numChannels = 2;
		this.isStereo = true;
		this.RIGHT_CHANNEL = 1;
		this.data = new double [this.numChannels][this.length];
		this.initializeData();
	}
	
	//constructor: mono or stereo
	public AudioSignal(int length, boolean stereo) {
		this.length = length;
		if (stereo) {
			this.numChannels = 2;
			this.isStereo = true;
			this.RIGHT_CHANNEL = 1;
		} else {
			this.numChannels = 1;
			this.isStereo = false;
			this.RIGHT_CHANNEL = 0;
		}
		this.data = new double [this.numChannels][this.length];
		this.initializeData();
	}
	
	//get playback signal, convert to mono or stereo as needed
	public short[] getPlaybackSignal(boolean stereoFlag) {
		short[] playBuffer;

		if (stereoFlag) { //return stereo signal: interleave
			short[][] shortData = this.getDataAsShort();
			playBuffer = AudioSignal.interleave(shortData);
		} else { //return mono signal
			playBuffer = AudioSignal.convertToShort(this.getMono());

		}
		
		return playBuffer;
	}
	

	public double[] getChannel(int n) {
		//return cloned array, not reference. Java is weird.
		return data[n].clone();
	}
		
	public double[][] getData() {
		//clone each channel rather than passing back references to each channel. Java is weird.
		double[][] returnData = new double[this.numChannels][this.length];
		for (int chan=0; chan<this.numChannels; chan++) {
			returnData[chan] = this.getChannel(chan); 	// gets cloned channel
		}
		return returnData;
	}

	//return single channel data as short. Convert +/-1 to +/-Short.MAX_VALUE.
	public short[] getChannelAsShort(int chan) {
		return AudioSignal.convertToShort(data[chan]);
	}

	//return all channel data as short. Convert +/-1 to +/-Short.MAX_VALUE.
	public short[][] getDataAsShort() {
		short[][] buffer = new short[this.numChannels][this.length];
		for (int chan=0;chan<this.numChannels;chan++) {
			buffer[chan] = this.getChannelAsShort(chan);
		}
		return buffer;
	}
	
	//set channel data (clone array).
	public void setChannel(int chan, double[] newData) {
		data[chan] = newData.clone();
	}
	
	//set all data at once (clone channel arrays)
	public void setData(double[][] newData) {
		for (int chan=0;chan<this.numChannels;chan++) {
			this.setChannel(chan,newData[chan]);
		}
	}
	
	//create a copy of the data structure
	public AudioSignal clone() {
		AudioSignal returnSignal = new AudioSignal(this.length, this.isStereo) ;
		returnSignal.setData(this.data);
		return returnSignal;
	}
	
	//returns a disease that makes you sleepy.
	public double[] getMono(){
		double[] monoSignal = new double [this.length];
		monoSignal = this.getChannel(LEFT_CHANNEL);
		//if stereo, average with right channel, else, return as is.
		if (this.isStereo) {
			for (int n=0;n<this.length; n++) {
				monoSignal[n] += this.data[RIGHT_CHANNEL][n];
				monoSignal[n] /= 2;
			}
		} 
		
		return monoSignal;
	}
		

	
	private void initializeData() {
		for (int chan=0;chan<this.numChannels;chan++)
			for (int n=0;n<this.length;n++)
				this.data[chan][n] = 0;
	}

	
	/*--- static functions ---*/
	
	public static short[] convertToShort(double[] doubleVector) {
		short[] shortVector = new short [doubleVector.length];
		for (int n=0;n<doubleVector.length;n++) {
			double sample = doubleVector[n];
			if(Math.abs(sample)>1){	
				shortVector[n] = (short) (Short.MAX_VALUE * (Math.signum(sample)));
				Log.w(TAG,"Digital (short) audio signal is being clipped!!");
			}else{
				shortVector[n] = (short) (Short.MAX_VALUE * (sample));
			}
		}
		return shortVector;

	}
	
	//interleave 2xN data into 1x2N vector
	public static short[] interleave(short[][] data) {
		return interleave(data[0],data[1]);
	}
	
	//interleave left and right vectors into stereo interleaved
	//left and right should be equal length (truncates if not)
	public static short[] interleave(short[] left, short[] right) {
		int N = Math.min(left.length, right.length);  //safe handling: choose minimum length. Really they should be equal.
		short[] playBuffer = new short [2*N];
		for (int n=0; n<N; n++) {
			playBuffer[2*n] = left[n];
			playBuffer[2*n+1] = right[n];
		}
		return playBuffer;
	}
}
