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
package org.dkpro.tc.examples.shallow.serialization.svmhmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
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
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class SVMHMMSaveAndLoadModelTest
    extends TestCaseSuperClass
    implements Constants
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public void cleanUp()
    {
        folder.delete();
    }

    @Test
    public void saveModel() throws Exception
    {
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace();
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

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

    private ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        DemoUtils.setDkproHome(this.getClass().getName());

        String trainFolder = "src/main/resources/data/brown_tei/";

        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the
        // train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, trainFolder,
                TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_PATTERNS, "*.xml");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(
                        TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K,
                                50, WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N, 3),
                        TcFeatureFactory.create(TokenRatioPerDocument.class)));

        Map<String, Object> wekaConfig = new HashMap<>();
        wekaConfig.put(DIM_CLASSIFICATION_ARGS, new Object[] { new SvmHmmAdapter() });
        wekaConfig.put(DIM_DATA_WRITER, new SvmHmmAdapter().getDataWriterClass());
        wekaConfig.put(DIM_FEATURE_USE_SPARSE, new SvmHmmAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", wekaConfig);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE), dimFeatureSets, mlas);
        return pSpace;
    }

    @Test
    public void loadModel() throws Exception
    {
        // create a model
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace();
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
            String outcome = o.getOutcome();
            //tags should be alpha and be rather short
            assertTrue(outcome.matches("[A-Za-z]+") && outcome.length() < 5);
        }

    }
}