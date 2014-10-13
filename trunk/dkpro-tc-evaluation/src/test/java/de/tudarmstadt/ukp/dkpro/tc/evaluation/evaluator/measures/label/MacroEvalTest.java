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

import static org.junit.Assert.*;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;

public class MacroEvalTest {

	@Test
	public void macroPrecisionTest() {
		ContingencyTable cTable = new ContingencyTable(1);
		cTable.addTruePositives(0, 3);
		cTable.addFalsePositives(0, 1);
		cTable.addFalseNegatives(0, 2);
		cTable.addTrueNegatives(0, 4);
		
		assertEquals(0.7, MacroAccuracy.calculate(cTable, true), 0.01);
		assertEquals(0.75, MacroPrecision.calculate(cTable, true), 0.01);
		assertEquals(0.6, MacroRecall.calculate(cTable, true), 0.01);
		assertEquals(0.66, MacroFScore.calculate(cTable, true), 0.01);
	}
}
