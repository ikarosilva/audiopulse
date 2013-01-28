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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.audiopulse.activities.GeneralAudioTestActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


//TODO: Translate all message (informMiddle informEnd) Strings into the Resource folder for easy changes to other languages
public class PackageDataThreadRunnable implements Runnable
{
	private static final String TAG="PackageDataThreadRunnable";
	Handler mainThreadHandler = null;
	private Bundle results;

	//TODO: handle external storage unavailability
	private static File root = Environment.getExternalStorageDirectory();

	Context context;
	private File outFile =null;
	private AudioPulseXMLData xmlData;

	public PackageDataThreadRunnable(Handler h, AudioPulseXMLData xmlData,Context context)
	{
		Log.v(TAG,"constructing packagedata thread");
		mainThreadHandler = h;
		this.context=context;
		this.xmlData=xmlData;
	}

	public synchronized void run()
	{
		informStart();
		// Write file to disk
		// Define file name here because inform finish is adding the Uri to the message bundle 
		// TODO Does the bundling need to happen post SHortfile.writeFile	
		if(xmlData == null){
			informMiddle("No metadata! Results will not be saved!");
		} else {
			try {
				outFile=packageData();
				informMiddle("Saved file at: "+ outFile.getAbsolutePath());
			} catch (AudioPulseXmlException e) {
				Log.e(TAG, "Unable to package data: "+ e.getMessage());
				informMiddle("Unable to package data: "+ e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Unable to package data: "+ e.getMessage());
				informMiddle("Unable to package data: "+ e.getMessage());
			}
		}
		informFinish();
	}

	public void informMiddle(String str)
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.mainThreadHandler.sendMessage(m);
	}

	public void informStart()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Compressing & packaging Data for transmission"));
		GeneralAudioTestActivity.setPackedDataState(GeneralAudioTestActivity.threadState.ACTIVE);
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		results= new Bundle();
		// TODO use the final zip file uri instead of the raw file
		Uri output = (outFile != null)? Uri.fromFile(outFile):
			Uri.EMPTY;
		Log.d(TAG, "Output Uri: " + output);
		results.putParcelable("outfile", output);

		m.setData(results);
		GeneralAudioTestActivity.setPackedDataState(GeneralAudioTestActivity.threadState.COMPLETE);
		this.mainThreadHandler.sendMessage(m);
	}


	private synchronized File packageData() throws AudioPulseXmlException, IOException {

		//Create XML File
		String xmlFileName="AP_MetaData_"+ new Date().toString()+".xml";
		xmlFileName= root + "/" +  xmlFileName.replace(" ","-").replace(":", "-");
		xmlData.writeXMLFile(xmlFileName);
		Log.v(TAG,"xmldata is :" + xmlData.getElements().toString());
		//TODO: Generate list of files to compress and send to zip
		List<String> fileList= xmlData.getFileList();
		fileList.add(xmlFileName);

		//Zip all the files
		String zipFileName="AP_Encounder_"+ new Date().toString()+".zip";
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
		
		return new File(zipFileName);

	}

	public File getOutFile(){

		return outFile;
	}

}
