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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;

public class MacroEvalTest {

	@Test
	public void macroPrecisionTest() {
		ContingencyTable cTable = new ContingencyTable("class1");
		cTable.addTruePositives("class1", 3);
		cTable.addFalsePositives("class1", 1);
		cTable.addFalseNegatives("class1", 2);
		cTable.addTrueNegatives("class1", 4);
		
		Double macroAcc = MacroAccuracy.calculate(cTable, true).get(MacroAccuracy.class.getSimpleName());
		assertEquals(0.7, macroAcc, 0.01);
		
		Double macroPr = MacroPrecision.calculate(cTable, true).get(MacroPrecision.class.getSimpleName());
		assertEquals(0.75, macroPr, 0.01);
		
		Double macroRe = MacroRecall.calculate(cTable, true).get(MacroRecall.class.getSimpleName());
		assertEquals(0.6, macroRe, 0.01);
		
		Double macroFSc = MacroFScore.calculate(cTable, true).get(MacroFScore.class.getSimpleName());
		assertEquals(0.66, macroFSc, 0.01);
	}
}