package org.audiopulse.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class AudioPulseActivity extends Activity {

	public final String TAG = "AudioPulseActivity";
	
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
//
}
