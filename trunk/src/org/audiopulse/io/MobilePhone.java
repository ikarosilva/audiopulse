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

/*
 * Class that contains calibration parameters for the different devices to be used with AudioPulse
 * 
 */

package org.audiopulse.io;


/*
Full 16-bit range = 1V pp = 69 dB SPL
10 mV pp input = max input range, 
record yourself blowing into the microphone to confirm that this 
takes up the full bit range of input values in the input buffer, or adjust accordingly.
0dB DPL = 1uV rms (for 0 dB gain)
 */

/*
		This class should have an ENUM for each phone. In the enum
	 	there should be a constructor for each supported acoustic device
		and phone-device specific configurations.
		Follow the example of LGVM670 to add devices to this class
	
		The Dummy device is the phone loudspeaker and mic. Can be useful for 
		free-field testing/debugging without having to connect to a specific audiological 
		device.
*/

public abstract class MobilePhone {
	
	public abstract double getMaxAmplitude(); //maximum normalized amplitude that avoids clipping
											  // of a tone at CalFreq
	public abstract int getMinAttenuation();  //Minium attenuation necessary to avoid clipping in dB
	public abstract int getCalFreq(); //Frequency used for calibratio, in Hz
	public abstract String getName(); 
	//Acoustic device apparatus supported by the phone
	public abstract AcousticDevice.ioDevice getAcousticDevice();
	
	//Equation used to calculate MaxAmplitude from MinAttenuation
	public static double getMaxAmp(int minAttenuation){
	  double amp = Short.MAX_VALUE*Math.pow(10,(double)(-minAttenuation)/20);
	  return amp;
	}
	
}













