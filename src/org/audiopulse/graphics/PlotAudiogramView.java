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

	public String title;
	public double[] DPOAEData;
	public double[] noiseFloor;
	public double[] f1Data;
	public double[] f2Data;
	/**
	 * constructor
	 * @param context
	 * @param f2Data2 
	 * @param f1Data2 
	 * @param noiseFloor2 
	 * @param dPOAEData2 
	 * @param title2 
	 */
	public PlotAudiogramView(Context context, String title, double[] DPOAEData, 
			double[] noiseFloor, double[] f1Data, double[] f2Data) {
		super(context);
	
		this.DPOAEData = DPOAEData;
		this.noiseFloor =noiseFloor;
		this.f1Data =f1Data;
		this.f2Data =f2Data;
		
		final AFreeChart chart = createChart2();

		setChart(chart);
	}

	private static YIntervalSeriesCollection createDataset2() {
	       
        //TODO: These are normative values. Maybe be best to move these values
        //into an resource folder where they can be easily modified in the future.
		
		//TODO: Add this dataset to the graph
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
    	
    	
		XYSeries series1 = new XYSeries("DPOAE");
		XYSeries series2 = new XYSeries("Noise Floor");
		XYSeries series3 = new XYSeries("F1");
		XYSeries series4 = new XYSeries("F2");
    	
		//NOTE: We assume data is being send in an interleaved array where
		// odd samples are X-axis and even samples go in the Y-axis
		for(int i=0;i<(DPOAEData.length/2);i++){
			series1.add(DPOAEData[i*2], DPOAEData[i*2+1]);
			series2.add(noiseFloor[i*2], noiseFloor[i*2+1]);
			series3.add(f1Data[i*2], f1Data[i*2+1]);
			series4.add(f2Data[i*2], f2Data[i*2+1]);

		}
		
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

private AFreeChart createChart2() {
	XYDataset data = createDataset();
	AFreeChart chart = ChartFactory.createXYLineChart(
			title, // chart title
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


