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
	public final static String TAG="AudioSignal";
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	
	
	//default constructor: stereo signal
	public AudioSignal(int length) {
		this.length = length;
		this.numChannels = 2;
		this.data = new double [this.numChannels][this.length];
		this.initializeData();
	}
	
	//constructor: n-channels
	public AudioSignal(int length, int numChannels) {
		this.length = length;
		this.numChannels = numChannels;
		this.data = new double [this.numChannels][this.length];
		this.initializeData();
	}
	
	private void initializeData() {
		for (int chan=0;chan<this.numChannels;chan++)
			for (int n=0;n<this.length;n++)
				this.data[chan][n] = 0;
	}
	

	public double[] getChannel(int n) {
		//return cloned array, not reference. Java is weird.
		return data[n].clone();
	}
		
	public double[][] getData() {
		//clone each channel rather than passing back references to each channel. Java is weird.
		double[][] returnData = new double[this.numChannels][data[0].length];
		for (int chan=0; chan<this.numChannels; chan++) {
			returnData[chan] = this.getChannel(chan); 	// gets cloned channel
		}
		return returnData;
	}

	//return single channel data as short. Convert +/-1 to +/-Short.MAX_VALUE.
	public short[] getChannelAsShort(int chan) {
		short[] buffer = new short[data[chan].length];
		for (int n=0;n<data.length;n++) {
			double sample = data[chan][n];
			//clip all values outside of +/-1
			if(Math.abs(sample)>1){	
				buffer[n] = (short) (Short.MAX_VALUE * (Math.signum(sample)));
				Log.w(TAG,"Digital (short) audio signal is being clipped!!");
			}else{
				buffer[n] = (short) (Short.MAX_VALUE * (sample));
			}
		}
		return buffer;
	}

	//return all channel data as short. Convert +/-1 to +/-Short.MAX_VALUE.
	public short[][] getDataAsShort() {
		short[][] buffer = new short[data.length][data[0].length];
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
	
	//create a copy of the data
	public AudioSignal clone() {
		AudioSignal returnSignal = new AudioSignal(this.numChannels, this.length) ;
		returnSignal.setData(this.data);
		return returnSignal;
	}
	
	//returns a disease that makes you sleepy.
	public double[] getMono(){
		double[] monoSignal = new double [this.length];
		monoSignal = this.data[0].clone();
		for (int chan=1; chan<this.numChannels; chan++) {
			for (int n=0;n<this.length; n++) {
				monoSignal[n] += this.data[chan][n];
			}
		}
		return monoSignal;
	}
	
	//return mono signal for playback: add all channels, clip values outside of +/- 1.
	public double[] getMonoAsShort(){
		double[] monoSignal = new double [this.length];
		monoSignal = this.data[0].clone();
		for (int chan=1; chan<this.numChannels; chan++) {
			for (int n=0;n<this.length; n++) {
				monoSignal[n] += this.data[chan][n];
			}
		}
		for (int n=0;n<monoSignal.length;n++) {
			double sample = monoSignal[n];
			boolean clip = Math.abs(sample)>1;		//clip all values outside of +/-1
			monoSignal[n] = (short) (Short.MAX_VALUE * (clip?Math.signum(sample):sample)); 
		}

		return monoSignal;
	}
	
}
