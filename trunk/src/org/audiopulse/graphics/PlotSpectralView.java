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

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.plot.IntervalMarker;
import org.afree.chart.plot.Marker;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.ValueMarker;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.SolidColor;
import org.afree.ui.Layer;
import org.afree.ui.LengthAdjustmentType;

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
	private static double[] audioBuffer;
	private static float sampleRate;
	private static double frange=100; //Range for highlighting where the expected response should 
	//occur (in Hz)
	private static double expectedFrequency;
	static double fRangeStart;
	static double fRangeEnd;
	static int fftSize;

	//Assumptions!!
	//If FFT size is zero, the user is passing the already calculated FFT values

	public PlotSpectralView(Context context,double[] aBuffer, float Fs, 
			double eFrequency, int fftN) {
		super(context);
		audioBuffer=aBuffer;
		sampleRate=Fs;	
		fftSize=fftN;
		expectedFrequency=eFrequency;
		fRangeStart=expectedFrequency-frange;
		fRangeEnd=expectedFrequency+frange;
		Log.v(TAG,"Fs=" + sampleRate + " ,N= " + aBuffer.length);
		final AFreeChart chart = createChart2();
		setChart(chart);
	}
	private static XYSeriesCollection createDataset2() {

		XYSeriesCollection result = new XYSeriesCollection();
		XYSeries series = new XYSeries(1);
		Log.v(TAG,"loading spectrum");
		//In this case the spectrum is already calculated
		double step=sampleRate/(2.0*(audioBuffer.length-1));
		for(int k=0;k<(audioBuffer.length);k++){
			series.add(k*step, audioBuffer[k]);
		}
		result.addSeries(series);
		return result;
	}


	private static AFreeChart createChart2() {
		XYDataset dataset = createDataset2();
		// create the chart...
		AFreeChart chart = ChartFactory.createXYLineChart(
				"Spectrum", // chart title
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

		renderer.setSeriesStroke(0,3.0f);
		plot.setRenderer(renderer);

		//Plot expected range
		if(expectedFrequency != 0){
			Marker marker_V = new IntervalMarker(fRangeStart, fRangeEnd);
			marker_V.setLabelOffsetType(LengthAdjustmentType.EXPAND);
			marker_V.setPaintType(new SolidColor(Color.rgb(150, 150, 255)));
			plot.addDomainMarker(marker_V, Layer.BACKGROUND);
			Marker marker_V_Start = new ValueMarker(fRangeStart, Color.BLUE, 2.0f);
			Marker marker_V_End = new ValueMarker(fRangeEnd, Color.BLUE, 2.0f);
			plot.addDomainMarker(marker_V_Start, Layer.BACKGROUND);
			plot.addDomainMarker(marker_V_End, Layer.BACKGROUND);
		}

		return chart;
	}
}














