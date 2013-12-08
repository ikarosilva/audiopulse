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

package org.audiopulse.io;
import java.io.*;
import java.util.Date;

import android.os.Environment;
import android.util.Log;


public class AudioPulseFileWriter extends Thread {

	private static final String TAG="AudioPulseWriteFile";
	private static final File root = Environment.getExternalStorageDirectory();
	private final File outFile;
	private final double[] data;
	private final static String fileExtension=".raw";
	
	public AudioPulseFileWriter(File f, double[] d){
		outFile=f;
		data=d;
	}

	public static String getFileExtension(){
		return fileExtension;
	}
	
	@Deprecated //Used the one that requires the ear being tested (below)
	public synchronized static File generateFileName(String testType,
			String testFrequency){
		String fileName="AP_" + testType + "-" + testFrequency + "kHz-" +new Date().toString()+fileExtension;
		File outFile = new File(root, fileName.replace(" ","-").replace(":", "-") );
		return outFile;
	}
	
	public synchronized static File generateFileName(String testType,
			String testFrequency, String testEar, Double attenuation){
		String fileName="AP_" + attenuation.toString()+ "-" 
			+ testType + "-" + testEar + '-'+ testFrequency + "kHz-" +new Date().toString()+fileExtension;
		File outFile = new File(root, fileName.replace(" ","-").replace(":", "-") );
		return outFile;
	}
	
	public synchronized static void writeFile(File outFile, double[] samples) throws Exception {
		//Write Short file to disk
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(outFile);
			out = new ObjectOutputStream(fos);
			out.writeObject(samples);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(TAG);
		} 
		Log.v(TAG,"Done writing file to disk");
	}
	
	public void run(){
		Log.v(TAG,"Writing file: " + outFile);
		try {
			AudioPulseFileWriter.writeFile(outFile, data);
		} catch (Exception e) {
			Log.v(TAG,"Error: could not acccess file!");
			e.printStackTrace();
		}
	}

}