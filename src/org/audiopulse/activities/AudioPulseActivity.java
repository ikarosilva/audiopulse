package org.audiopulse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class AudioPulseActivity extends Activity{
	
	public static final String TAG = "AudioPulse Activity";
	public static boolean userAirplaneMode;
	public static boolean initialized = false;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!initialized) {
			//code block for things that will only happen on the first launch
			userAirplaneMode = Settings.System.getInt( getContentResolver(),
	    			Settings.System.AIRPLANE_MODE_ON, 0
	    			) == 1;
			
			initialized = true;
		}
		 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setAirplaneMode(true);
		//TODO: wait for airplane mode (& volume, other settings) to be set before continuing
	}
	
	
	
	@Override
	protected void onDestroy() {
		if (isTaskRoot() && isFinishing()) {
			setAirplaneMode(userAirplaneMode);
		}
		super.onDestroy();
	}
		
	public boolean setAirplaneMode(Boolean enable){ 
    	try{ 
    		if (enable == (Settings.System.getInt(getContentResolver(), 
    		    	Settings.System.AIRPLANE_MODE_ON)==1)) {
    			return true; //don't write setting if it's already what we want
    		}

	    	Settings.System.putInt( 
		    	getContentResolver(), 
		    	Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0); 

	    	//broadcast event.
	    	//TODO: establish listener so we know if someone else turned it off
	    	Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
		    	intent.putExtra("state", !enable); 
	    	getBaseContext().sendBroadcast(intent); 
	    	return true;
    	} catch(Exception e) { 
	    	Toast.makeText(this, "exception:"+e.toString(), Toast.LENGTH_LONG).show();
	    	return false;
    	} 
	} 
}
