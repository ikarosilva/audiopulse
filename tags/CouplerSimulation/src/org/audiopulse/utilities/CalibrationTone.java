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

import org.audiopulse.io.MobilePhone;

/*
 * CALIBRATION ONLY CLASS, USED FOR INITIAL MEASUREMENTS.
 * 
 * This class should be used only to calibrate phones for the first time.
 * The calibration should be done by modifying the minimum attenuation value
 * on the class that inherits from MobilePhone. This minimum attenuation value
 * is also device specific and should be modified in the ENUM type for that device (deviceAttn).
 * 
 * For an example or template to modify to your own phone see the class LGVM670.
 * 
 */


public class CalibrationTone extends PeriodicSeries {

	public CalibrationTone(int N, double Fs, MobilePhone phone, int channelConfig){
		//Call constructor on PeriodicSeries
		super(N,Fs,phone.getCalFreq(),phone.getMaxAmplitude(),channelConfig);
	}
	
}
