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
package org.dkpro.tc.examples.model.weka;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.bayes.NaiveBayes;

/**
 * Round-trip tests for save/load model experiments. Tests all feature modes (document, pair, unit),
 * as well as all learning models (single-label, multi-label, regression).
 *
 */
public class WekaSaveAndLoadModelDocumentSingleLabelTest extends TestCaseSuperClass
    implements Constants
{
    static String documentTrainFolder = "src/main/resources/data/twentynewsgroups/bydate-train";
    static String documentTrainFolderReuters = "src/main/resources/data/reuters/training";
    static String documentGoldLabelsReuters = "src/main/resources/data/reuters/cats.txt";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws Exception
    {
    	super.setup();
        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentSingleLabelTest.class.getSimpleName());
    }

    private ParameterSpace documentGetParameterSpaceSingleLabel()
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, documentTrainFolder,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, "en",
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new Object[] { new WekaClassificationAdapter(), NaiveBayes.class.getName() }));

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(NrOfChars.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
                dimClassificationArgs);
        return pSpace;
    }

    @Test
    public void documentRoundTripWekaSingleLabel()
        throws Exception
    {

        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentSingleLabelTest.class.getSimpleName());
        File modelFolder = folder.newFolder();

        ParameterSpace docParamSpace = documentGetParameterSpaceSingleLabel();
        documentWriteModel(docParamSpace, modelFolder);
        documentLoadModelSingleLabel(modelFolder);

        // verify created files

        File classifierFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
        assertTrue(classifierFile.exists());

        File metaOverride = new File(modelFolder.getAbsolutePath() + "/" + META_COLLECTOR_OVERRIDE);
        assertTrue(metaOverride.exists());

        File extractorOverride = new File(
                modelFolder.getAbsolutePath() + "/" + META_EXTRACTOR_OVERRIDE);
        assertTrue(extractorOverride.exists());

        File modelMetaFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_META);
        assertTrue(modelMetaFile.exists());

        File featureMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_MODE);
        assertTrue(featureMode.exists());

        File learningMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_LEARNING_MODE);
        assertTrue(learningMode.exists());

        File bipartitionThreshold = new File(
                modelFolder.getAbsolutePath() + "/" + MODEL_BIPARTITION_THRESHOLD);
        assertTrue(bipartitionThreshold.exists());

        modelFolder.deleteOnExit();
    }

    private static void documentWriteModel(ParameterSpace paramSpace, File modelFolder)
                throws Exception
    {
        ExperimentSaveModel batch;
        batch = new ExperimentSaveModel("TestSaveModel", modelFolder);
        batch.setPreprocessing(
                createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class)));
        batch.setParameterSpace(paramSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);
    }

    private static void documentLoadModelSingleLabel(File modelFolder)
        throws Exception
    {

        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath());

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an example text");
        jcas.setDocumentLanguage("en");

        tokenizer.process(jcas);
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(1, outcomes.size());
        assertEquals("comp.graphics", outcomes.get(0).getOutcome());
    }

}