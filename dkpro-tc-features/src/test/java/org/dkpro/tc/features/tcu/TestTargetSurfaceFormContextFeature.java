/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.features.tcu;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class TestTargetSurfaceFormContextFeature
{
    @Test
    public void testTokenFeatureExtractors()
        throws Exception
    {

        Object[] o = setUp();
        JCas jcas = (JCas) o[0];
        TextClassificationTarget tcu = (TextClassificationTarget) o[1];

        assertResult(jcas, tcu, -4, TargetSurfaceFormContextFeature.OUT_OF_BOUNDARY);
        assertResult(jcas, tcu, -3, TargetSurfaceFormContextFeature.BEG_OF_SEQUENCE);
        assertResult(jcas, tcu, -2, "it");
        assertResult(jcas, tcu, -1, "is");
        assertResult(jcas, tcu, 0, "raining");
        assertResult(jcas, tcu, +1, "all");
        assertResult(jcas, tcu, +2, "day");
        assertResult(jcas, tcu, +3, TargetSurfaceFormContextFeature.END_OF_SEQUENCE);
        assertResult(jcas, tcu, +4, TargetSurfaceFormContextFeature.OUT_OF_BOUNDARY);
    }

    private void assertResult(JCas jcas, TextClassificationTarget tcu, int i, String o) throws Exception
    {
        TargetSurfaceFormContextFeature createResource = FeatureUtil.createResource(
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class,
                TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, i));
        Set<Feature> extract = createResource.extract(jcas, tcu);

        assertEquals(1, extract.size());
        assertEquals(o, extract.iterator().next().getValue());        
    }

    private Object[] setUp()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("It is raining all day");

        DocumentMetaData dmd = new DocumentMetaData(jcas);
        dmd.setDocumentId("1");
        dmd.addToIndexes();

        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        engine.process(jcas.getCas());

        ArrayList<Token> arrayList = new ArrayList<Token>(JCasUtil.select(jcas, Token.class));

        Token bb = arrayList.get(0);
        TextClassificationTarget tcbb = new TextClassificationTarget(jcas, bb.getBegin(),
                bb.getEnd());
        tcbb.addToIndexes();

        Token b = arrayList.get(1);
        TextClassificationTarget tcb = new TextClassificationTarget(jcas, b.getBegin(), b.getEnd());
        tcb.addToIndexes();

        Token c = arrayList.get(2);
        TextClassificationTarget tcu = new TextClassificationTarget(jcas, c.getBegin(), c.getEnd());
        tcu.addToIndexes();

        Token n = arrayList.get(3);
        TextClassificationTarget tcn = new TextClassificationTarget(jcas, n.getBegin(), n.getEnd());
        tcn.addToIndexes();

        Token nn = arrayList.get(4);
        TextClassificationTarget tcnn = new TextClassificationTarget(jcas, nn.getBegin(),
                nn.getEnd());
        tcnn.addToIndexes();

        return new Object[] { jcas, tcu };
    }
}