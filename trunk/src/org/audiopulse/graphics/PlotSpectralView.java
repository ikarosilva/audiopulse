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
 * PlotSpectralView.java 
 * based on DeviationRendererDemo02View.java
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
package org.audiopulse.graphics;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.SolidColor;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.audiopulse.io.PlayThreadRunnable;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

/**
 * DeviationRendererDemo02View
 */
public class PlotSpectralView extends DemoView {

	/**
	 * constructor
	 * @param context
	 * @param sampleRate 
	 * @param audioBuffer 
	 * @param N 
	 */
	private static final String TAG="PlotSpectralView";
	private static long N;
	private static short[] audioBuffer;
	private static float sampleRate;
	private static final int maxFreq=4000;
	
	public PlotSpectralView(Context context, long N, short[] audioBuffer2, float sampleRate) {
		super(context);
		PlotSpectralView.N=N;
		PlotSpectralView.audioBuffer=audioBuffer2;
		PlotSpectralView.sampleRate=sampleRate;
		
		//PlotSpectralView.N=PlayThreadRunnable.samples.length;
		//PlotSpectralView.audioBuffer=PlayThreadRunnable.samples;
		//PlotSpectralView.sampleRate=PlayThreadRunnable.sampleRatePlay;
				
		//Log.v(TAG,"Constructor: N= " + N +  " sampleRate= " + sampleRate );
		
		final AFreeChart chart = createChart2();
		setChart(chart);
	}
    private static XYSeriesCollection createDataset2() {	
    		XYSeriesCollection result = new XYSeriesCollection();
        	XYSeries series = new XYSeries(1);
        	FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);

        	//Calculate the size of averaged waveform
        	//based on the maximum desired frequency for FFT analysis
        	int SPEC_N=(int) N;//(N*maxFreq/sampleRate ); 
        	SPEC_N=(int) Math.pow(2,Math.round(Math.log(SPEC_N)/Math.log(2)));
        	double fres= (double) sampleRate/SPEC_N;
        	double[] winData=new double[SPEC_N];
        	Complex[] tmpFFT=new Complex[SPEC_N];
    		double[] Pxx = new double[SPEC_N];
    		double tmpPxx;
        	//Break FFT averaging into SPEC_N segments for averaging
        	//Calculate spectrum 
        	//Variation based only
        	//http://www.mathworks.com/support/tech-notes/1700/1702.html
    		
    		//Perform windowing and averaging on the power spectrum
    		Log.v(TAG,"SPEC_N= " + SPEC_N + " fres= " + fres+ " N=" +N);
        	for (int i=0; i < N; i++){
        		if(i*SPEC_N+SPEC_N > N)
        			break;
        		for (int k=0;k<SPEC_N;k++){
        			winData[k]= (double) audioBuffer[i*SPEC_N + k]*SpectralWindows.hamming(k,SPEC_N);
        		}
        		tmpFFT=FFT.transform(winData,TransformType.FORWARD);
        		//Calculate the average only  about maxFreq
        		for(int k=0;k<(SPEC_N/2);k++){
        			tmpPxx = tmpFFT[k].abs()/(double)SPEC_N;
        			tmpPxx*=tmpPxx; //Not accurate for the DC & Nyquist, but we are not using it!
        			Pxx[k]=( (i*Pxx[k]) + tmpPxx )/((double) i+1);
        		}
     		}
    		for(int k=0;k<(SPEC_N/2);k++){
    			series.add(((double) 0) + k*fres, 10*Math.log10(Pxx[k]));
    		}
        	result.addSeries(series);
            return result;
    }


private static AFreeChart createChart2() {
	XYDataset dataset = createDataset2();
	// create the chart...
	AFreeChart chart = ChartFactory.createXYLineChart(
			"Power Spectrum", // chart title
			"Frequency (Hz)", // x axis label
			"Amplitude (dB)", // y axis label
			dataset, // data
			PlotOrientation.VERTICAL,
			false, // include legend
			true, // tooltips
			false // urls
			);
	XYPlot plot = (XYPlot) chart.getPlot();
	plot.setBackgroundPaintType(new SolidColor(Color.rgb(0, 0, 0)));
    plot.getDomainAxis().setLowerMargin(0.0);
    plot.getDomainAxis().setUpperMargin(0.0);
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
	renderer.setSeriesPaintType(0, new SolidColor(Color.rgb(0, 255, 0)));
	plot.setRenderer(renderer);
    return chart;
}
}














