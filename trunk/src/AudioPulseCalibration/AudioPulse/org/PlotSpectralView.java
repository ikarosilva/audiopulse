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
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import AudioPulseCalibration.AudioPulse.org.DemoView;

import android.content.Context;
import android.graphics.Color;

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
	private static int N;
	private static double[] audioBuffer;
	private static int sampleRate;
	
	public PlotSpectralView(Context context, int N, double[] audioBuffer, int sampleRate) {
		super(context);
		PlotSpectralView.N=N;
		PlotSpectralView.audioBuffer=audioBuffer;
		PlotSpectralView.sampleRate=sampleRate;
		final AFreeChart chart = createChart2();
		setChart(chart);
	}
    private static XYSeriesCollection createDataset2() {	
    		XYSeriesCollection result = new XYSeriesCollection();
        	XYSeries series = new XYSeries(1);
        	FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);
        	
    		//Calculate spectrum 
        	//Variation based on
        	//http://www.mathworks.com/support/tech-notes/1700/1702.html
    		Complex[] A= FFT.transform(audioBuffer, TransformType.FORWARD);
    		double fres= (double) sampleRate/N;
    		double Pxx;
    		for(int n=0;n<(N/2);n++){
    			Pxx = A[n].abs()/(double)N;
    			Pxx*=Pxx; //Not accurate for the DC & Nyquist, but we are not using it!
    			series.add(((double) 0) + n*fres, 10*Math.log10(Pxx));
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














