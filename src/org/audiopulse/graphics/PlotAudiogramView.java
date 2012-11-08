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

package org.audiopulse.graphics;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.DeviationRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.data.xy.YIntervalSeries;
import org.afree.data.xy.YIntervalSeriesCollection;
import org.afree.graphics.SolidColor;


import android.content.Context;
import android.graphics.Color;

/**
 * DeviationRendererDemo02View
 */
public class PlotAudiogramView extends DemoView {

	/**
	 * constructor
	 * @param context
	 */
	public PlotAudiogramView(Context context) {
		super(context);

		final AFreeChart chart = createChart2();

		setChart(chart);
	}

	/**
	 * Creates a sample dataset.
	 * @return a sample dataset.
	 */
	
    private static YIntervalSeriesCollection createDataset2() {
    	
    	
    	YIntervalSeries series1 = new YIntervalSeries("RE DPOAE");
    	int[] NUB={-10, -5, -5, -5, -4};
    	int[] NLB={-15, -10, -13, -15, -13};
    	
		series1.add(7.206, -7,NLB[0], NUB[0]);
		series1.add(5.083, 13.1,NLB[1], NUB[1]);
		series1.add(3.616, 17.9,NLB[2], NUB[2]);
		series1.add(2.542, 11.5,NLB[3], NUB[3]);
		series1.add(1.818, 17.1,NLB[4], NUB[4]);
		
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		dataset.addSeries(series1);
		return dataset;
    }

private static XYSeriesCollection createDataset() {
    	
    	
		XYSeries series1 = new XYSeries("DPOAE");
		XYSeries series2 = new XYSeries("Noise Floor");
		XYSeries series3 = new XYSeries("F1");
		XYSeries series4 = new XYSeries("F2");
    	
		series1.add(7.206, -7);
		series1.add(5.083, 13.1);
		series1.add(3.616, 17.9);
		series1.add(2.542, 11.5);
		series1.add(1.818, 17.1);
		
		series2.add(7.206, -7-10);
		series2.add(5.083, 13.1-10);
		series2.add(3.616, 17.9-10);
		series2.add(2.542, 11.5-10);
		series2.add(1.818, 17.1-10);
		
		series3.add(7.206, 64);
		series3.add(5.083, 64);
		series3.add(3.616, 64);
		series3.add(2.542, 64);
		series3.add(1.818, 64);
		
		series4.add(7.206, 54.9);
		series4.add(5.083, 56.6);
		series4.add(3.616, 55.6);
		series4.add(2.542, 55.1);
		series4.add(1.818, 55.1);
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		dataset.addSeries(series4);
		return dataset;
    }
	/**
	 * Creates a chart.
	 * @param dataset the data for the chart.
	 * @return a chart.
	 */

private static AFreeChart createChart2() {
	//XYDataset dataset = createDataset2();
	XYDataset data = createDataset();
	// create the chart...
	AFreeChart chart = ChartFactory.createXYLineChart(
			"RE DPOAE Test", // chart title
			"Frequency (kHz)", // x axis label
			"DPOAE Level (dB SPL)", // y axis label
			data, // data
			PlotOrientation.VERTICAL,
			true, // include legend
			true, // tooltips
			false // urls
			);

	XYPlot plot = (XYPlot) chart.getPlot();

	DeviationRenderer renderer = new DeviationRenderer(true, false);
	renderer.setSeriesStroke(0, 3.0f);
	renderer.setSeriesStroke(1, 3.0f);
	
	renderer.setSeriesPaintType(0, new SolidColor(Color.rgb(0, 0, 255)));
	renderer.setSeriesFillPaintType(0, new SolidColor(Color.rgb(250, 100, 100)));
	renderer.setSeriesPaintType(1, new SolidColor(Color.rgb(150, 150, 150)));
	renderer.setSeriesFillPaintType(1, new SolidColor(Color.rgb(150, 150, 150)));
	renderer.setSeriesPaintType(2, new SolidColor(Color.rgb(100, 100, 250)));
	renderer.setSeriesFillPaintType(2, new SolidColor(Color.rgb(100, 100, 250)));
	renderer.setSeriesPaintType(3, new SolidColor(Color.rgb(100, 250, 100)));
	renderer.setSeriesFillPaintType(3, new SolidColor(Color.rgb(100, 250, 100)));
	plot.setRenderer(renderer);

	return chart;
}
}


