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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.SimpleDkproTCReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This demo uses the {@link SimpleDkproTCReader}.
 */

public class SimpleDkproTCReaderDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";
    public static final int NUM_FOLDS = 2;
    public static final String FILEPATH_TRAIN = "src/main/resources/data/simple_reader/train";
    public static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/simple_reader/gold_labels.txt";

    public static void main(String[] args)
        throws Exception
    {
        SimpleDkproTCReaderDemo demo = new SimpleDkproTCReaderDemo();
        demo.runCrossValidation(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation(
                "SimpleReaderDemoCV", WekaAdapter.getInstance(), getPreprocessing(), NUM_FOLDS);
        batch.addInnerReport(WekaClassificationReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(WekaBatchCrossValidationReport.class);
        batch.addReport(WekaBatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace()
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, SimpleDkproTCReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] {
                        SimpleDkproTCReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                        SimpleDkproTCReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        SimpleDkproTCReader.PARAM_SENTENCES_FILE,
                        FILEPATH_TRAIN + "/instances.txt"}));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                asList(new String[] { LuceneNGramDFE.class.getName() }));

        // parameters to configure feature extractors
        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension
                .create(DIM_PIPELINE_PARAMS,
                        asList(new Object[] {
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                                "100",
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
                                1,
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N,
                                3 }));

        @SuppressWarnings("unchecked")
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle(
                "readers", dimReaders), Dimension.create(DIM_DATA_WRITER,
                WekaDataWriter.class.getName()), Dimension.create(
                DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters,
                dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(
                BreakIteratorSegmenter.class,
                BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
