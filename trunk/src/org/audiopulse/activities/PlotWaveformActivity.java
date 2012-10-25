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
 * PlotWaveformActivity.java 
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

import org.audiopulse.graphics.PlotWaveformView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * DeviationRendererDemo02Activity
 */
public class PlotWaveformActivity extends AudioPulseActivity {
	
	private static final String TAG="PlotWaveformActivity";

    /**
     * Called when the activity is starting.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle audio_bundle = getIntent().getExtras();
        long N=audio_bundle.getLong("N");
        short[] samples;
		samples=audio_bundle.getShortArray("samples");
		float recSampleRate=audio_bundle.getFloat("recSampleRate");
		Log.v(TAG,"creating plot, recSampleRate= " + recSampleRate + " N=" + N + " time =" + N/recSampleRate);
        PlotWaveformView mView = new PlotWaveformView(this,N,samples,recSampleRate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mView);
    }
}
