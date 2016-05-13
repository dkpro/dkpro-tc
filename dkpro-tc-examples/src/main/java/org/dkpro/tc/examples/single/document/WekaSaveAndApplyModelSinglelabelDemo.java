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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DiscriminableReaderCollectionFactory;
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfTokensDFE;
import org.dkpro.tc.features.ngram.LuceneNGramDFE;
import org.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.bayes.NaiveBayes;

/**
 * Demo to show-case how trained models can be persisted.
 */
public class WekaSaveAndApplyModelSinglelabelDemo
    implements Constants
{
    /**
     * language of input files
     */
    public static final String LANGUAGE_CODE = "en";
    /**
     * input directory file path
     */
    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    /**
     * output folder path
     */
    public static final File modelPath = new File("target/model");
    /**
     * example text
     */
    public static final String EXAMPLE_TEXT = "This is an exmaple.";
    /**
     * example text id
     */
    public static final String EXAMPLE_TEXT_ID = "example_text";
    /**
     * path to where the prediction outcome is store
     */
    public static final File PREDICTION_PATH = new File("target/prediction");

    /**
     * Start the demo.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
        throws Exception
    {

        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(WekaSaveAndApplyModelSinglelabelDemo.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace();
        WekaSaveAndApplyModelSinglelabelDemo experiment = new WekaSaveAndApplyModelSinglelabelDemo();
        experiment.runSaveModel(pSpace);
        experiment.applyStoredModel(EXAMPLE_TEXT);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
        throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Object readerTrain = DiscriminableReaderCollectionFactory.createReaderDescription(
                TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] { NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 500,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, Arrays.asList(
                new String[] { NrOfTokensDFE.class.getName(), LuceneNGramDFE.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters,
                dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    // ##### SAVE-MODEL #####
    protected void runSaveModel(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel("TwentyNewsgroupsSaveModel",
                WekaClassificationAdapter.class, modelPath);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        LANGUAGE_CODE));
    }

    protected void applyStoredModel(String text)
        throws ResourceInitializationException, UIMAException, IOException
    {
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReader(StringReader.class,
                        StringReader.PARAM_DOCUMENT_TEXT, text, StringReader.PARAM_DOCUMENT_ID,
                        EXAMPLE_TEXT_ID, StringReader.PARAM_LANGUAGE, LANGUAGE_CODE),
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(TcAnnotator.class,
                        TcAnnotator.PARAM_TC_MODEL_LOCATION, modelPath),
                AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH));
    }
}
