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
package org.dkpro.tc.examples.model.resource;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.examples.io.BrownCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * This demo demonstrates the usage of the sequence classifier CRFsuite which uses Conditional
 * Random Fields (CRF).
 */
public class CRFSuiteSaveAndLoadModelTest
    implements Constants
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public Set<String> postags = new HashSet<>();

    @Before
    public void setup()
    {

        postags.add("AT");
        postags.add("NN");
        postags.add("PPS");
        postags.add("VBG");
        postags.add("HVD");
        postags.add("pct");
        postags.add("VBN");
        postags.add("IN");
        postags.add("AT");
        postags.add("NN");
        postags.add("pct");
        postags.add("DT");
        postags.add("TO");
        postags.add("NP");
        postags.add("VB");
        postags.add("CC");
        postags.add("JJ");
        postags.add("AP");
        postags.add("PPS");
        postags.add("BEDZ");
        postags.add("NNS");
    }

    @After
    public void cleanUp()
    {
        folder.delete();
    }

    @Test
    public void saveModel()
        throws Exception
    {
        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(Constants.DIM_CLASSIFICATION_ARGS, asList());

        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(dimClassificationArgs);
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
        ExperimentSaveModel batch = new ExperimentSaveModel("TestSaveModel", CRFSuiteAdapter.class,
                aModelFolder);
        batch.setParameterSpace(aPSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);

    }

    private ParameterSpace getParameterSpace(Dimension<List<String>> dimClassificationArgs)
        throws ResourceInitializationException
    {
        DemoUtils.setDkproHome(this.getClass().getName());

        String trainFolder = "src/main/resources/data/brown_tei/";

        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE, "en",
                BrownCorpusReader.PARAM_SOURCE_LOCATION, trainFolder,
                BrownCorpusReader.PARAM_LANGUAGE, "en", BrownCorpusReader.PARAM_PATTERNS, "*.xml");

        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(
                        TcFeatureFactory.create(LuceneCharacterNGram.class,
                                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 500,
                                LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 1,
                                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 3),
                // This is the only model store/load demo at the moment that makes use of a
                // resources in the file system which is loaded - please keep the Brown cluster
                // feature in here
                // to ensure that this functionality is covered by a test case :)
                TcFeatureFactory.create(BrownClusterFeature.class,
                        BrownClusterFeature.PARAM_BROWN_CLUSTERS_LOCATION,
                        "src/test/resources/brownCluster/enTweetBrownC1000F40"),
                TcFeatureFactory.create(NrOfChars.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE), dimFeatureSets,
                dimClassificationArgs);
        return pSpace;
    }

    @Test
    public void loadModelArow()
        throws Exception
    {

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(Constants.DIM_CLASSIFICATION_ARGS, asList(new String[] {
                        CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

        // create a model
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(dimClassificationArgs);
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an example text. It has 2 sentences.");
        jcas.setDocumentLanguage("en");

        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());

        tokenizer.process(jcas);
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(11, outcomes.size());// 9 token + 2 punctuation marks
        for (TextClassificationOutcome o : outcomes) {
            String label = o.getOutcome();
            assertTrue(postags.contains(label));
        }
    }

    @Test
    public void loadModelArowParameters()
        throws Exception
    {

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                asList(new String[] {
                        CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR, "-p",
                        "feature.minfreq=2", "-p", "gamma=2.0", "-p", "variance=3.000" }));

        // create a model
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace(dimClassificationArgs);
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an example text. It has 2 sentences.");
        jcas.setDocumentLanguage("en");

        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());

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