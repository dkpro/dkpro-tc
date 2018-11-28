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
package org.dkpro.tc.examples.deeplearning.keras.regression;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertTrue;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.DeepExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.keras.KerasAdapter;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;

public class KerasRegressionCrossValidation
    extends PythonLocator
{
    ContextMemoryReport contextReport;
    
    @Test
    public void runTest() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        boolean testConditon = true;
        String python3 = null;
        python3 = getEnvironment();

        if (python3 == null) {
            System.err.println("Failed to locate Python with Keras - will skip this test case");
            testConditon = false;
        }
        
        if (testConditon) {

            DeepExperimentBuilder builder = new DeepExperimentBuilder();
            builder.experiment(ExperimentType.CROSS_VALIDATION, "kerasTrainTest")
                    .numFolds(2)
                    .dataReaderTrain(getTrainReader())
                    .dataReaderTest(getTestReader())
                    .learningMode(LearningMode.REGRESSION)
                    .featureMode(FeatureMode.DOCUMENT)
                    .preprocessing(getPreprocessing())
                    .pythonPath("/usr/local/bin/python3")
                    .embeddingPath("src/test/resources/wordvector/glove.6B.50d_250.txt")
                    .maximumLength(50).vectorizeToInteger(true)
                    .reports(contextReport)
                    .machineLearningBackend(new MLBackend(new KerasAdapter(),
                            "src/main/resources/kerasCode/regression/essay.py"))
                    .run();

            EvaluationData<Double> data = Tc2LtlabEvalConverter.convertRegressionModeId2Outcome(
                    contextReport.crossValidationCombinedIdFiles.get(0));
            SpearmanCorrelation spear = new SpearmanCorrelation(data);

            assertTrue(spear.getResult() < 0.0);
        }
    }
    
    private static CollectionReaderDescription getTestReader()
        throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/train/essay_train.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
    }

    private static CollectionReaderDescription getTrainReader()
        throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/test/essay_test.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
    }

    protected static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
