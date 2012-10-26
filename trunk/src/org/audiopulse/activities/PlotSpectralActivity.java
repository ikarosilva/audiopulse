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

import org.audiopulse.graphics.PlotSpectralView;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * DeviationRendererDemo02Activity
 */
public class PlotSpectralActivity extends AudioPulseActivity {

    /**
     * Called when the activity is starting.
     * @param savedInstanceState
     */
	private static final String TAG="PlotSpectralActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle audio_bundle = getIntent().getExtras();
        long N=audio_bundle.getLong("N");
        Double recordRMS=audio_bundle.getDouble("recordRMS");
        short[] audioBuffer;
		audioBuffer=audio_bundle.getShortArray("samples");
		float sampleRate=audio_bundle.getFloat("recSampleRate");
		double expectedFrequency=audio_bundle.getDouble("expectedFrequency");
		
		Log.v(TAG,"Sample rate is " + sampleRate);
        PlotSpectralView mView = new PlotSpectralView(this,N,audioBuffer,
        		sampleRate,recordRMS,expectedFrequency);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mView);
    }
}
