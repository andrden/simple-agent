package audio.cords;

/*
 * Simple Linear Regression Demo
 * Created by Tim De Pauw <http://pwnt.be/> on 2009-08-17
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.Paint;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;

@SuppressWarnings("serial")
public class SimplestChart extends JFrame {
	public SimplestChart(XYSeriesCollection data) {
		super("Regression Demo");
		setContentPane(new ChartPanel(createChart(data)));
		pack();
	}

  private static JFreeChart createChart(XYSeriesCollection data) {
		JFreeChart chart = ChartFactory.createScatterPlot(null, "X", "Y", data,
				PlotOrientation.VERTICAL, true, false, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer scatterRenderer = plot.getRenderer();

//        plot.getDomainAxis().resizeRange(2);
//        plot.getRangeAxis().resizeRange(2);
        plot.zoom(2);

		//StandardXYItemRenderer regressionRenderer = new StandardXYItemRenderer();
		//regressionRenderer.setBaseSeriesVisibleInLegend(false);
		//plot.setDataset(1, regress(data));
		//plot.setRenderer(1, regressionRenderer);
		DrawingSupplier ds = plot.getDrawingSupplier();
		for (int i = 0; i < data.getSeriesCount(); i++) {
			Paint paint = ds.getNextPaint();
			scatterRenderer.setSeriesPaint(i, paint);
			//regressionRenderer.setSeriesPaint(i, paint);
		}
		return chart;
	}
/*
	private static XYDataset regress(XYSeriesCollection data) {
		// Determine bounds
		double xMin = Double.MAX_VALUE, xMax = 0;
		for (int i = 0; i < data.getSeriesCount(); i++) {
			XYSeries ser = data.getSeries(i);
			for (int j = 0; j < ser.getItemCount(); j++) {
				double x = ser.getX(j).doubleValue();
				if (x < xMin) {
					xMin = x;
				}
				if (x > xMax) {
					xMax = x;
				}
			}
		}
		// Create 2-point series for each of the original series
		XYSeriesCollection coll = new XYSeriesCollection();
		for (int i = 0; i < data.getSeriesCount(); i++) {
			XYSeries ser = data.getSeries(i);
			int n = ser.getItemCount();
			double sx = 0, sy = 0, sxx = 0, sxy = 0, syy = 0;
			for (int j = 0; j < n; j++) {
				double x = ser.getX(j).doubleValue();
				double y = ser.getY(j).doubleValue();
				sx += x;
				sy += y;
				sxx += x * x;
				sxy += x * y;
				syy += y * y;
			}
			double b = (n * sxy - sx * sy) / (n * sxx - sx * sx);
			double a = sy / n - b * sx / n;
			XYSeries regr = new XYSeries(ser.getKey());
			regr.add(xMin, a + b * xMin);
			regr.add(xMax, a + b * xMax);
			coll.addSeries(regr);
		}
		return coll;
	}
*/
	private static XYSeriesCollection getTestData() {
		Random rg = new Random();
		XYSeriesCollection data = new XYSeriesCollection();
		for (int i = 1; i <= 3; i++) {
			XYSeries series = new XYSeries("Series " + i);
			double a = rg.nextDouble() - .5;
			int b = rg.nextInt(20) - 10;
			for (int j = 1; j <= 20; j++) {
				double x = j + (rg.nextDouble() - .5);
				double y = a * j + b + (rg.nextDouble() - .5) * 2;
				series.add(x, y);
			}
			data.addSeries(series);
		}
		return data;
	}

	public static void main(String[] args) throws Exception {
		SimplestChart demo = new SimplestChart(getTestData());
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
}
