/**
 * Copyright 2019
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
package org.dkpro.tc.examples.shallow.serialization.xgboost;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
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
import org.dkpro.tc.examples.shallow.annotators.UnitOutcomeAnnotator;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.io.libsvm.AdapterFormat;
import org.dkpro.tc.ml.experiment.ExperimentSaveModel;
import org.dkpro.tc.ml.model.PreTrainedModelProviderDocumentMode;
import org.dkpro.tc.ml.model.PreTrainedModelProviderUnitMode;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class XgboostSaveAndLoadModelDocumentSingleLabelTest
    extends TestCaseSuperClass
    implements Constants
{
    static String documentTrainFolder = "src/main/resources/data/twitter/train";
    static String documentTestFolder = "src/main/resources/data/twitter/test";
    static String unitTrainFolder = "src/main/resources/data/brown_tei/";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ParameterSpace documentGetParameterSpaceSingleLabel(boolean useParametrizedArgs)
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                documentTrainFolder, FolderwiseDataReader.PARAM_LANGUAGE, "en",
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                        TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K,
                                50, WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N,
                                3)));

        ParameterSpace pSpace;
        if (useParametrizedArgs) {

            Map<String, Object> config = new HashMap<>();
            config.put(DIM_CLASSIFICATION_ARGS,
                    new Object[] { new XgboostAdapter(), "objective=multi:softmax" });
            config.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
            config.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());
            Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

            pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), mlas, dimFeatureSets);
        }
        else {
            Map<String, Object> config = new HashMap<>();
            config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new XgboostAdapter() });
            config.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
            config.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());
            Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

            pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets, mlas);
        }
        return pSpace;
    }

    @Test
    public void documentRoundTripTest() throws Exception
    {

        File modelFolder = folder.newFolder();

        ParameterSpace docParamSpace = documentGetParameterSpaceSingleLabel(false);
        documentTrainAndStoreModel(docParamSpace, modelFolder);
        documentLoadAndUseModel(modelFolder, false);
        documentVerifyCreatedModelFiles(modelFolder);

        docParamSpace = documentGetParameterSpaceSingleLabel(true);
        documentTrainAndStoreModel(docParamSpace, modelFolder);
        documentLoadAndUseModel(modelFolder, true);

        modelFolder.deleteOnExit();
    }

    private void documentVerifyCreatedModelFiles(File modelFolder)
    {
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

        File id2outcomeMapping = new File(
                modelFolder.getAbsolutePath() + "/" + AdapterFormat.getOutcomeMappingFilename());
        assertTrue(id2outcomeMapping.exists());
    }

    private static void documentTrainAndStoreModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel experiment = new ExperimentSaveModel("TestSaveModel", modelFolder);
        experiment.setPreprocessing(
                createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class)));
        experiment.setParameterSpace(paramSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(experiment);
    }

    private static void documentLoadAndUseModel(File modelFolder,
            boolean evaluateWithClassificationArgs)
        throws Exception
    {
        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(PreTrainedModelProviderDocumentMode.class,
        		PreTrainedModelProviderDocumentMode.PARAM_ADD_TC_BACKEND_ANNOTATION, true,
        		PreTrainedModelProviderDocumentMode.PARAM_TC_MODEL_LOCATION, modelFolder);

        CollectionReader reader = CollectionReaderFactory.createReader(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, documentTestFolder, TextReader.PARAM_LANGUAGE,
                "en", TextReader.PARAM_PATTERNS,
                Arrays.asList(TextReader.INCLUDE_PREFIX + "*/*.txt"));

        List<TextClassificationOutcome> outcomes = new ArrayList<>();
        while (reader.hasNext()) {
            JCas jcas = JCasFactory.createJCas();
            reader.getNext(jcas.getCas());
            jcas.setDocumentLanguage("en");

            tokenizer.process(jcas);
            tcAnno.process(jcas);

            outcomes.add(JCasUtil.selectSingle(jcas, TextClassificationOutcome.class));
        }

        assertEquals(4, outcomes.size());
        
        Set<String> expectedOutcomes = new HashSet<>();
        expectedOutcomes.add("emotional");
        expectedOutcomes.add("neutral");
        
        for(TextClassificationOutcome o : outcomes) {
            assertTrue(expectedOutcomes.contains(o.getOutcome()));
        }
    }

    @Test
    public void unitRoundTripTest() throws Exception
    {

        File modelFolder = folder.newFolder();

        ParameterSpace unitParamSpace = unitGetParameterSpaceSingleLabel();
        unitTrainAndStoreModel(unitParamSpace, modelFolder);
        unitLoadAndUseModel(modelFolder);
        unitVerifyCreatedModelFiles(modelFolder);

        modelFolder.deleteOnExit();
    }

    public static ParameterSpace unitGetParameterSpaceSingleLabel()
        throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, unitTrainFolder,
                TeiReader.PARAM_PATTERNS,   "a01.xml" );

        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Map<String, Object> wekaConfig = new HashMap<>();
        wekaConfig.put(DIM_CLASSIFICATION_ARGS, new Object[] { new XgboostAdapter() });
        wekaConfig.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
        wekaConfig.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", wekaConfig);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                        TcFeatureFactory.create(CharacterNGram.class,
                                CharacterNGram.PARAM_NGRAM_LOWER_CASE, false)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets, mlas);

        return pSpace;
    }

    private static void unitLoadAndUseModel(File modelFolder) throws Exception
    {
        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(PreTrainedModelProviderUnitMode.class,
        		PreTrainedModelProviderUnitMode.PARAM_TC_MODEL_LOCATION, modelFolder,
        		PreTrainedModelProviderUnitMode.PARAM_NAME_TARGET_ANNOTATION, Token.class.getName());

        CollectionReader reader = CollectionReaderFactory.createReader(TeiReader.class,
                TeiReader.PARAM_SOURCE_LOCATION, unitTrainFolder, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_PATTERNS, "a02.xml");

        List<TextClassificationOutcome> outcomes = new ArrayList<>();
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        reader.getNext(jcas.getCas());

        tcAnno.process(jcas);

        outcomes.addAll(JCasUtil.select(jcas, TextClassificationOutcome.class));

        Set<String> possibleOutcomes = new HashSet<>();
        possibleOutcomes.add("AT");
        possibleOutcomes.add("NP");
        possibleOutcomes.add("pct");
        possibleOutcomes.add("WDT");
        possibleOutcomes.add("JJ");
        possibleOutcomes.add("VBD");
        possibleOutcomes.add("NNS");
        possibleOutcomes.add("TO");
        possibleOutcomes.add("VBN");
        possibleOutcomes.add("IN");
        possibleOutcomes.add("CC");
        possibleOutcomes.add("VB");
        possibleOutcomes.add("NN");
        possibleOutcomes.add("VBD");
        possibleOutcomes.add("AP");
        possibleOutcomes.add("BEDZ");
        possibleOutcomes.add("HVD");

        assertEquals(31, outcomes.size());
        for (TextClassificationOutcome o : outcomes) {
            assertTrue(possibleOutcomes.contains(o.getOutcome()));
        }

    }

    private static void unitTrainAndStoreModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel experiment = new ExperimentSaveModel("UnitLiblinearTestSaveModel",
                modelFolder);
        experiment.setParameterSpace(paramSpace);
        experiment.setPreprocessing(createEngineDescription(UnitOutcomeAnnotator.class));
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(experiment);
    }

    private void unitVerifyCreatedModelFiles(File modelFolder)
    {
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

        File id2outcomeMapping = new File(
                modelFolder.getAbsolutePath() + "/" + AdapterFormat.getOutcomeMappingFilename());
        assertTrue(id2outcomeMapping.exists());
    }
}