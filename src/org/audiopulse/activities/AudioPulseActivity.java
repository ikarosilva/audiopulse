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
 * AudioPulseActivity.java 
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

import org.sana.android.app.ObservationActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.media.AudioManager;

//AudioPulseActivity: base activity that all other AudioPulse activities should extend
public class AudioPulseActivity extends ObservationActivity {

	public final String TAG = "AudioPulseActivity";
	
	public static boolean userAirplaneMode;
	public static int userVolume;
	
	AudioManager audioManager;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	//disable some hardware key functionality that would interfere with a test
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    Log.v(TAG, event.toString());
	    
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
	    	//override volume down function: do nothing.
	        return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
	    	//override volume up function: do nothing.
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

	    Log.v(TAG, event.toString());
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
	    	//override volume down function: do nothing.
	        return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
	    	//override volume up function: do nothing.
	        return true;
	    }

	    return super.onKeyUp(keyCode, event);
	}

	// Hardware setup prior to test: airplane mode, max volume, input AGC(?)
	public void beginTest() {

		//set airplane mode
    	try{ 
    		//first read if airplane mode is already on
    		userAirplaneMode = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON)==1;
    	} catch(Exception e) { 
    		Log.e(TAG, "Airplane Mode setting not found");
	    	Toast.makeText(this, "Warning: airplane mode error", Toast.LENGTH_LONG).show();
	    	userAirplaneMode = true; 		//bypass set by pretending it's already on
    	}
    	
    	//set airplane mode if not already
		if (!userAirplaneMode) {
			setAirplaneMode(true);
		}
		
		//set volume to max
		//Set the audio properties for play and recording
		audioManager = (AudioManager) getApplicationContext().getSystemService(android.content.Context.AUDIO_SERVICE);
		userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);	
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,  0);
		
		//TODO: set up listeners / broadcast receivers / whatever to monitor volume & airplane mode
		//TODO: wait until settings are confirmed (e.g. delay to set airplane mode)
		
		//TODO: disable input AGC?
	}
	
	//restore setting to those prior to beginTest()
	public void endTest() {
		Log.v(TAG,"End Test cleanup");
		
		//TODO: unregister listeners that check for changes
		
		//if airplane mode was off, turn it back off
		if (!userAirplaneMode) {
			setAirplaneMode(false);
		}
		
		//set volume back to user setting
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, 0);
	
		//TODO: re-enable input AGC?
	}
	
	// enable or disable airplane mode
	public void setAirplaneMode(boolean enable) {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON,enable?1:0); 
    	//broadcast event.
    	Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
	    	intent.putExtra("state", enable); 
    	getBaseContext().sendBroadcast(intent);
	
	}
}
