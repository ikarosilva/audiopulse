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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.os.Environment;
import android.util.Log;

public class AudioPulseFilePackager extends Thread
{
	private static final String TAG="PackageDataThreadRunnable";
	List<String> fileList;
	//TODO: handle external storage unavailability
	private static File root = Environment.getExternalStorageDirectory();
	private File outFile =null;

	public AudioPulseFilePackager(List<String> fileList){
		this.fileList=fileList;
	}
	
	public synchronized void run()
	{
		try {
			outFile=packageData();
			Log.v(TAG,"Packaged data save at " +
			outFile.getAbsolutePath());
		} catch (IOException e) {
			Log.v(TAG,"Could not package data: " + e.getMessage());
			outFile=null;
		}
	}

	
	private synchronized File packageData() throws IOException {

		//Zip all the files
		String zipFileName="AP_Encounter_"+ new Date().toString()+".zip";
		zipFileName= root + "/" +zipFileName.replace(" ","-").replace(":", "-");
		OutputStream os= new FileOutputStream(zipFileName);
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
		//create byte buffer
		byte[] buffer = new byte[1024];
		try {
			for (String fileName : fileList) {
				Log.v(TAG,"Adding file to zip: " + fileName);
				//File file=new File(fileName);
				ZipEntry entry = new ZipEntry(fileName);
				zos.putNextEntry(entry);
				zos.write(buffer);
				zos.closeEntry();
			}
		} finally {
			zos.close();
		}
		outFile=new File(zipFileName);
		
		//Remove the files added to the zip
			for (String fileName : fileList) {
				Log.v(TAG,"Removing redundant data file : " + fileName);
				File tmpFile=new File(fileName);
				tmpFile.delete();	
			}
		
		return outFile ;

	}

	public static String unpackData(String inputFile) throws Exception {
		//Unzips data into the zip file directory - Should not be used by the Android Client...
		if(! inputFile.endsWith(".zip")){
			throw new Exception("Input File is NOT a zipped file: " + inputFile);
		}
		String outFile = inputFile.replace(".zip",""); 
		byte[] buffer = new byte[1024];
		try{
			File outFolder = new File(outFile);
			if(!outFolder.exists()){
				outFolder.mkdir();
			}
			ZipInputStream zis =new ZipInputStream(new FileInputStream(inputFile));
			ZipEntry entry = zis.getNextEntry();
			while(entry!=null){
				String fileName = entry.getName();
				File newFile = new File(outFile + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);             
				int length;
				while ((length = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}
				fos.close();   
				entry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}catch(IOException ex){
			ex.printStackTrace(); 
		}  
		return outFile;	
	}

	public synchronized File getOutFile(){
		return outFile;
	}

}
