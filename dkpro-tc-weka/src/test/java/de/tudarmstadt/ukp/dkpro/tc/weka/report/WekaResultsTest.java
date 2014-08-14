/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import meka.classifiers.multilabel.BR;
import meka.core.Result;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Tests several Weka and Meka classifiers and their evaluation scores. Changes in the underlying
 * classifier algorithms might change the evaluation scores and hence make the tests fail.
 * 
 * @author daxenberger
 * 
 */
public class WekaResultsTest
{
    Instances singleLabelTestData;
    Instances multiLabelTestData;
    Instances regressionTestData;
    Instances singleLabelTrainData;
    Instances multiLabelTrainData;
    Instances regressionTrainData;

    @Before
    public void initialize()
        throws IOException
    {

        File singleLabelTestFile;
        File multiLabelTestFile;
        File regressionTestFile;
        File singleLabelTrainFile;
        File multiLabelTrainFile;
        File regressionTrainFile;
        try {
            singleLabelTrainFile = new File(this.getClass()
                    .getResource("/train_test/singleLabelTrainInstances.arff").toURI());
            multiLabelTrainFile = new File(this.getClass()
                    .getResource("/train_test/multiLabelTrainInstances.arff").toURI());
            regressionTrainFile = new File(this.getClass()
                    .getResource("/train_test/regressionTrainInstances.arff").toURI());
            singleLabelTestFile = new File(this.getClass()
                    .getResource("/train_test/singleLabelTestInstances.arff").toURI());
            multiLabelTestFile = new File(this.getClass()
                    .getResource("/train_test/multiLabelTestInstances.arff").toURI());
            regressionTestFile = new File(this.getClass()
                    .getResource("/train_test/regressionTestInstances.arff").toURI());
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }

        singleLabelTrainData = TaskUtils.getInstances(singleLabelTrainFile, false);
        singleLabelTestData = TaskUtils.getInstances(singleLabelTestFile, false);
        multiLabelTrainData = TaskUtils.getInstances(multiLabelTrainFile, true);
        multiLabelTestData = TaskUtils.getInstances(multiLabelTestFile, true);
        regressionTrainData = TaskUtils.getInstances(regressionTrainFile, false);
        regressionTestData = TaskUtils.getInstances(regressionTestFile, false);
    }

    @Test
    public void testWekaResultsRegression()
        throws Exception
    {
        SMOreg cl = new SMOreg();
        Instances trainData = WekaUtils.removeInstanceId(regressionTrainData, false);
        Instances testData = WekaUtils.removeInstanceId(regressionTestData, false);
        cl.buildClassifier(trainData);
        Evaluation eval = WekaUtils.getEvaluationSinglelabel(cl, trainData, testData);
        assertEquals(0.45, eval.correlationCoefficient(), 0.01);
    }

    @Test
    public void testWekaResultsSingleLabel()
        throws Exception
    {
        SMO cl = new SMO();
        Instances testData = WekaUtils.makeOutcomeClassesCompatible(singleLabelTrainData,
                singleLabelTestData, false);
        Instances trainData = WekaUtils.removeInstanceId(singleLabelTrainData, false);
        testData = WekaUtils.removeInstanceId(testData, false);
        cl.buildClassifier(trainData);
        Evaluation eval = WekaUtils.getEvaluationSinglelabel(cl, trainData, testData);
        assertEquals(7.0, eval.correct(), 0.01);
    }

    @Test
    public void testWekaResultsMultiLabel()
        throws Exception
    {
        BR cl = new BR();
        cl.setOptions(new String[] { "-W", J48.class.getName() });
        Instances testData = WekaUtils.makeOutcomeClassesCompatible(multiLabelTrainData,
                multiLabelTestData, true);
        Instances trainData = WekaUtils.removeInstanceId(multiLabelTrainData, true);
        testData = WekaUtils.removeInstanceId(testData, true);
        cl.buildClassifier(trainData);
        Result eval = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, "0.2");
        assertEquals(16.0, eval.L, 0.01);
        assertEquals(0.0, Result.getStats(eval, "1").get("Exact match"), 0.01);
    }

}
