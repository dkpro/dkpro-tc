/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.report.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.ArrayUtils;
import org.dkpro.lab.reporting.ChartUtil;
import org.dkpro.lab.storage.StreamWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class ScatterplotRenderer
    implements StreamWriter
{
    private DefaultXYDataset aDataset;
    private double min;
    private double max;

    public ScatterplotRenderer(double[] gold, double[] prediction)
    {
    	if (gold.length != prediction.length) {
    		throw new IllegalArgumentException("A equal number of gold and prediction values is required.");
    	}
    	
    	if (gold.length == 0) {
    		throw new IllegalArgumentException("Cannot draw without values.");
    	}
    	
    	min = Math.min(getMin(gold), getMin(prediction));
    	max = Math.max(getMax(gold), getMax(prediction));
    	
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][gold.length];
        data[0] = gold;
        data[1] = Arrays.copyOf(prediction, prediction.length);
        dataset.addSeries("Scatterplot", data);
        aDataset = dataset;
    }

    @Override
    public void write(OutputStream aStream)
        throws IOException
    {
        JFreeChart chart = ChartFactory.createXYLineChart("Scatterplot", "Gold", "Prediction", aDataset,
                PlotOrientation.VERTICAL, false, false, false);
        
        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setDotHeight(2);
        renderer.setDotWidth(2);
        
        double padding = (max - min) / 10;
        
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().getRangeAxis().setRange(min - padding, max + padding);
        chart.getXYPlot().getDomainAxis().setRange(min - padding, max + padding);
        ChartUtil.writeChartAsPDF(aStream, chart, 400, 400);
    }
    
    private double getMin(double[] values) {
    	return Collections.min(Arrays.asList(ArrayUtils.toObject(values)));
    }
    
    private double getMax(double[] values) {
    	return Collections.max(Arrays.asList(ArrayUtils.toObject(values)));
    }
}