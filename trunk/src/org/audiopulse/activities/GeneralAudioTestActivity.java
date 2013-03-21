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
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.AudioPulseXMLData;
import org.audiopulse.io.PackageDataThreadRunnable;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.
@Deprecated 
/* Use BasicTestActivity as the base class, which handles a much smaller scope
 * of functions limited primarily to UI stuff only. 
 */
public abstract class GeneralAudioTestActivity extends AudioPulseActivity 
{
	public static final String TAG="GeneralAudioTestActivity";

	private Bundle audioResultsBundle;
	private Bundle resultsBundle;
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audioBundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	Thread packageDataThread = null;
	AudioPulseXMLData xmlData= new AudioPulseXMLData();//Initial XML Data only when being called from Sana
	PackageDataThreadRunnable pRun=null;
	Handler packageStatusBackHandler = null;
	public static double playTime=0.5;
	private static threadState recordingstate=GeneralAudioTestActivity.threadState.INITIALIZED;
	private static threadState playbackstate=GeneralAudioTestActivity.threadState.INITIALIZED;
	private static threadState packedDataState=GeneralAudioTestActivity.threadState.INITIALIZED;
	private static threadState testState=GeneralAudioTestActivity.threadState.INITIALIZED;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	public static enum threadState{
		INITIALIZED(0),
		ACTIVE(1),
		COMPLETE(2);
		int state;
		threadState(int i){
			state=i;
		}	
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_test_layout);

		// // to perform the test automatically rather than by a button press:
		//findViewById(android.R.id.content).invalidate();
		//performTest();
		//TODO: We should always have an option to do the test automatically (perhaps by reading from
		//the context in which the activity was called
		//[AHS] why can't the calling activity simply call startTest()?
	}

	//begin test. Generally, this function is called by a ButtonView in the layout.
	public abstract void startTest(View callingView);
	{

		//perform test	
		//plot results

	}

	//TODO: implement a focused "playRecordThread" function that simply takes params for what to play and what
	// to record, so that the base class implementation can be called from all extended GeneralAudioTestActivity classes.
	protected RecordThreadRunnable playRecordThread()
	{
		//This is the most generic form. Some specific tests could overwrite this for optimization.
		//For example, for SOAE there is no need to play a stimulus, so the play thread can be omitted.

		beginTest();
		Context context=this.getApplicationContext();		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context);
		playStatusBackHandler = new ReportStatusHandler(this);
		PlayThreadRunnable pRun = new PlayThreadRunnable(playStatusBackHandler,playTime);
		ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
		playThread = new Thread(pRun);
		rRun.setExpectedFrequency(pRun.stimulus.expectedResponse);
		recordThread = new Thread(rRun);        
		playThread.setPriority(Thread.MAX_PRIORITY);
		recordThread.setPriority(Thread.MAX_PRIORITY);
		execSvc.execute( recordThread );
		execSvc.execute( playThread );
		execSvc.shutdown();

		endTest();
		return rRun;
	}


	//TODO: not all of these functions are general to GeneralAudioTestActivity but instead specific to DPOAEActivity
	//NOTE(by Ikaro): Maybe we should still leave these functions here in general form, so that they can be used by other 
	//activities, if we think that they are not being used by most children, than we can push them into more specific classes.
	//But, in general if two or more children use these function, it best to keep it here.
	public void appendText(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + str);
	}

	public static void setRecordingState(GeneralAudioTestActivity.threadState state){
		GeneralAudioTestActivity.recordingstate=state;
	}

	public static GeneralAudioTestActivity.threadState getRecordingState(){
		return GeneralAudioTestActivity.recordingstate;
	}
	public static GeneralAudioTestActivity.threadState getTestState(){
		return GeneralAudioTestActivity.testState;
	}
	public static void setTestState(GeneralAudioTestActivity.threadState state){
		GeneralAudioTestActivity.testState=state;
	}

	public static void setPackedDataState(GeneralAudioTestActivity.threadState state){
		GeneralAudioTestActivity.packedDataState=state;
	}
	public static GeneralAudioTestActivity.threadState getPackedDataState(){
		return GeneralAudioTestActivity.packedDataState;
	}
	public void appendLine(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + str);
	}

	public void emptyText(){
		TextView tv = getTextView();
		tv.setText("");
	}

	private TextView getTextView(){
		return (TextView)this.findViewById(R.id.testLog);
	}

	//plot recorded signal spectrum
	public void plotSpectrum(Bundle audioResultsBundle) {
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(audioResultsBundle);
		this.audioResultsBundle=audioResultsBundle;
		startActivity(intent);
	}

	//plot audiogram
	public void plotAudiogram(Bundle resultsBundle) {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		intent.putExtras(resultsBundle);
		this.resultsBundle=resultsBundle;
		Log.v(TAG,"Bundled set");
		startActivity(intent);
	}

	//plot recorded waveform
	public void plotWaveform() {
		//TODO: Add check for not null audioResultsBundle (notify user that to run stimulus if they press this option before running anything).
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}

	// TODO Set some common functionality here or make abstract
	public void appendData(Bundle b) {
		// TODO Auto-generated method stub

	}

	//May need to overwrite this method in the child class ...
	//Add data through the ReportStatusHandler callback methods
	public synchronized PackageDataThreadRunnable packageThread()
	{
		if(xmlData != null){
			Context context=this.getApplicationContext();		
			packageStatusBackHandler = new ReportStatusHandler(this);
			pRun = new PackageDataThreadRunnable(recordStatusBackHandler,xmlData,context);
			ExecutorService execSvc = Executors.newFixedThreadPool( 1 );
			packageDataThread = new Thread(pRun);	
			execSvc.execute( packageDataThread );
			execSvc.shutdown();
			endTest();
		}
		return pRun;
	}

	public static void setPlaybackState(GeneralAudioTestActivity.threadState state) {
		GeneralAudioTestActivity.playbackstate=state;
	}
	public static GeneralAudioTestActivity.threadState getPlaybackState() {
		return GeneralAudioTestActivity.playbackstate;
	}


	public boolean hasNextTestFrequency(){
		//TODO: make this abstract and push implementation to subclass
		return false;
	}

	public void selectAndRunThread(){
		//TODO: make this abstract and push implementation to subclass
	}

	public void AnalyzeData(Bundle analysisResults) {
		//TODO: make this abstract and push implementation to subclass

	}

	public void addFileToPackage(String key, String fileName){
		this.xmlData.setSingleElement(key,fileName);
	}

}