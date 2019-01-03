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
package org.dkpro.tc.tcAnnotator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.ml.model.PreTrainedModelProviderSequenceMode;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class ModelProviderConversionFeatureTest
{

    @Test
    public void testAnnotator() throws UIMAException
    {

        JCas aJCas = JCasFactory.createJCas();
        aJCas.setDocumentText(
                "This article attempts to provide a general introduction to atheism.");

        AnalysisEngine segmenter = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);
        segmenter.process(aJCas);

        String[] converter = new String[] { ConversionAnnotator.class.getName(),
                ConversionAnnotator.PARAM_SUFFIX, "-X" };
        AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(
        		PreTrainedModelProviderSequenceMode.class,
        		PreTrainedModelProviderSequenceMode.PARAM_ADD_TC_BACKEND_ANNOTATION, true,
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
        		PreTrainedModelProviderSequenceMode.PARAM_NAME_TARGET_ANNOTATION, Token.class.getName(),
        		PreTrainedModelProviderSequenceMode.PARAM_TC_MODEL_LOCATION, "src/test/resources/TcAnnotatorTestModelDummy",
        		PreTrainedModelProviderSequenceMode.PARAM_CONVERTION_ANNOTATOR, converter, 
        		PreTrainedModelProviderSequenceMode.PARAM_RETAIN_TARGETS, false);
        tcAnno.process(aJCas);

        assertEquals(0, JCasUtil.select(aJCas, TextClassificationTarget.class).size());
        assertEquals(11, JCasUtil.select(aJCas, POS.class).size());
        assertTrue(new ArrayList<POS>(JCasUtil.select(aJCas, POS.class)).get(0).getPosValue()
                .endsWith("-X"));
    }
}
