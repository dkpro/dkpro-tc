/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SparseFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.ml.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.random.RandomSVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.OriginalTextHolderFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Habernal
 */
@RunWith(JUnit4.class)
public class SequenceClassificationTest
{
    private static final int NUM_FOLDS = 2;
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Before
    public void setUp()
            throws Exception
    {

        // create 10 XMI files in temp folder
        for (int i = 0; i < 10; i++) {
            JCas newJCasWithText = createAnnotatedCas(i);

            SimplePipeline.runPipeline(newJCasWithText,
                    AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                            XmiWriter.PARAM_TARGET_LOCATION, tempFolder.getRoot())
            );
        }
    }

    /**
     * Sequence reader
     */
    public static class TestingSequenceReader
            extends XmiReader
            implements TCReaderSequence
    {

        @Override public void getNext(CAS aCAS)
                throws IOException, CollectionException
        {
            super.getNext(aCAS);

            JCas jCas = null;
            try {
                jCas = aCAS.getJCas();
            }
            catch (CASException e) {
                throw new CollectionException(e);
            }

            // make sure there are 4 tokens
            Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
            assertEquals(4, tokens.size());

            // create text classification sequence
            TextClassificationSequence textClassificationSequence = new TextClassificationSequence(jCas);
            textClassificationSequence.setBegin(0);
            textClassificationSequence.setEnd(jCas.getDocumentText().length());
            textClassificationSequence.addToIndexes();

            for (Token token : tokens) {
                List<ROOT> roots = JCasUtil.selectCovering(ROOT.class, token);
                // each token belongs to a single ROOT annotation
                assertEquals(1, roots.size());

                // create unit on token level
                TextClassificationUnit unit = new TextClassificationUnit(jCas, token.getBegin(),
                        token.getEnd());
                unit.addToIndexes();

                // create some outcome
                TextClassificationOutcome outcome = new TextClassificationOutcome(jCas,
                        token.getBegin(), token.getEnd());
                outcome.setOutcome("someOutcome");
                outcome.addToIndexes();
            }

            // make sure again we have 4 classification units
            Collection<TextClassificationUnit> textClassificationUnits = JCasUtil
                    .select(jCas, TextClassificationUnit.class);
            assertEquals(4, textClassificationUnits.size());

            // so make sure again each unit has a covering ROOT annotations
            for (TextClassificationUnit unit : textClassificationUnits) {
                List<ROOT> roots = JCasUtil.selectCovering(ROOT.class, unit);
                assertEquals(1, roots.size());
            }
        }

        @Override
        public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
                throws CollectionException
        {
            // empty
            return null;
        }
    }

    /**
     * Testing feature that is planned to work with the ROOT annotation; but returns a random value
     */
    public static class TestingFeature
            extends FeatureExtractorResource_ImplBase
            implements ClassificationUnitFeatureExtractor
    {

        @Override public List<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
                throws TextClassificationException
        {
            List<ROOT> roots = JCasUtil.selectCovering(ROOT.class, classificationUnit);
            // make sure the ROOT annotation is still there
            assertEquals(1, roots.size());

            return Arrays.asList(new Feature("randomFeature",
                    new Random(System.currentTimeMillis()).nextInt(5)));
        }
    }

    @Test
    public void testClassify()
            throws Exception

    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("SequenceLabelingCV",
                // random classifier
                new RandomSVMHMMAdapter(),
                // no additional annotations
                AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class),
                NUM_FOLDS);
        batch.setParameterSpace(getParamSpace());
        batch.setExecutionPolicy(BatchTask.ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(batch);
    }

    @SuppressWarnings("unchecked")
    protected ParameterSpace getParamSpace()
    {
        Map<String, Object> dimReaders = new HashMap<>();
        dimReaders.put(Constants.DIM_READER_TRAIN, TestingSequenceReader.class);
        dimReaders.put(
                Constants.DIM_READER_TRAIN_PARAMS,
                Arrays.asList(
                        TestingSequenceReader.PARAM_SOURCE_LOCATION, tempFolder.getRoot(),
                        TestingSequenceReader.PARAM_PATTERNS,
                        TestingSequenceReader.INCLUDE_PREFIX + "*.xmi"));

        Dimension<List<String>> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
                Arrays.asList(
                        OriginalTextHolderFeatureExtractor.class.getName(),
                        TestingFeature.class.getName())
        );

        return new ParameterSpace(
                Dimension.createBundle("readers", dimReaders),
                Dimension.create(Constants.DIM_DATA_WRITER, SVMHMMDataWriter.class.getName()),
                Dimension.create(Constants.DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(Constants.DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                Dimension.create(Constants.DIM_FEATURE_STORE, SparseFeatureStore.class.getName()),
                dimFeatureSets
        );

    }

    /**
     * Creates JCas with 4 Token, 1 Sentence, and 1 ROOT annotations
     *
     * @param id id
     * @return jcas
     * @throws Exception
     */
    public static JCas createAnnotatedCas(int id)
            throws Exception
    {
        JCas jCas = JCasFactory.createJCas();
        DocumentMetaData d = new DocumentMetaData(jCas);
        d.setDocumentId(Integer.toString(id));
        d.setDocumentUri("Fake URI");
        d.addToIndexes();
        jCas.setDocumentText("This is test.");

        Token token;
        token = new Token(jCas, 0, 4);
        token.addToIndexes();

        token = new Token(jCas, 5, 7);
        token.addToIndexes();

        token = new Token(jCas, 8, 12);
        token.addToIndexes();

        token = new Token(jCas, 12, 13);
        token.addToIndexes();

        Sentence sentence = new Sentence(jCas, 0, 13);
        sentence.addToIndexes();

        ROOT root = new ROOT(jCas, 0, 13);
        root.addToIndexes();

        return jCas;
    }

    /**
     * Creates the reader for the 10 generated files
     *
     * @return reader
     * @throws ResourceInitializationException
     */
    private CollectionReaderDescription getReaderDescription()
            throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                TestingSequenceReader.class, TestingSequenceReader.PARAM_SOURCE_LOCATION,
                tempFolder.getRoot(),
                TestingSequenceReader.PARAM_PATTERNS,
                TestingSequenceReader.INCLUDE_PREFIX + "*.xmi"
        );
    }

    /**
     * Reads the collection and dumps the cases
     *
     * @throws Exception
     */
    @Test
    public void testReaderSanity()
            throws Exception
    {
        SimplePipeline.runPipeline(
                getReaderDescription(),
                AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class)
        );
    }
}
