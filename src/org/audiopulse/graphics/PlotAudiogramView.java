/* ===========================================================
 * SanaAudioPulse : a free platform for Teleaudiology.
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

import java.util.ArrayList;

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
import android.util.Log;

/**
 * DeviationRendererDemo02View
 */
public class PlotAudiogramView extends DemoView {

	private String title;
	private ArrayList<Double> responseData;
	private ArrayList<Double> noiseData;
	private ArrayList<Double> stimData;
	private static String TAG="PlotAudiogramView";

	public PlotAudiogramView(Context context, String title, ArrayList<Double> responseData, 
			ArrayList<Double> noiseData, ArrayList<Double> stimData) {
		super(context);

		this.responseData = responseData;
		this.noiseData =noiseData;
		this.stimData =stimData;

		final AFreeChart chart = createChart2();

		setChart(chart);
	}

	private static YIntervalSeriesCollection createDataset2() {

		//TODO: These are normative values. Maybe be best to move these values
		//into an resource folder where they can be easily modified in the future.

		//FIXME: Add this dataset to the graph, when the results are properly
		//calibrated, for both TEOAE and DPOAE ??
		YIntervalSeries normativeRange = new YIntervalSeries("Normative Range");
		int[] NUB={-10, -5, -5, -5, -4};
		int[] NLB={-15, -10, -13, -15, -13};

		normativeRange.add(7.206, -7,NLB[0], NUB[0]);
		normativeRange.add(5.083, 13.1,NLB[1], NUB[1]);
		normativeRange.add(3.616, 17.9,NLB[2], NUB[2]);
		normativeRange.add(2.542, 11.5,NLB[3], NUB[3]);
		normativeRange.add(1.818, 17.1,NLB[4], NUB[4]);

		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		dataset.addSeries(normativeRange);
		return dataset;
	}


	private  XYSeriesCollection createDataset() {


		XYSeries series1 = convertData2Series("Response", responseData);
		XYSeries series2 = convertData2Series("Noise Floor", noiseData);
		XYSeries series3 = convertData2Series("Stimulus", stimData);

		XYSeriesCollection dataset = new XYSeriesCollection();
		if(series1 != null)
			dataset.addSeries(series1);
		if(series2 != null)
			dataset.addSeries(series2);
		if(series3 != null)
			dataset.addSeries(series3);
		return dataset;
	}
	/**
	 * Creates a chart.
	 * @param dataset the data for the chart.
	 * @return a chart.
	 */

	private XYSeries convertData2Series(String name, ArrayList<Double> data){

		XYSeries series = null;
		//NOTE: We assume data is being send in an interleaved array where
		// odd samples are X-axis and even samples go in the Y-axis
		if(! data.isEmpty()){
			series = new XYSeries(name);
			for(int i=0;i<(data.size()/2);i++){
				series.add(data.get(i*2), data.get(i*2+1));		
			}
		}else{
			Log.v(TAG,"empty series: "+ name);
		}
		return series;
	}

	private AFreeChart createChart2() {
		XYDataset data = createDataset();
		AFreeChart chart = ChartFactory.createXYLineChart(
				title, // chart title
				"Frequency (kHz)", // x axis label
				"Level (dB SPL)", // y axis label
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
		renderer.setSeriesStroke(2, 3.0f);
		renderer.setSeriesStroke(3, 3.0f);

		renderer.setSeriesPaintType(0, new SolidColor(Color.GREEN));
		renderer.setSeriesFillPaintType(0, new SolidColor(Color.GREEN));
		renderer.setSeriesPaintType(1, new SolidColor(Color.BLUE));
		renderer.setSeriesFillPaintType(1, new SolidColor(Color.BLUE));
		renderer.setSeriesPaintType(2, new SolidColor(Color.RED));
		renderer.setSeriesFillPaintType(2, new SolidColor(Color.RED));
		renderer.setSeriesPaintType(3, new SolidColor(Color.GRAY));
		renderer.setSeriesFillPaintType(3, new SolidColor(Color.GRAY));
		plot.setRenderer(renderer);

		return chart;
	}
}


