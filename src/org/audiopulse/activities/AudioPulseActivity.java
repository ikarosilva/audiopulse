package org.audiopulse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.media.AudioManager;

public class AudioPulseActivity extends Activity {

	public final String TAG = "AudioPulseActivity";
	
	public static boolean userAirplaneMode;
	public static int userVolume;
	
	AudioManager audioManager;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    Log.v(TAG, event.toString());
	    
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
	    	//do nothing
	        return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
	    	//do nothing
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

	    Log.v(TAG, event.toString());
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
	    	//do nothing
	        return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
	    	//do nothing
	        return true;
	    }

	    return super.onKeyUp(keyCode, event);
	}

	public void beginTest() {
		// actions such as set volume, airplane mode, etc, to do before beginning any test
		
		//set airplane mode
    	try{ 
    		userAirplaneMode = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON)==1;
    	} catch(Exception e) { 
    		Log.e(TAG, "Airplane Mode setting not found");
	    	Toast.makeText(this, "Warning: airplane mode error", Toast.LENGTH_LONG).show();
	    	userAirplaneMode = true; 		//bypass set by pretending it's already on
    	}
    		
		if (!userAirplaneMode) {
			setAirplaneMode(true);
		}
		
		//set volume
		//Set the audio properties for play and recording
		audioManager = (AudioManager) getApplicationContext().getSystemService(android.content.Context.AUDIO_SERVICE);
		userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);	
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,  0);
		
		//TODO: set up listeners / broadcast receivers / whatever to monitor volume & airplane mode
		//TODO: wait until settings are confirmed (e.g. delay to set airplane mode)
	}
	
	public void endTest() {
		//release / restore resources set by beginTest()
		Log.v(TAG,"End Test cleanup");
		
		if (!userAirplaneMode) {
			setAirplaneMode(false);
		}
		
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, 0);
	
	}
	
	public void setAirplaneMode(boolean enable) {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON,enable?1:0); 
    	//broadcast event.
    	Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
	    	intent.putExtra("state", enable); 
    	getBaseContext().sendBroadcast(intent);
	
	}
}
