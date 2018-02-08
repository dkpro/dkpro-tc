/**
 * Copyright 2018
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
package org.dkpro.tc.examples.single.sequence;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.random.RandomSVMHMMAdapter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;

/**
 * This test just ensures that the experiment runs without throwing any
 * exception.
 */
public class SVMHMMBrownPosDemoTest extends TestCaseSuperClass {
	SVMHMMBrownPosDemo javaExperiment;
	ParameterSpace pSpace;

	@Before
	public void setup() throws Exception {
		super.setup();

		javaExperiment = new SVMHMMBrownPosDemo();
	}

	@Test
	public void testRandomSVMHMM() throws Exception {
		@SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				Arrays.asList(new RandomSVMHMMAdapter()));
		pSpace = SVMHMMBrownPosDemo.getParameterSpace(true, dimClassificationArgs);
		javaExperiment.runTrainTest(pSpace);
	}

	@Test
	public void testActualSVMHMM() throws Exception {
		@SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				Arrays.asList(new SVMHMMAdapter(), "-c", "5.0", "-t", "1", "-m", "0"));

		// Dimension<List<String>> dimClassificationArgs = Dimension.create(
		// Constants.DIM_CLASSIFICATION_ARGS,
		// new ArrayList<>());

		pSpace = SVMHMMBrownPosDemo.getParameterSpace(true, dimClassificationArgs);

		javaExperiment.runTrainTest(pSpace);

		EvaluationData<String> data = Tc2LtlabEvalConverter
				.convertSingleLabelModeId2Outcome(ContextMemoryReport.id2outcomeFiles.get(0));
		Accuracy<String> acc = new Accuracy<String>(data);

		assertTrue(acc.getResult() > 0.5);

	}
}
