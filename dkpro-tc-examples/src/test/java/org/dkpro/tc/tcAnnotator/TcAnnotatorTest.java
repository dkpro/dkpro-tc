/*******************************************************************************
 * Copyright 2018
 * Language Technology Lab
 * University of Duisburg-Essen
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
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class TcAnnotatorTest {

	@Test
	public void testAnnotator() throws UIMAException{
		
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentText("This article attempts to provide a general introduction to atheism.");
		
		AnalysisEngine segmenter = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);
		segmenter.process(aJCas);
		
		String [] converter = new String [] {ConversionAnnotator.class.getName(), ConversionAnnotator.PARAM_SUFFIX, "-X"};
		AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
				TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
				TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName(), 
				TcAnnotator.PARAM_TC_MODEL_LOCATION, "src/test/resources/TcAnnotatorTestModelDummy", 
				TcAnnotator.PARAM_CONVERTION_ANNOTATOR, converter,
				TcAnnotator.PARAM_RETAIN_TARGETS, false);
		tcAnno.process(aJCas);
		
		
		assertEquals(0, JCasUtil.select(aJCas, TextClassificationTarget.class).size());
		assertEquals(11, JCasUtil.select(aJCas, POS.class).size());
		assertTrue(new ArrayList<POS>(JCasUtil.select(aJCas, POS.class)).get(0).getPosValue().endsWith("-X"));
	}
}
