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
package org.dkpro.tc.examples.shallow;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.io.ReutersCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.weka.MekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.CCq;
import meka.classifiers.multilabel.incremental.PSUpdateable;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.bayes.NaiveBayes;

public class MultilabelDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "en";

    private static final String EXPERIMENT_NAME = "ReutersTextClassificationComplex";
    private static final String FILEPATH_TRAIN = "src/main/resources/data/reuters/training";
    private static final String FILEPATH_TEST = "src/main/resources/data/reuters/test";
    private static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/reuters/cats.txt";

    public static void main(String[] args) throws Exception
    {
        DemoUtils.setDkproHome(MultilabelDemo.class.getSimpleName());

        new MultilabelDemo().runTrainTest();
    }
    
    public CollectionReaderDescription getReaderTrain() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class, ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                FILEPATH_TRAIN, ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, FILEPATH_GOLD_LABELS,
                ReutersCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                ReutersCorpusReader.PARAM_PATTERNS, "*.txt");
    }
    
    public CollectionReaderDescription getReaderTest() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class, ReutersCorpusReader.PARAM_SOURCE_LOCATION, FILEPATH_TEST,
                ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, FILEPATH_GOLD_LABELS,
                ReutersCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                ReutersCorpusReader.PARAM_PATTERNS,  "*.txt");
    }
    
    public TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K,
                        600, WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N,
                        3));
    }

    // ##### Train Test #####
    public void runTrainTest() throws Exception
    {
        
        //additional Dimensions that cannot be set via the builder
        // multi-label feature selection (Mulan specific options), reduces the feature set to 10
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_LABEL_TRANSFORMATION_METHOD,
                "BinaryRelevanceAttributeEvaluator");
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_NUM_LABELS_TO_KEEP, 10);
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);

        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, EXPERIMENT_NAME)
                .dataReaderTrain(getReaderTrain())
                .dataReaderTest(getReaderTest())
                .preprocessing(getPreprocessing())
                .featureSets(getFeatureSet())
                .learningMode(LearningMode.MULTI_LABEL)
                .featureMode(FeatureMode.DOCUMENT)
                .additionalDimensions(dimFeatureSelection)
                .bipartitionThreshold(0.5)
                .machineLearningBackend(
                        new MLBackend(new MekaAdapter(), BR.class.getName(), "-W", NaiveBayes.class.getName()),
                        new MLBackend(new MekaAdapter(), CCq.class.getName(), "-P", "0.9"),
                        new MLBackend(new MekaAdapter(), PSUpdateable.class.getName(), "-B", "900", "-S", "9")
                        )
                .run();
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
