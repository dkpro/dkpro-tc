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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
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
import org.dkpro.tc.examples.io.STSReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMOreg;

/**
 * Round-trip tests for save/load model experiments. Tests all feature modes (document, pair, unit),
 * as well as all learning models (single-label, multi-label, regression).
 *
 */
public class WekaSaveAndLoadModelDocumentPairRegression extends TestCaseSuperClass
    implements Constants
{
    static String pairTrainFiles = "src/main/resources/data/sts2012/STS.input.MSRpar.txt";
    static String pairGoldFiles = "src/main/resources/data/sts2012/STS.gs.MSRpar.txt";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws Exception
    {
    	super.setup();
        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentPairRegression.class.getSimpleName());
    }

    @Test
    public void pairRoundTripWekaRegression()
        throws Exception
    {

        DemoUtils.setDkproHome(WekaSaveAndLoadModelDocumentPairRegression.class.getSimpleName());
        File modelFolder = folder.newFolder();

        ParameterSpace pairParamSpace = pairGetParameterSpace();
        pairWriteModel(pairParamSpace, modelFolder);
        pairLoadModelRegression(modelFolder);

        // verify created files

        File classifierFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
        assertTrue(classifierFile.exists());

        File metaOverride = new File(modelFolder.getAbsolutePath() + "/" + META_COLLECTOR_OVERRIDE);
        assertFalse(metaOverride.exists());

        File extractorOverride = new File(
                modelFolder.getAbsolutePath() + "/" + META_EXTRACTOR_OVERRIDE);
        assertFalse(extractorOverride.exists());

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

    private static void pairWriteModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel("TestSaveModel", modelFolder);
        batch.setPreprocessing(
                createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class)));
        batch.setParameterSpace(paramSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);
    }

    private static ParameterSpace pairGetParameterSpace()
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Object readerTrain = CollectionReaderFactory.createReaderDescription(STSReader.class,
                STSReader.PARAM_INPUT_FILE, pairTrainFiles, STSReader.PARAM_GOLD_FILE,
                pairGoldFiles);
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new Object[] { new WekaAdapter(), SMOreg.class.getName() }));


        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class)));
        
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_REGRESSION),
                Dimension.create(DIM_FEATURE_MODE, FM_PAIR), dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    private static void pairLoadModelRegression(File modelFolder)
        throws Exception
    {
        CollectionReader reader = CollectionReaderFactory.createReader(STSReader.class,
                STSReader.PARAM_INPUT_FILE, pairTrainFiles, STSReader.PARAM_GOLD_FILE,
                pairGoldFiles);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath());

        JCas jcas = JCasFactory.createJCas();

        reader.getNext(jcas.getCas());
        tcAnno.process(jcas);

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        assertEquals(1, outcomes.size());
        assertEquals("4.0958", outcomes.get(0).getOutcome());

    }

}