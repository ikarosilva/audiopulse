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

package org.audiopulse.hardware;

public class AcousticDevice {

	// This class should only be used by the MobilePhone class (you should be able 
	// to access all information in here from within the MobilePhone Device.
	
	private int minAttenuation; //in dB !!
	private ioDevice device;
	
	public static enum ioDevice {
		
		ER10C_40dBGain(72,0.000001,"ER10C_40dBGain"),
		Dummy(69,0.000001,"Dummy");
		
		private double out1Volt2SPL; //maps driving voltage (1V peak-to-peak) to SPL at transducer
		private double in0SPL2Volt;  //maps 0 dB SLP to voltage at the recording 
		private String ioDeviceName;
		
		ioDevice(double out1Volt2SPL,double in0SPL2Volt, String ioDeviceName ) {
			//Convert attenuation in dB relative to the maximum track level
			this.out1Volt2SPL=out1Volt2SPL; 
			this.in0SPL2Volt=in0SPL2Volt;
			this.ioDeviceName=ioDeviceName;
			
		}		
		
		public String getioDeviceName(){
			return ioDeviceName;
		}
		
		public double getin0SPL2Volt(){
			return in0SPL2Volt;
		}
		
		public double getoutout1Volt2SPL(){
			return out1Volt2SPL;
		}
		
	}
	
	public AcousticDevice(AcousticDevice.ioDevice device,int minAttenuation){
		this.device=device;
		this.minAttenuation=minAttenuation;
	}
	public void setminAttenuation(int minAttenuation){
		this.minAttenuation=minAttenuation;
	}
	
	public double getMaxBitPeak(){
		return Short.MAX_VALUE*Math.pow(10,(double)(-minAttenuation)/20); 
	}
	
	public AcousticDevice.ioDevice getioDevcie(){
		return this.device;
	}
	
}
