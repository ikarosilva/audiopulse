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
 * PlotWaveformView.java 
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
package AudioPulseCalibration.AudioPulse.org;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.SolidColor;

import AudioPulseCalibration.AudioPulse.org.DemoView;

import android.content.Context;
import android.graphics.Color;

/**
 * DeviationRendererDemo02View
 */
public class PlotWaveformView extends DemoView {

	/**
	 * constructor
	 * @param context
	 * @param sampleRate 
	 * @param audioBuffer 
	 * @param N 
	 */
	private static int N;
	private static double[] audioBuffer;
	private static int sampleRate;
	
	public PlotWaveformView(Context context, int N, double[] audioBuffer, int sampleRate) {
		super(context);
		PlotWaveformView.N=N;
		PlotWaveformView.audioBuffer=audioBuffer;
		PlotWaveformView.sampleRate=sampleRate;
		final AFreeChart chart = createChart2();
		setChart(chart);
	}
    private static XYSeriesCollection createDataset2() {	
    		XYSeriesCollection result = new XYSeriesCollection();
        	XYSeries series = new XYSeries(1);
        	for(int n=0;n<N;n++){
    			series.add(1000*n/sampleRate, audioBuffer[n]);
    		}
        	result.addSeries(series);
            return result;
    }


private static AFreeChart createChart2() {
	XYDataset dataset = createDataset2();
	// create the chart...
	AFreeChart chart = ChartFactory.createXYLineChart(
			"Waveform Plot", // chart title
			"Time (ms)", // x axis label
			"Amplitude", // y axis label
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














