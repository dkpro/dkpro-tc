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
package de.tudarmstadt.ukp.dkpro.tc.examples.multi.document;

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

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.ReutersCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentSaveModel;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotator;
import de.tudarmstadt.ukp.dkpro.tc.weka.MekaClassificationUsingTCEvaluationAdapter;
import meka.classifiers.multilabel.MULAN;
import weka.classifiers.trees.RandomForest;

public class SaveAndApplyModelMultilabelDemo
    implements Constants
{

    public static final String EXPERIMENT_NAME = "ReutersTextClassificationUsingTCEvaluation";
    public static final String FILEPATH_TRAIN = "src/main/resources/data/reuters/training";
    public static final String FILEPATH_TEST = "src/main/resources/data/reuters/test";
    public static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/reuters/cats.txt";
    public static final String LANGUAGE_CODE = "en";
    public static final String BIPARTITION_THRESHOLD = "0.001";
   
    public static final File modelPath = new File("target/model");
    public static final File PREDICTION_PATH = new File("target/prediction");


    public static void main(String[] args)
        throws Exception
    {
    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(SaveAndApplyModelMultilabelDemo.class.getSimpleName());
    	
        ParameterSpace pSpace = getParameterSpace();
        SaveAndApplyModelMultilabelDemo experiment = new SaveAndApplyModelMultilabelDemo();
        experiment.runSaveModel(pSpace);
        experiment.applyStoredModel("An example sentence. And another one.");
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, ReutersCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS,
                Arrays.asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TRAIN,
                        ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        ReutersCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE,
                        ReutersCorpusReader.PARAM_PATTERNS,
                        ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"));
        dimReaders.put(DIM_READER_TEST, ReutersCorpusReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TEST,
                        ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        ReutersCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE,
                        ReutersCorpusReader.PARAM_PATTERNS,
                        ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"));

        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        Arrays.asList(new String[] {         
                                MULAN.class.getName(),
                                "-S",
                                "RAkEL2",
                                "-W",
                                RandomForest.class.getName()}));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "100", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
                        1, FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfTokensDFE.class.getName(),
                        LuceneNGramDFE.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_MULTI_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                Dimension.create(DIM_BIPARTITION_THRESHOLD, BIPARTITION_THRESHOLD),
                dimPipelineParameters, dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    protected void runSaveModel(ParameterSpace pSpace)
        throws Exception
    {
    	ExperimentSaveModel batch = new ExperimentSaveModel(EXPERIMENT_NAME + "-TrainTest",
        		MekaClassificationUsingTCEvaluationAdapter.class, modelPath);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        LANGUAGE_CODE));
    }
    
    protected void applyStoredModel(String text) throws ResourceInitializationException, UIMAException, IOException{
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReader(
						StringReader.class,
						StringReader.PARAM_DOCUMENT_TEXT, text,
						StringReader.PARAM_DOCUMENT_ID, "exampleID",
						StringReader.PARAM_LANGUAGE, LANGUAGE_CODE),
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(
						TcAnnotator.class,
						TcAnnotator.PARAM_TC_MODEL_LOCATION,
						modelPath),
				AnalysisEngineFactory.createEngineDescription(
						XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH));
    }
}