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
package org.dkpro.tc.examples.shallow.serialization.crfsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.experiment.ExperimentSaveModel;
import org.dkpro.tc.ml.model.PreTrainedModelProviderSequenceMode;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * This demo demonstrates the usage of the sequence classifier CRFsuite which uses Conditional
 * Random Fields (CRF).
 */
public class CRFSuiteSaveAndLoadModelTest
    extends TestCaseSuperClass
    implements Constants
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public Set<String> postags = new HashSet<>();

    @Before
    public void setup() throws Exception
    {
        super.setup();
        postags.add("NN");
        postags.add("JJ");
        postags.add("NP");
        postags.add("DTS");
        postags.add("BEDZ");
        postags.add("HV");
        postags.add("PPO");
        postags.add("DT");
        postags.add("NNS");
        postags.add("PPS");
        postags.add("JJT");
        postags.add("ABX");
        postags.add("MD");
        postags.add("DOD");
        postags.add("VBD");
        postags.add("VBG");
        postags.add("QL");
        postags.add("pct");
        postags.add("CC");
        postags.add("VBN");
        postags.add("NPg");
        postags.add("IN");
        postags.add("WDT");
        postags.add("BEN");
        postags.add("VB");
        postags.add("BER");
        postags.add("AP");
        postags.add("RB");
        postags.add("CS");
        postags.add("AT");
        postags.add("HVD");
        postags.add("TO");
    }

    @After
    public void cleanUp()
    {
        folder.delete();
    }

    @Test
    public void saveModel() throws Exception
    {
        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new CrfSuiteAdapter(),
                        CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR,
                        "max_iterations=2" });
        config.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass());
        config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(mlas);
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        File classifierFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
        assertTrue(classifierFile.exists());

        File parameterFile = new File(
                modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_EXTRACTOR_CONFIGURATION);
        assertTrue(parameterFile.exists());

        File metaOverride = new File(modelFolder.getAbsolutePath() + "/" + META_COLLECTOR_OVERRIDE);
        assertTrue(metaOverride.exists());

        File extractorOverride = new File(
                modelFolder.getAbsolutePath() + "/" + META_EXTRACTOR_OVERRIDE);
        assertTrue(extractorOverride.exists());

        File modelMetaFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_META);
        assertTrue(modelMetaFile.exists());

        File tcversion = new File(modelFolder.getAbsolutePath() + "/" + MODEL_TC_VERSION);
        assertTrue(tcversion.exists());

        File featureMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_MODE);
        assertTrue(featureMode.exists());

        File learningMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_LEARNING_MODE);
        assertTrue(learningMode.exists());

        modelFolder.deleteOnExit();
    }

    private void executeSaveModelIntoTemporyFolder(ParameterSpace aPSpace, File aModelFolder)
        throws Exception
    {
        ExperimentSaveModel experiment = new ExperimentSaveModel("TestSaveModel", aModelFolder);
        experiment.setParameterSpace(aPSpace);
        experiment.setPreprocessing(AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class));
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(experiment);

    }

    private ParameterSpace getParameterSpace(Dimension<Map<String, Object>> mlas)
        throws ResourceInitializationException
    {
        String trainFolder = "src/main/resources/data/brown_tei/";

        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the
        // train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, trainFolder,
                TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_PATTERNS, "a*.xml");

        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(CharacterNGram.class,
                        CharacterNGram.PARAM_NGRAM_USE_TOP_K, 50, CharacterNGram.PARAM_NGRAM_MIN_N,
                        1, CharacterNGram.PARAM_NGRAM_MAX_N, 3),
                        // This is the only model store/load demo at the moment that
                        // makes use of a
                        // resources in the file system which is loaded - please keep
                        // the Brown cluster
                        // feature in here
                        // to ensure that this functionality is covered by a test case
                        // :)
                        TcFeatureFactory.create(BrownClusterFeature.class,
                                BrownClusterFeature.PARAM_BROWN_CLUSTERS_LOCATION,
                                "src/test/resources/brownCluster/enTweetBrownC1000F40"),
                        TcFeatureFactory.create(TokenRatioPerDocument.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE), dimFeatureSets, mlas);
        return pSpace;
    }

    @Test
    public void loadModelArow() throws Exception
    {

        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new CrfSuiteAdapter(),
                        CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR,
                        "max_iterations=2" });
        config.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass());
        config.put(DIM_FEATURE_USE_SPARSE, new CrfSuiteAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        // create a model
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(mlas);
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an example text. It has 2 sentences.");
        jcas.setDocumentLanguage("en");

        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(
        		PreTrainedModelProviderSequenceMode.class,
        		PreTrainedModelProviderSequenceMode.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
        		PreTrainedModelProviderSequenceMode.PARAM_ADD_TC_BACKEND_ANNOTATION, true,
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_TARGET_ANNOTATION, Token.class.getName());

        tokenizer.process(jcas);
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(11, outcomes.size());// 9 token + 2 punctuation marks
        for (TextClassificationOutcome o : outcomes) {
            String label = o.getOutcome();
            boolean b = postags.contains(label);
            if (!b) {
                fail("The tag [" + label + "] is not in the set of expected tags");
            }
        }
    }

    @Test
    public void loadModelArowParameters() throws Exception
    {

        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new CrfSuiteAdapter(),
                        CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR,
                        "max_iterations=2" });
        config.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass());
        config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        // create a model
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(mlas);
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an example text. It has 2 sentences.");
        jcas.setDocumentLanguage("en");

        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(PreTrainedModelProviderSequenceMode.class,
        		PreTrainedModelProviderSequenceMode.PARAM_ADD_TC_BACKEND_ANNOTATION, true,
        		PreTrainedModelProviderSequenceMode.PARAM_TC_MODEL_LOCATION, modelFolder,
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_TARGET_ANNOTATION, Token.class.getName());

        tokenizer.process(jcas);
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(11, outcomes.size());// 9 token + 2 punctuation marks
        for (TextClassificationOutcome o : outcomes) {
            assertTrue(postags.contains(o.getOutcome()));
        }
    }
}