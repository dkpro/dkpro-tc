/**
 * Copyright 2016
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
package org.dkpro.tc.examples.single.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcFeature;
import org.dkpro.tc.core.util.TcFeatureFactory;
import org.dkpro.tc.examples.io.SimpleDkproTCReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfTokens;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.bayes.NaiveBayes;

/**
 * This demo uses the {@link SimpleDkproTCReader}.
 */

public class WekaSimpleDkproTCReaderDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";
    public static final int NUM_FOLDS = 2;
    public static final String FILEPATH_TRAIN = "src/main/resources/data/simple_reader/train";
    public static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/simple_reader/gold_labels.txt";

    public static void main(String[] args)
        throws Exception
    {
        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(SimpleDkproTCReader.class.getSimpleName());

        WekaSimpleDkproTCReaderDemo demo = new WekaSimpleDkproTCReaderDemo();
        demo.runTrainTest(getParameterSpace());
    }

    private void runTrainTest(ParameterSpace parameterSpace) throws Exception
    {
        ExperimentTrainTest batch = new ExperimentTrainTest("SimpleReaderDemoCV",
                WekaClassificationAdapter.class);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(parameterSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        
        Lab.getInstance().run(batch);
        
    }

    public static ParameterSpace getParameterSpace()
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                SimpleDkproTCReader.class, SimpleDkproTCReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                SimpleDkproTCReader.PARAM_GOLD_LABEL_FILE, FILEPATH_GOLD_LABELS,
                SimpleDkproTCReader.PARAM_SENTENCES_FILE, FILEPATH_TRAIN + "/instances.txt");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        dimReaders.put(DIM_READER_TEST, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<TcFeature<ExternalResourceDescription>>> dimFeatureExtractors = Dimension
                .create(DIM_FEATURE_SET,
                        Arrays.asList(
                                TcFeatureFactory.create(LuceneNGram.class, 
                                        LuceneNGram.PARAM_NGRAM_USE_TOP_K, "100",
                                        LuceneNGram.PARAM_NGRAM_MIN_N, "3",
                                        LuceneNGram.PARAM_NGRAM_MAX_N, "3"),
                                TcFeatureFactory.create(NrOfTokens.class),
                                TcFeatureFactory.create(LuceneNGram.class, 
                                        LuceneNGram.PARAM_NGRAM_USE_TOP_K, "5",
                                        LuceneNGram.PARAM_NGRAM_MIN_N, "5",
                                        LuceneNGram.PARAM_NGRAM_MAX_N, "5"))
                                );

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureExtractors,
                dimClassificationArgs);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class,
                BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
