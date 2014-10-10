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
