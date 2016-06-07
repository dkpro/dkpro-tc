/**
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
 */

package org.dkpro.tc.core.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TestTaskUtils
{

    //test numeration for unit mode i.e. no sequence
    @Test
    public void testUnitModeInstanceNumbering()
        throws Exception
    {
        JCas jCas = initJCas(true);

        FeatureExtractorResource_ImplBase[] featureExtractors = {};
        List<Instance> multipleInstances = TaskUtils.getMultipleInstancesUnitMode(
                featureExtractors, jCas, true);

        assertEquals(6, multipleInstances.size());

        int idx = 0;
        assertEquals("4711_0_a", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(0, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 1;
        assertEquals("4711_1_car", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(1, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 2;
        assertEquals("4711_2_drives", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(2, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());

        idx = 3;
        assertEquals("4711_3_the", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(3, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 4;
        assertEquals("4711_4_hedgehogs", multipleInstances.get(idx).getFeatures().iterator()
                .next().getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(4, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 5;
        assertEquals("4711_5_dies", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(5, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());
    }

    @Test
    public void testInstanceMultiplicationWithUnitId()
        throws Exception
    {
        JCas jCas = initJCas(true);

        FeatureExtractorResource_ImplBase[] featureExtractors = {};
        List<Instance> multipleInstances = TaskUtils.getMultipleInstancesSequenceMode(
                featureExtractors, jCas, true);

        assertEquals(6, multipleInstances.size());

        int idx = 0;
        assertEquals("4711_0_0_a", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(0, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 1;
        assertEquals("4711_0_1_car", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(1, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 2;
        assertEquals("4711_0_2_drives", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(2, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());

        idx = 3;
        assertEquals("4711_1_0_the", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(0, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 4;
        assertEquals("4711_1_1_hedgehogs", multipleInstances.get(idx).getFeatures().iterator()
                .next().getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(1, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 5;
        assertEquals("4711_1_2_dies", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(2, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());
    }

    @Test
    public void testInstanceMultiplicationWithoutUnitId()
        throws Exception
    {
        JCas jCas = initJCas(false);

        FeatureExtractorResource_ImplBase[] featureExtractors = {};
        List<Instance> multipleInstances = TaskUtils.getMultipleInstancesSequenceMode(
                featureExtractors, jCas, true);

        assertEquals(6, multipleInstances.size());

        // Sequence 1
        int idx = 0;
        assertEquals("4711_0_0", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(0, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 1;
        assertEquals("4711_0_1", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(1, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 2;
        assertEquals("4711_0_2", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(0, multipleInstances.get(idx).getSequenceId());
        assertEquals(2, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());

        // Sequence 2
        idx = 3;
        assertEquals("4711_1_0", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(0, multipleInstances.get(idx).getSequencePosition());
        assertEquals("DT", multipleInstances.get(idx).getOutcome());

        idx = 4;
        assertEquals("4711_1_1", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(1, multipleInstances.get(idx).getSequencePosition());
        assertEquals("NN", multipleInstances.get(idx).getOutcome());

        idx = 5;
        assertEquals("4711_1_2", multipleInstances.get(idx).getFeatures().iterator().next()
                .getValue());
        assertEquals(1, multipleInstances.get(idx).getSequenceId());
        assertEquals(2, multipleInstances.get(idx).getSequencePosition());
        assertEquals("VBZ", multipleInstances.get(idx).getOutcome());
    }

    private JCas initJCas(boolean setUnitIdAsPartOfTheInstanceId)
        throws Exception
    {
        AnalysisEngine engine = AnalysisEngineFactory.createEngine(NoOpAnnotator.class);
        JCas jCas = engine.newJCas();
        
        JCasId id = new JCasId(jCas);
        id.setId(4711);
        id.addToIndexes();

        DocumentMetaData meta = new DocumentMetaData(jCas);
        meta.setDocumentTitle("title");
        meta.setDocumentId("4711");
        meta.addToIndexes();

        String[][] tokens = { { "a", "DT" }, { "car", "NN" }, { "drives", "VBZ" }, // sequence 1
                { "the", "DT" }, { "hedgehogs", "NN" }, { "dies", "VBZ" } // sequence 2
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            int start = sb.length();
            int end = start + tokens[i][0].length();

            TextClassificationTarget unit = new TextClassificationTarget(jCas, start, end);
            if (setUnitIdAsPartOfTheInstanceId) {
                unit.setSuffix(tokens[i][0]);
            }
            unit.setId(i);
            unit.addToIndexes();

            TextClassificationOutcome outcome = new TextClassificationOutcome(jCas, start, end);
            outcome.setOutcome(tokens[i][1]);
            outcome.addToIndexes();

            sb.append(tokens[i][0]);
            if (i + 1 < tokens.length) {
                sb.append(" ");
            }
        }
        String text = sb.toString();
        jCas.setDocumentText(text);

        int lenSeq1 = tokens[0][0].length() + 1 + tokens[1][0].length() + 1 + tokens[2][0].length();
        TextClassificationSequence seq1 = new TextClassificationSequence(jCas, 0, lenSeq1);
        seq1.addToIndexes();
        TextClassificationSequence seq2 = new TextClassificationSequence(jCas, lenSeq1 + 1,
                text.length());
        seq2.addToIndexes();

        return jCas;
    }
}
