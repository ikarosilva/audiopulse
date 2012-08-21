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
 * RecordActivity.java
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

package AudioPulseCalibration.AudioPulse.org;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

public class RecordActivity extends Activity {
	protected static final String TAG = "RecordActivity";
	private int mAudioBufferSize;
	private int mAudioBufferSampleSize;
	private AudioRecord mAudioRecord;
	private boolean inRecordMode = false;
	final int sampleRate = 8000;
	final int stopTime = 2; //in frames
	final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	final int mAudioSource=MediaRecorder.AudioSource.MIC;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initAudioRecord();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "Resuming audio recording...");
		inRecordMode = true;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				getSamples();
			}
		});
		t.start();
	}

	protected void onPause() {
		Log.v(TAG, "Pausing audio recording...");
		inRecordMode = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "Destroying audio recording...");
		if(mAudioRecord != null) {
			mAudioRecord.release();
            Log.v(TAG, "Released recording");			
		}
		super.onDestroy();
	}
	
	public OnRecordPositionUpdateListener mListener = new OnRecordPositionUpdateListener(){
		
		public void onPeriodicNotification(AudioRecord recorder){
			Log.v(TAG,"in onPeriodicNotification");
		}
		public void onMarkerReached(AudioRecord recorder){
			inRecordMode = false;
		}
	};

	private void initAudioRecord() {
		try {
			mAudioBufferSize = 5 * AudioRecord.getMinBufferSize(sampleRate,
					channelConfig, audioFormat);
			mAudioBufferSampleSize = mAudioBufferSize / 2;
			mAudioRecord = new AudioRecord(
					mAudioSource,
					sampleRate,
					channelConfig,
					audioFormat,
					mAudioBufferSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		mAudioRecord.setNotificationMarkerPosition(stopTime);
		mAudioRecord.setRecordPositionUpdateListener(mListener);
		int audioRecordState = mAudioRecord.getState();
		if(audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.e(TAG, "Audio record was not properly initialized!!");
			finish();
		}
		else {
			Log.v(TAG, "Audio record initialized");
		}
	}
		
	private void getSamples() {
		if(mAudioRecord == null) return;
		short[] audioBuffer = new short[mAudioBufferSampleSize];
		double[] rec = new double[1024];
		final long play_time=3000;
		final int Play_Sample_Rate=44100;
		final int Play_Buffer_Size=1024*2;
		final float frequency1 = 2000;
        final float frequency2 = 2400;
        final float increment1 = (float)(2*Math.PI) * frequency1 / Play_Sample_Rate; // angular increment for each sample
        final float increment2 = (float)(2*Math.PI) * frequency2 / Play_Sample_Rate; // angular increment for each sample
        
		//Start playing calibration sound before recording
		new Thread( new Runnable( ) 
	      {
	         public void run( )
	         {        		
	            float angle1 = 0;
	            float angle2 = 0;
	            AudioPlay device = new AudioPlay(Play_Sample_Rate);
	            float samples[] = new float[Play_Buffer_Size];
	            //Play for X duration
	            long t0=System.currentTimeMillis();
	            while( true )
	            {
	               for( int i = 0; i < samples.length; i++ )
	               {
	                  samples[i] = (float) (0.5*(float)Math.sin( angle1 ) + 0.60653066*0.5*(float)Math.sin( angle2 ));
	                  angle1 += increment1;
	                  angle2 += increment2;
	               }
	               device.writeSamples( samples );
	               if(System.currentTimeMillis()-t0 > play_time)
	            	   break;
	            }        	
	         }
	      } ).start();
		
		mAudioRecord.startRecording();
		Bundle audio_bundle = new Bundle();
		int audioRecordingState = mAudioRecord.getRecordingState();
		if(audioRecordingState != AudioRecord.RECORDSTATE_RECORDING) {
			Log.e(TAG, "audio is not being recorded");
			finish();
		}
		else {
			Log.v(TAG, "starting recording of audio");
		}
		while(inRecordMode) {
		    int samplesRead = mAudioRecord.read(audioBuffer, 0, mAudioBufferSampleSize);
		    Log.v(TAG, "Got samples: " + samplesRead);
		}	
		mAudioRecord.stop();
		Log.v(TAG, "Recording has stopped recording");
		for (int i=0; i < 1024; i++){
	       rec[i]= (double) audioBuffer[i]*SpectralWindows.hamming(i,1024);
		//rec[i]=Math.sin((double)2*Math.PI*3000*i/sampleRate);
		}
		Log.v(TAG, "plotting data...");
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		//Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		audio_bundle.putDoubleArray("audio_data",rec);
		audio_bundle.putInt("sampleRate",sampleRate);
		audio_bundle.putInt("N",1024);
		intent.putExtras(audio_bundle);
        startActivity(intent);
	}
}