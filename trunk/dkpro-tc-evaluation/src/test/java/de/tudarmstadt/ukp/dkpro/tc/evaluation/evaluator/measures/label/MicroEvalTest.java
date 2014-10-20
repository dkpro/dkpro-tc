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

package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.measures.label;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroRecall;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class MicroEvalTest {

	@Test
	public void macroPrecisionTest() {
		double[][] table = new double[][]{{3, 2},{1, 4}};
		CombinedContingencyTable cCTable = new CombinedContingencyTable(table);
		
		Double microAcc = MicroAccuracy.calculate(cCTable, true).get(MicroAccuracy.class.getSimpleName());
		assertEquals(0.7, microAcc, 0.01);
		
		Double microPr = MicroPrecision.calculate(cCTable, true).get(MicroPrecision.class.getSimpleName());
		assertEquals(0.75, microPr, 0.01);
		
		Double microRe = MicroRecall.calculate(cCTable, true).get(MicroRecall.class.getSimpleName());
		assertEquals(0.6, microRe, 0.01);
		
		Double microFSc = MicroFScore.calculate(cCTable, true).get(MicroFScore.class.getSimpleName());
		assertEquals(0.66, microFSc, 0.01);
	}
}