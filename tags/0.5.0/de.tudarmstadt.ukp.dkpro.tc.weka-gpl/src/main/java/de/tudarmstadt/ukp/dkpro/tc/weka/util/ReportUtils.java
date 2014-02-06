package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.CORRELATION;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PCT_CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PCT_INCORRECT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import weka.core.Result;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ChartUtil;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.storage.StreamWriter;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MulanEvaluationWrapper;

/**
 * Utility methods needed in reports
 * 
 * @author Johannes Daxenberger
 * 
 */
public class ReportUtils
{
    /**
     * Creates a confusion matrix by collecting the results from the overall CV run stored in
     * {@code tempM}
     * 
     * @param tempM
     * @param actualLabelsList
     *            the label powerset transformed list of actual/true labels
     * @param predictedLabelsList
     *            the label powerset transformed list of predicted labels
     * @return
     */
    public static double[][] createConfusionMatrix(HashMap<String, Map<String, Integer>> tempM,
            List<String> actualLabelsList, List<String> predictedLabelsList)
    {
        double[][] matrix = new double[actualLabelsList.size()][predictedLabelsList.size()];

        Iterator<String> actualsIter = tempM.keySet().iterator();
        while (actualsIter.hasNext()) {
            String actual = actualsIter.next();
            Iterator<String> predsIter = tempM.get(actual).keySet().iterator();
            while (predsIter.hasNext()) {
                String pred = predsIter.next();
                int a = actualLabelsList.indexOf(actual);
                int p = predictedLabelsList.indexOf(pred);
                matrix[a][p] = tempM.get(actual).get(pred);
            }
        }
        return matrix;
    }

    /**
     * Converts a bipartition array into a list of class names. Parameter arrays must have the same
     * length
     * 
     * @param labels
     * @param classNames
     * @return
     */
    public static String doubleArrayToClassNames(int[] labels, String[] classNames)
    {
        StringBuffer buffer = new StringBuffer();

        for (int y = 0; y < labels.length; y++) {
            if (labels[y] == 1) {
                buffer.append(classNames[y] + ", ");
            }
        }
        String classString;
        try {
            classString = buffer.substring(0, buffer.length() - 2).toString();
        }
        catch (StringIndexOutOfBoundsException e) {
            classString = "";
        }
        return classString;
    }

    /**
     * Adds results from one fold to the overall CV confusion matrix. Also updates actual and
     * predicted label lists for the label powerset transformation
     * 
     * @param numInstances
     * @param r
     * @param classNames
     * @param actualLabelsList
     *            not null
     * @param predictedLabelsList
     *            not null
     * @param tempM
     *            not null
     */
    public static void updateTempMLConfusionMatrix(Result r, String[] classNames,
            List<String> actualLabelsList, List<String> predictedLabelsList,
            HashMap<String, Map<String, Integer>> tempM)
    {
        for (int i = 0; i < r.size(); i++) {
            int[] prediction = r.rowPrediction(i, Double.parseDouble(r.getInfo("Threshold")));
            int[] actual = r.rowActual(i);

            // in ML mode, we build the confusion matrix over the Label Power Set of all
            // actuals/predictions
            String predString = doubleArrayToClassNames(prediction, classNames);
            String actString = doubleArrayToClassNames(actual, classNames);

            if (!predictedLabelsList.contains(predString)) {
                predictedLabelsList.add(predString);
            }
            if (tempM.get(actString) != null) {
                if (tempM.get(actString).get(predString) != null) {
                    tempM.get(actString).put(predString, tempM.get(actString).get(predString) + 1);
                }
                else {
                    tempM.get(actString).put(predString, 1);
                }
            }
            else {
                HashMap<String, Integer> h = new HashMap<String, Integer>();
                h.put(predString, 1);
                tempM.put(actString, h);
                actualLabelsList.add(actString);
            }
        }
    }

    /**
     * Adds results from one fold to the overall CV results
     * 
     * @param results
     * @param cvResults
     */
    public static void addToResults(Map<String, Double> results, Map<String, List<Double>> cvResults)
    {
        for (String s : results.keySet()) {
            if (cvResults.get(s) != null) {
                cvResults.get(s).add(results.get(s));
            }
            else {
                List<Double> d = new ArrayList<Double>();
                d.add(results.get(s));
                cvResults.put(s, d);
            }
        }
    }

    /**
     * Creates data for average PR curve diagram over a threshold. <br>
     * See: <br>
     * article{Vens2008, <br>
     * author = {Vens, Celine and Struyf, Jan and Schietgat, Leander and D\v{z}eroski, Sa\v{s}o and
     * Blockeel, Hendrik}, <br>
     * title = {Decision trees for hierarchical multi-label classification}, <br>
     * journal = {Mach. Learn.}, <br>
     * issue_date = {November 2008}, <br>
     * volume = {73}, <br>
     * number = {2}, <br>
     * month = nov, <br>
     * year = {2008},<br>
     * pages = {185--214} <br>
     * }
     * 
     * @param r
     * @return
     * @throws IOException
     */
    public static double[][] createPRData(boolean[][] actualsArray, ArrayList<double[]> predictions)
        throws IOException
    {
        double[][] data = new double[2][11];
        double t = 0;

        for (int j = 0; j <= 10; j++) {
            double[] thresholds = new double[predictions.size()];
            Arrays.fill(thresholds, t / 10);

            double precision = MulanEvaluationWrapper.getMulanMeasure(predictions, actualsArray,
                    thresholds, new MicroPrecision(actualsArray[0].length)).getValue();
            double recall = MulanEvaluationWrapper.getMulanMeasure(predictions, actualsArray,
                    thresholds, new MicroRecall(actualsArray[0].length)).getValue();
            data[0][j] = recall;
            data[1][j] = precision;

            t += 1;
        }
        return data;
    }

    /**
     * @param prcData
     * @return
     */
    public static DefaultXYDataset createXYDataset(List<double[][]> prcData)
    {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][11];

        double[] avPrec = new double[11];
        double[] avRec = new double[11];

        for (int i = 0; i < prcData.size(); i++) {
            double[] r = prcData.get(i)[0];
            for (int j = 0; j < r.length; j++) {
                avRec[j] += r[j];
            }
            double[] p = prcData.get(i)[1];
            for (int j = 0; j < p.length; j++) {
                avPrec[j] += p[j];
            }
        }
        for (int i = 0; i < avPrec.length; i++) {
            avPrec[i] = avPrec[i] / prcData.size();
            avRec[i] = avRec[i] / prcData.size();
        }
        data[0] = avRec;
        data[1] = avPrec;
        dataset.addSeries("PR-Curve", data);
        return dataset;
    }

    /**
     * From TrecTool README:
     * 
     * Interpolated Recall - Precision Averages: at 0.00 at 0.10 ... at 1.00 See any standard IR
     * text (especially by Salton) for more details of recall-precision evaluation. Measures
     * precision (percent of retrieved docs that are relevant) at various recall levels (after a
     * certain percentage of all the relevant docs for that query have been retrieved).
     * 'Interpolated' means that, for example, precision at recall 0.10 (ie, after 10% of rel docs
     * for a query have been retrieved) is taken to be MAXIMUM of precision at all recall points >=
     * 0.10. Values are averaged over all queries (for each of the 11 recall levels). These values
     * are used for Recall-Precision graphs.
     * 
     * @author Richard Eckart de Castilho
     * @author Johannes Daxenberger
     */
    public static class PrecisionRecallDiagramRenderer
        implements StreamWriter
    {
        private DefaultXYDataset dataset;

        public PrecisionRecallDiagramRenderer(DefaultXYDataset aDataset)
        {
            dataset = aDataset;
        }

        @Override
        public void write(OutputStream aStream)
            throws IOException
        {
            JFreeChart chart = ChartFactory.createXYLineChart(null, "Recall", "Precision", dataset,
                    PlotOrientation.VERTICAL, false, false, false);
            chart.getXYPlot().setRenderer(new XYSplineRenderer());
            chart.getXYPlot().getRangeAxis().setRange(0.0, 1.0);
            chart.getXYPlot().getDomainAxis().setRange(0.0, 1.0);
            ChartUtil.writeChartAsSVG(aStream, chart, 400, 400);
        }
    }

    public static boolean containsExcludePattern(String string, List<String> patterns)
    {

        Pattern matchPattern;
        for (String pattern : patterns) {
            matchPattern = Pattern.compile(pattern);
            if (matchPattern.matcher(string).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks into the {@link FlexTable} and outputs general performance numbers if available
     * 
     * @param table
     */
    public static String getPerformanceOverview(FlexTable<String> table)
    {
        // output some general performance figures
        // TODO this is a bit of a hack. Is there a better way?
        Set<String> columnIds = new HashSet<String>(Arrays.asList(table.getColumnIds()));
        StringBuffer buffer = new StringBuffer("\n");
        if (columnIds.contains(PCT_CORRECT) && columnIds.contains(PCT_INCORRECT)) {
            int i = 0;
            buffer.append("ID\t% CORRECT\t% INCORRECT\n");
            for (String id : table.getRowIds()) {
                String correct = table.getValueAsString(id, PCT_CORRECT);
                String incorrect = table.getValueAsString(id, PCT_INCORRECT);
                buffer.append(i + "\t" + correct + "\t" + incorrect + "\n");
                i++;
            }
            buffer.append("\n");
        }
        else if (columnIds.contains(CORRELATION)) {
            int i = 0;
            buffer.append("ID\tCORRELATION\n");
            for (String id : table.getRowIds()) {
                String correlation = table.getValueAsString(id, CORRELATION);
                buffer.append(i + "\t" + correlation + "\n");
                i++;
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}