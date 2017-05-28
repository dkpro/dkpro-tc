/**
 * Copyright 2017
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
package org.dkpro.tc.examples.deeplearning.dl4j;

import org.dkpro.tc.examples.deeplearning.dl4j.doc.DeepLearningDl4jDocumentTrainTest;
import org.dkpro.tc.examples.deeplearning.dl4j.seq.DeepLearningDl4jSeq2SeqTrainTest;
import org.junit.Test;

public class Dl4jTest {
	@Test
	public void runSequenceTest() throws Exception {
		DeepLearningDl4jSeq2SeqTrainTest dl4j = new DeepLearningDl4jSeq2SeqTrainTest();
		dl4j.runTrainTest(DeepLearningDl4jSeq2SeqTrainTest.getParameterSpace());
	}
	
	@Test
	public void runDocumentTest() throws Exception {
		DeepLearningDl4jDocumentTrainTest dl4j = new DeepLearningDl4jDocumentTrainTest();
		dl4j.runTrainTest(DeepLearningDl4jDocumentTrainTest.getParameterSpace());
	}
}
