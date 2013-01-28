package org.audiopulse.io;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

class AudioPulseXmlException extends Exception {
	private static final long serialVersionUID = 1L;
	public AudioPulseXmlException() {
	}
	public AudioPulseXmlException(String msg) {
		super(msg);
	}
}


public class AudioPulseXMLData {

	public static String HEADER="AudioPulseXMLData";//Tag to the start of an AudioPulse XML object
	//TODO: Key values hard-coded for now, implement a more abstract way (see todo in the getters)
	//Should not use R.string.xxxx because the viewer may not have access to this resource
	public static final String keyPhoneType="phoneType";
	public static final String keyAcousticDevice="acousticDevice";
	public static final String keyAppVersion="appVersion";
	public static final String keyDPOAERightEarGram ="DPOAERightEarGram";
	public static final String keyDPOAELeftEarGram ="DPOAELeftEarGram";
	public static final String keySOAE="SOAE";
	private HashMap<String,String> elements = new HashMap<String, String>();

	public AudioPulseXMLData(){
		//initialize the persistent data --this data should not change within a patient encounter !
		elements.put(keyPhoneType, getPhoneType());
		elements.put(keyAcousticDevice, getAcousticDevice());
		elements.put(keyAppVersion, getAppVersion());
	}

	public AudioPulseXMLData(String file) throws XmlPullParserException, IOException, AudioPulseXmlException{
		//Use this method to load XML data from file
		this.readXMLFile(file);
	}

	private String getPhoneType() {
		//TODO this function should take a AudioSignal Type with MobilePhone as one of its data members and dynamically fetch phone type
		return "DummyPhone";
	}

	private String getAcousticDevice() {
		//TODO this function should take a AudioSignal Type with AcousticDevice as one of its data members and dynamically fetch device type
		return "DummyDevice";
	}

	private String getAppVersion() {
		//TODO this function should dynamically fetch the app version.
		return "DummyVersion";
	}

	public void setSOAE(String fileName) {
		//TODO we need to implement this as a list of AcousticFileType with 
		//the hash indexing into that list, so the parser can pull more info about the recording
		//type such as Fs, encoding type, channel configuration, bit resolution, ear tested, etc 
		elements.put(keySOAE,fileName);
	}

	public void setDPOAERightEarGram(String results) {
		//TODO we need to this as a list of DPOAEGramType with 
		//the hash indexing into that list, so the parser can pull more info about the results
		//type such as protocol used, ear tested, calibration, etc 
		//for now results is a CSV string with semi-colons delimiting rows
		elements.put(keyDPOAERightEarGram, results);
	}
	public void setDPOAELeftEarGram(String results) {
		//TODO we need to this as a list of DPOAEGramType with 
		//the hash indexing into that list, so the parser can pull more info about the results
		//type such as protocol used, ear tested, calibration, etc 
		//for now results is a CSV string with semi-colons delimiting rows
		elements.put(keyDPOAELeftEarGram, results);
	}
	public HashMap<String,String> getElements(){
		return elements;
	}

	public void setElements(HashMap<String, String> elements2) {
		elements=elements2;
	}
	public void setSingleElement(String key,String value) {
		elements.put(key, value);
	}
	
	public ArrayList<String> getFileList(){
		
		ArrayList<String> fileList=new ArrayList<String>();
		String tmp=null;
		Set<String> keys = elements.keySet();
		for(String thisKey : keys){
			if (elements.get(thisKey).endsWith(".raw") ){
				tmp=elements.get(thisKey).replace("file://","");
				fileList.add(tmp);
			}
		}
		return fileList;
	}

	/////---METHOD FOR READING Called by a constructor---	 
	private void readXMLFile(String file) throws XmlPullParserException, IOException, AudioPulseXmlException {

		BufferedReader buf = new BufferedReader(new FileReader(file.toString()));
		XmlPullParser xpp = Xml.newPullParser();
		xpp.setInput(buf);

		int eventType = xpp.getEventType();
		HashMap<String,String> elements = this.getElements();
		Set<String> keys = elements.keySet();
		String name=null;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			name = xpp.getName();
			if(eventType == XmlPullParser.START_TAG && !name.equalsIgnoreCase(AudioPulseXMLData.HEADER)) {
				eventType = xpp.next();
				String tmp_str=xpp.getText();
				if(tmp_str != null && eventType != XmlPullParser.END_TAG){
					boolean foundKey=false;
					for(String thisKey : keys){
						if (name.equalsIgnoreCase(thisKey)){
							elements.put(thisKey, tmp_str);
							foundKey=true;
							break;
						}
					}
					if(foundKey == false){
						throw new AudioPulseXmlException("Key: " + name + " not found!! Allowed keys are:" + keys);
					}
				} //!=  end_tag && value != null
			}
			eventType = xpp.next();
		}
		//End of xml document
		this.setElements(elements);
	}


	/////---METHOD FOR WRITING XML File to disk---	
	public void writeXMLFile(String file) throws AudioPulseXmlException{
		
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		Set<String> keys = elements.keySet();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("",this.HEADER);
			for (String thisKey : keys){
				serializer.startTag("", thisKey);
				serializer.text(elements.get(thisKey));
				serializer.endTag("", thisKey);	
			}
			serializer.endTag("", this.HEADER);
			serializer.endDocument();
		} catch (Exception e) {
			throw new AudioPulseXmlException("Error generating XML tree: " + e.getMessage());
		} 
			
		BufferedWriter xmlBuffer =null;
		File xmlFile = new File(file);
		try {
			xmlBuffer= new BufferedWriter(new FileWriter(xmlFile));
			xmlBuffer.write(writer.toString());
			xmlBuffer.flush();
			xmlBuffer.close();
		} catch (IOException e) {
			throw new AudioPulseXmlException("Could not create XML file for writing: " + e.getMessage());
		}
	}
}
