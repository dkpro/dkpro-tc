/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.pair.core.chunk;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.pair.core.ngram.PPipelineTestBase;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class SharedNounChunksTest
    extends PPipelineTestBase

{

    private JCas jcas1;
    private JCas jcas2;

    int jcasId;

    @Before
    public void setUp()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngine engine = createEngine(desc);

        jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is the text of view 1");
        JCasId id = new JCasId(jcas1);
        id.setId(jcasId++);
        id.addToIndexes();
        engine.process(jcas1);

        jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2");
        id = new JCasId(jcas2);
        id.setId(jcasId++);
        id.addToIndexes();
        engine.process(jcas2);
    }

    @Test
    public void testExtract1()
        throws Exception
    {

        SharedNounChunksTest test = new SharedNounChunksTest();
        test.initialize();
        test.parameters = new Object[] { SharedNounChunks.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                SharedNounChunks.PARAM_NORMALIZE_WITH_FIRST, false };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 1);
        assertTrue(test.featureNames.first().startsWith("SharedNounChunkView2"));
        assertEquals(0.0, test.instanceList.get(0).getFeatures().iterator().next().getValue());

    }

    @Test
    public void testExtract2()
        throws Exception
    {
        SharedNounChunksTest test = new SharedNounChunksTest();
        test.initialize();
        test.parameters = new Object[] { SharedNounChunks.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                SharedNounChunks.PARAM_NORMALIZE_WITH_FIRST, true };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 1);
        assertTrue(test.featureNames.first().startsWith("SharedNounChunkView1"));
        assertEquals(0.0, test.instanceList.get(0).getFeatures().iterator().next().getValue());

    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(SharedNounChunks.class,
                        toString(parameterList.toArray()));
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, DenseFeatureStore.class.getName(),
                false, false, false, new ArrayList<>(), false, fes);
    }

    private Object[] toString(Object[] array)
    {
        List<Object> out = new ArrayList<>();
        for (Object o : array) {
            out.add(o.toString());
        }

        return out.toArray();
    }

    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
    }

}
