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

public class AcousticDevice {

	// This class should only be used by the MobilePhone class (you should be able 
	// to access all information in here from within the MobilePhone Device.
	
	private int minAttenuation; //in dB !!
	private ioDevice device;
	
	public static enum ioDevice {
		
		ER10C(1,32767,69,0.000001,"ER10C"), // Short.MAX_VALUE (int16) = 32767
		Dummy(1,32767,69,0.000001,"Dummy");
		
		private double out16bitToVolt; //maps 16bit output to physical voltage (peak-to-peak)
		private double in10MilliVoltTobit;  //maps input voltage to bit depth (peak-to-peak)
		private double out1VTodBSPL;  //maps a 1V pp to dB SPL
		private double in0dBSPLToVolt;  //maps 0 dBSPL to a recorded voltage (with device set 0 dB gain!!)
		private String ioDeviceName;
		
		ioDevice(double out16bitToVolt,double in10MilliVoltTobit,
				double out1VTodBSPL, double in0dBSPLToVolt, String ioDeviceName ) {
			//Convert attenuation in dB relative to the maximum track level
			this.out16bitToVolt=out16bitToVolt; 
			this.in10MilliVoltTobit=in10MilliVoltTobit;
			this.out1VTodBSPL=out1VTodBSPL;
			this.in0dBSPLToVolt=in0dBSPLToVolt;
			this.ioDeviceName=ioDeviceName;
			
		}		
		
		public String getioDeviceName(){
			return ioDeviceName;
		}
		
		public double getin0dBSPLToVolt(){
			return in0dBSPLToVolt;
		}
		
		public double getout16bitToVolt(){
			return out16bitToVolt;
		}
		
		public double getin10MilliVoltTobit(){
			return in10MilliVoltTobit;
		}
		
		public double getout1VTodBSPL(){
			return out1VTodBSPL;
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
