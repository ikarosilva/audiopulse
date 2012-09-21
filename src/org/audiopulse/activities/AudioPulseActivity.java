package org.audiopulse.activities;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class AudioPulseActivity extends Activity{
	
	public static final String TAG = "AudioPulse Activity";
	
		
	public void setAirplaneMode(Boolean enable){ 
    	try{ 
	    	boolean isEnabled = Settings.System.getInt(
	    			getBaseContext().getContentResolver(),
	    			Settings.System.AIRPLANE_MODE_ON, 0
	    			) == 1; 
	    	// If airplane mode is on, value 0, else value is 1 
	    	Settings.System.putInt( 
		    	getBaseContext().getContentResolver(), 
		    	Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0); 
	    	// Reload when the mode is changed each time by sending Intent 
	    	Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
		    	intent.putExtra("state", !isEnabled); 
	    	getBaseContext().sendBroadcast(intent); 
    	} catch(Exception e) { 
	    	Toast.makeText(getBaseContext(), "exception:"+e.toString(), Toast.LENGTH_LONG).show(); 
    	} 
	} 
}
