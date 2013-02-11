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
 * PlotSpectralActivity.java 
 * based on DeviationRendererDemo02Activity.java
 * from afreechartdemo
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

package org.audiopulse.activities;

import org.audiopulse.graphics.PlotAudiogramView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class PlotAudiogramActivity extends AudioPulseActivity {
	/**
     * Called when the activity is starting.
     * @param savedInstanceState
     */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle audioBundle = getIntent().getExtras();
    	String title=audioBundle.getString("title");
    	double[] DPOAEData=audioBundle.getDoubleArray("DPOAEData");
    	double[] noiseFloor=audioBundle.getDoubleArray("noiseFloor");;
    	double[] f1Data=audioBundle.getDoubleArray("f1Data");;
    	double[] f2Data=audioBundle.getDoubleArray("f2Data");;
    	Log.v(TAG,"extracted DPOAE data from bundle");
    	
		PlotAudiogramView mView = new PlotAudiogramView(this,title,DPOAEData,noiseFloor,f1Data,f2Data);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mView);
    }
}
