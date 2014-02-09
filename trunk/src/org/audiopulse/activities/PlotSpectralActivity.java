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
import org.audiopulse.R;
import org.audiopulse.graphics.PlotSpectralView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stacked_graphics_layout);
		Bundle audio_bundle = getIntent().getExtras();
		double[] psd=audio_bundle.getDoubleArray("psd");
		float sampleRate=audio_bundle.getFloat("recSampleRate");
		double respHz=audio_bundle.getDouble("respHz");
		double respSPL=audio_bundle.getDouble("respSPL");
		double noiseSPL=audio_bundle.getDouble("noiseSPL");
		double[] noiseRangeHz=audio_bundle.getDoubleArray("noiseRangeHz");
		Log.w(TAG,"got bundled data");
		int N=psd.length;
		Log.v(TAG,"plotting spectrum, fftSize= " + N);
		
		//Print numerical results in the log view
		TextView testLog;
		testLog = (TextView)this.findViewById(R.id.graphics_Log);
		double diff=Math.round(10*( respSPL-noiseSPL))/10.0;
		String display=testLog.getText() + "\n Response: " + respHz + " Hz at "
		       + respSPL +" dB SPL. Noise =  " + noiseSPL + " dB SPL. Diff (resp-noise)=  "+ (diff) + " \n";
	   testLog.setText(display);

	   //Print spectral plot in first graph area
	   PlotSpectralView mView1 = new PlotSpectralView(this,psd,sampleRate,respHz,N,noiseRangeHz);
	    LinearLayout layout2 = (LinearLayout) findViewById(R.id.graphics_canvas2);
	   layout2.addView(mView1,0);
		
	}
}
