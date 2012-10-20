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
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.utilities;


public class WhiteNoise {


	public static final String TAG="WhiteNoise";
	public double clickDuration;
	public short amplitude;
	private short[] data;
	public int N;
	public double Fs; //Sampling frequency in Hz

	public WhiteNoise(int N,double Fs){
		//Duration variables are defined in terms of second
		this.N=N;
		this.Fs=Fs;
		data=new short[N];
		amplitude=Short.MAX_VALUE;
	}

	public short[] generateWhiteNoise(){
		for( int i = 0; i < N; i++ )
		{
			data[i]=(short) (amplitude*Math.random());
		}
		return data;
	}

} //of Class definition
