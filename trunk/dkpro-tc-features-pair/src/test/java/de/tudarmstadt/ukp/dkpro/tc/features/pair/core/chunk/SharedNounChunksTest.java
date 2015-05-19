/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.PPipelineTestBase;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.DenseFeatureStore;

public class SharedNounChunksTest extends PPipelineTestBase

{

    private JCas jcas1;
    private JCas jcas2;

    @Before
    public void setUp()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngine engine = createEngine(desc);

        jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is the text of view 1");
        engine.process(jcas1);

        jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2");
        engine.process(jcas2);
    }

    @Test
    public void testExtract1()
        throws Exception
    {

        SharedNounChunksTest test = new SharedNounChunksTest();
        test.initialize();
        test.parameters = new Object[] { SharedNounChunks.PARAM_NORMALIZE_WITH_FIRST, false };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 1);
        assertTrue(test.featureNames.first().startsWith("SharedNounChunkView2"));
        assertEquals(0.0, test.instanceList.get(0).getFeatures().get(0).getValue());

    }

    @Test
    public void testExtract2()
        throws Exception
    {
        SharedNounChunksTest test = new SharedNounChunksTest();
        test.initialize();
        test.parameters = new Object[] { SharedNounChunks.PARAM_NORMALIZE_WITH_FIRST, true };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 1);
        assertTrue(test.featureNames.first().startsWith("SharedNounChunkView1"));
        assertEquals(0.0, test.instanceList.get(0).getFeatures().get(0).getValue());

    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList) throws ResourceInitializationException
    {
        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(parameterList,
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, DenseFeatureStore.class.getName(),
                false, false, false, false,
                SharedNounChunks.class.getName());
    }

    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory.createEngineDescription(
                NoOpAnnotator.class);
    }

}
