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
package org.dkpro.tc.examples.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureList;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.io.EssayScoreReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfSentences;
import org.dkpro.tc.features.length.NrOfTokens;
import org.dkpro.tc.features.length.NrOfTokensPerSentence;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.weka.WekaRegressionAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.LinearRegression;

/**
 * Round-trip tests for save/load model experiments. Tests all feature modes (document, pair, unit),
 * as well as all learning models (single-label, multi-label, regression).
 *
 */
public class WekaSaveAndLoadModelDocumentRegression
    implements Constants
{
    static String regressionTrain = "src/main/resources/data/essays/train/essay_train.txt";
    static String regressionTest = "src/main/resources/data/essays/test/essay_test.txt";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup()
    {
        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentRegression.class.getSimpleName());
    }

    /**
     * This test case trains a regression model on scored essay texts
     */
    @Test
    public void documentRoundTripWekaRegression()
        throws Exception
    {

        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentRegression.class.getSimpleName());
        File modelFolder = folder.newFolder();

        ParameterSpace paramSpace = regressionGetParameterSpace();
        regressionExecuteSaveModel(paramSpace, modelFolder);
        regressionLoadModel(modelFolder);

        // verify that all expected files have been created
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

    private void regressionLoadModel(File modelFolder)
        throws UIMAException, IOException
    {
        CollectionReader reader = CollectionReaderFactory.createReader(EssayScoreReader.class,
                EssayScoreReader.PARAM_SOURCE_LOCATION, regressionTest,
                EssayScoreReader.PARAM_LANGUAGE, "en");

        AnalysisEngine segmenter = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());

        JCas jcas = JCasFactory.createJCas();
        reader.hasNext();
        reader.getNext(jcas.getCas());

        segmenter.process(jcas);
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(1, outcomes.size());

        Double d = Double.valueOf(outcomes.get(0).getOutcome());
        assertTrue(d > 0.1 && d < 5);
    }

    private void regressionExecuteSaveModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel("regressionWeka",
                WekaRegressionAdapter.class, modelFolder);
        batch.setParameterSpace(paramSpace);

        Lab.getInstance().run(batch);
    }

    private ParameterSpace regressionGetParameterSpace()
        throws Exception
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                EssayScoreReader.class, EssayScoreReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/train/essay_train.txt",
                EssayScoreReader.PARAM_LANGUAGE, "en");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { LinearRegression.class.getName() }));

        Dimension<TcFeatureList> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureList(TcFeatureFactory.create(NrOfTokens.class),
                        TcFeatureFactory.create(NrOfSentences.class),
                        TcFeatureFactory.create(NrOfTokensPerSentence.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_REGRESSION),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

}