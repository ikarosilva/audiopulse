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

import java.util.ArrayList;

import org.audiopulse.R;
import org.audiopulse.analysis.DPOAEResults;
import org.audiopulse.graphics.PlotSpectralView;
import org.audiopulse.graphics.PlotWaveformView;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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
		double[] wave=audio_bundle.getDoubleArray("data");
		float sampleRate=audio_bundle.getFloat("recSampleRate");
		double respHz=audio_bundle.getDouble("respHz");
		double respSPL=audio_bundle.getDouble("respSPL");
		double noiseSPL=audio_bundle.getDouble("respSPL");
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
	  //setContentView(mView);
	   PlotSpectralView mView1 = new PlotSpectralView(this,psd,sampleRate,respHz,N);
	   PlotWaveformView mView2 = new PlotWaveformView(this,N,wave,sampleRate);
	   LinearLayout layout = (LinearLayout) findViewById(R.id.graphics_canvas);
	   mView1.canScrollHorizontally(0);
	   mView1.canScrollVertically(0);
	   mView1.setClickable(false);
	   mView2.canScrollHorizontally(0);
	   mView2.canScrollVertically(0);
	   layout.addView(mView1,0);
	   layout.addView(mView2,1);
		
	}
}
