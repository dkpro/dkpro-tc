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
package org.dkpro.tc.examples.model.libsvm;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
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
import org.dkpro.tc.examples.io.BrownCorpusReader;
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfTokens;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LibsvmSaveAndLoadModelDocumentSingleLabelTest extends TestCaseSuperClass
    implements Constants
{
    static String documentTrainFolder = "src/main/resources/data/twitter/train";
    static String documentTestFolder = "src/main/resources/data/twitter/test";
    static String unitTrainFolder = "src/main/resources/data/brown_tei/";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws Exception
    {
    	super.setup();
        DemoUtils.setDkproHome(LibsvmSaveAndLoadModelDocumentSingleLabelTest.class.getSimpleName());
    }

    private ParameterSpace documentGetParameterSpaceSingleLabel(boolean useClassificationArguments)
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
        Dimension<List<Object>> dimClassificationArguments = Dimension
                .create(DIM_CLASSIFICATION_ARGS, Arrays.asList(new LibsvmAdapter(), "-c", "100"));

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(NrOfTokens.class), TcFeatureFactory.create(
                        LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3)));

        ParameterSpace pSpace;
        if (useClassificationArguments) {
            pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimClassificationArguments,
                    dimFeatureSets);
        }
        else {
        	
        	  @SuppressWarnings("unchecked")
              Dimension<List<Object>> dimClassArguments = Dimension
                      .create(DIM_CLASSIFICATION_ARGS, Arrays.asList(new LibsvmAdapter()));
        	
            pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets, dimClassArguments);
        }
        return pSpace;
    }

    @Test
    public void documentRoundTripTest()
        throws Exception
    {

        DemoUtils.setDkproHome(LibsvmSaveAndLoadModelDocumentSingleLabelTest.class.getSimpleName());
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
                modelFolder.getAbsolutePath() + "/" + LibsvmAdapter.getOutcomeMappingFilename());
        assertTrue(id2outcomeMapping.exists());
    }

    private static void documentTrainAndStoreModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel("TestSaveModel", modelFolder);
        batch.setPreprocessing(
                createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class)));
        batch.setParameterSpace(paramSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);
    }

    private static void documentLoadAndUseModel(File modelFolder,
            boolean evaluateWithClassificationArgs)
                throws Exception
    {
        AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);

        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath());

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

        if (evaluateWithClassificationArgs) {
            assertEquals(4, outcomes.size());
            assertEquals("neutral", outcomes.get(0).getOutcome());
            assertEquals("neutral", outcomes.get(1).getOutcome());
            assertEquals("neutral", outcomes.get(2).getOutcome());
            assertEquals("neutral", outcomes.get(3).getOutcome());
        }
        else {
            assertEquals(4, outcomes.size());
            assertEquals("emotional", outcomes.get(0).getOutcome());
            assertEquals("emotional", outcomes.get(1).getOutcome());
            assertEquals("emotional", outcomes.get(2).getOutcome());
            assertEquals("emotional", outcomes.get(3).getOutcome());
        }
    }

    @Test
    public void unitRoundTripTest()
        throws Exception
    {

        DemoUtils.setDkproHome(LibsvmSaveAndLoadModelDocumentSingleLabelTest.class.getSimpleName());
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
                BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE, "en",
                BrownCorpusReader.PARAM_SOURCE_LOCATION, unitTrainFolder,
                BrownCorpusReader.PARAM_PATTERNS, new String[] { INCLUDE_PREFIX + "a01.xml" });

        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimClassificationArguments = Dimension
                .create(DIM_CLASSIFICATION_ARGS, Arrays.asList(new LibsvmAdapter(), "-c", "1000"));

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(NrOfTokens.class),
                                  TcFeatureFactory.create(LuceneCharacterNGram.class)
                                  )
                );

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets,
                dimClassificationArguments);

        return pSpace;
    }

    private static void unitLoadAndUseModel(File modelFolder)
        throws Exception
    {
        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());

        CollectionReader reader = CollectionReaderFactory.createReader(TeiReader.class,
                TeiReader.PARAM_SOURCE_LOCATION, unitTrainFolder, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_PATTERNS, Arrays.asList(TeiReader.INCLUDE_PREFIX + "a02.xml"));

        List<TextClassificationOutcome> outcomes = new ArrayList<>();
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        reader.getNext(jcas.getCas());

        tcAnno.process(jcas);

        outcomes.addAll(JCasUtil.select(jcas, TextClassificationOutcome.class));

        // int i=0;
        // for(TextClassificationOutcome o: outcomes){
        // System.out.println("assertEquals(\"" + o.getOutcome() + "\",
        // outcomes.get("+(i++)+").getOutcome());");
        // }

        assertEquals(31, outcomes.size());
        assertEquals("AT", outcomes.get(0).getOutcome());
        assertEquals("IN", outcomes.get(1).getOutcome());
        assertEquals("pct", outcomes.get(2).getOutcome());
        assertEquals("WDT", outcomes.get(3).getOutcome());
        assertEquals("NP", outcomes.get(4).getOutcome());
        assertEquals("VBD", outcomes.get(5).getOutcome());
        assertEquals("AT", outcomes.get(6).getOutcome());
        assertEquals("VBN", outcomes.get(7).getOutcome());
        assertEquals("RB", outcomes.get(8).getOutcome());
        assertEquals("pct", outcomes.get(9).getOutcome());
        assertEquals("NP", outcomes.get(10).getOutcome());
        assertEquals("CC", outcomes.get(11).getOutcome());
        assertEquals("AT", outcomes.get(12).getOutcome());
        assertEquals("pct", outcomes.get(13).getOutcome());
        assertEquals("JJ", outcomes.get(14).getOutcome());
        assertEquals("NN", outcomes.get(15).getOutcome());
        assertEquals("pct", outcomes.get(16).getOutcome());
        assertEquals("NP", outcomes.get(17).getOutcome());
        assertEquals("NN", outcomes.get(18).getOutcome());
        assertEquals("CC", outcomes.get(19).getOutcome());
        assertEquals("AP", outcomes.get(20).getOutcome());
        assertEquals("NN", outcomes.get(21).getOutcome());
        assertEquals("IN", outcomes.get(22).getOutcome());
        assertEquals("NNS", outcomes.get(23).getOutcome());
        assertEquals("JJ", outcomes.get(24).getOutcome());
        assertEquals("NP", outcomes.get(25).getOutcome());
        assertEquals("IN", outcomes.get(26).getOutcome());
        assertEquals("AT", outcomes.get(27).getOutcome());
        assertEquals("AT", outcomes.get(28).getOutcome());
        assertEquals("JJ", outcomes.get(29).getOutcome());
        assertEquals("pct", outcomes.get(30).getOutcome());
    }

    private static void unitTrainAndStoreModel(ParameterSpace paramSpace, File modelFolder)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel("UnitLibsvmTestSaveModel", modelFolder);
        batch.setParameterSpace(paramSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);
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
                modelFolder.getAbsolutePath() + "/" + LibsvmAdapter.getOutcomeMappingFilename());
        assertTrue(id2outcomeMapping.exists());

        File featureNameMapping = new File(modelFolder.getAbsolutePath() + "/"
                + LibsvmAdapter.getFeatureNameMappingFilename());
        assertTrue(featureNameMapping.exists());
    }
}