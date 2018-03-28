/**
 * Copyright 2018
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
package org.dkpro.tc.examples.shallow.multi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.regression.MeanSquaredError;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class MultiRegressionUsingWekaLibsvmLiblinearTest
    extends TestCaseSuperClass
{
    MultiRegressionWekaLibsvmLiblinear javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        javaExperiment = new MultiRegressionWekaLibsvmLiblinear();
        pSpace = MultiRegressionWekaLibsvmLiblinear.getParameterSpace();
    }

    @Test
    public void testJavaTrainTest() throws Exception
    {
        javaExperiment.runTrainTest(pSpace);

        assertEquals(getSumOfExpectedTasksForTrainTest().intValue(),
                ContextMemoryReport.allIds.size());
        assertEquals(getSumOfMachineLearningAdapterTasks().intValue(),
                ContextMemoryReport.id2outcomeFiles.size());

        assertEquals(1.3, getMeanSquaredError(ContextMemoryReport.id2outcomeFiles, "Xgboost"), 0.1);
        assertEquals(0.5, getMeanSquaredError(ContextMemoryReport.id2outcomeFiles, "Weka"), 0.1);
        assertEquals(0.6, getMeanSquaredError(ContextMemoryReport.id2outcomeFiles, "Libsvm"), 0.1);
        assertEquals(2.8, getMeanSquaredError(ContextMemoryReport.id2outcomeFiles, "Liblinear"),
                0.2);
    }

    @Test
    public void testCrossValidation() throws Exception
    {
        javaExperiment.runCrossValidation(pSpace);

        assertEquals(getSumOfExpectedTasksForCrossValidation().intValue(),
                ContextMemoryReport.allIds.size());
        assertTrue(combinedId2OutcomeReportsAreDissimilar(
                ContextMemoryReport.crossValidationCombinedIdFiles));

        // Larger variance is acceptable, i.e. Windows, OSX and Linux compute slightly different
        // values
        assertEquals(1.4, getMeanSquaredErrorCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Weka"), 0.3);
        assertEquals(1.3, getMeanSquaredErrorCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Libsvm"), 0.3);
        assertEquals(4.1, getMeanSquaredErrorCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Liblinear"), 0.3);
    }

    private boolean combinedId2OutcomeReportsAreDissimilar(List<File> crossValidationTaskIds)
        throws IOException
    {

        Set<String> idset = new HashSet<>();

        for (File f : crossValidationTaskIds) {
            String idfile = FileUtils.readFileToString(f, "utf-8");
            if (idset.contains(idfile)) {
                return false;
            }
            idset.add(idfile);
        }

        return true;
    }

    private Integer getSumOfExpectedTasksForCrossValidation()
    {

        Integer sum = 0;

        sum += 4; // 2 x FeatExtract Train/Test
        sum += 2; // 2 x Meta

        sum += 4; // 2 x Facade + 2x ML Adapter
        sum += 1; // 1 x Crossvalidation

        sum *= 3; // 3 adapter in the setup
        sum += 1; // 1 x Init
        sum += 1; // 1 x Outcome

        return sum;
    }

    private double getMeanSquaredError(List<File> id2outcomeFiles, String simpleName)
        throws Exception
    {

        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().contains(simpleName + "TestTask-")) {

                EvaluationData<Double> data = Tc2LtlabEvalConverter
                        .convertRegressionModeId2Outcome(f);
                MeanSquaredError mse = new MeanSquaredError(data);
                return mse.getResult();
            }
        }

        return -1;
    }

    private double getMeanSquaredErrorCrossValidation(List<File> id2outcomeFiles, String simpleName)
        throws Exception
    {

        for (File f : id2outcomeFiles) {

            File file = new File(f.getParentFile(), "ATTRIBUTES.txt");
            Set<String> readSubTasks = readSubTasks(file);
            for (String s : readSubTasks) {
                File file2 = new File(f.getParentFile().getParentFile() + "/" + s,
                        "ATTRIBUTES.txt");
                if (!file2.exists()) {
                    continue;
                }
                Set<String> readSubTasks2 = readSubTasks(file2);
                for (String k : readSubTasks2) {

                    if (k.toLowerCase().contains(simpleName.toLowerCase())) {

                        EvaluationData<Double> data = Tc2LtlabEvalConverter
                                .convertRegressionModeId2Outcome(f);
                        MeanSquaredError mse = new MeanSquaredError(data);
                        return mse.getResult();
                    }
                }
            }
        }

        return -1;
    }

    private Set<String> readSubTasks(File attributesTXT) throws Exception
    {
        List<String> readLines = FileUtils.readLines(attributesTXT, "utf-8");

        int idx = 0;
        boolean found = false;
        for (String line : readLines) {
            if (line.startsWith(BatchTask.SUBTASKS_KEY)) {
                found = true;
                break;
            }
            idx++;
        }

        if (!found) {
            return new HashSet<>();
        }

        String line = readLines.get(idx);
        int start = line.indexOf("[") + 1;
        int end = line.indexOf("]");
        String subTasks = line.substring(start, end);

        String[] tasks = subTasks.split(",");

        Set<String> results = new HashSet<>();

        for (String task : tasks) {
            results.add(task.trim());
        }

        return results;
    }

    private Integer getSumOfMachineLearningAdapterTasks()
    {

        Integer sum = 0;

        sum += 1; // Weka
        sum += 1; // Libsvm
        sum += 1; // Liblinear
        sum += 1; // Xgboost

        return sum;
    }

    private Integer getSumOfExpectedTasksForTrainTest()
    {

        Integer sum = 0;

        sum += 2; // 1 x Facade + 1x ML Adapter
        sum *= 4; // 3 adapter in setup
        
        sum += 2; // 2 x FeatExtract shared by Xgboost/Liblinear/Libsvm
        sum += 2; // 2 x FeatExtract for Weka
        sum += 2; // 2 x Init
        sum += 1; // 1 x Meta
        sum += 1; // 1 x Outcome

        return sum;
    }
}
