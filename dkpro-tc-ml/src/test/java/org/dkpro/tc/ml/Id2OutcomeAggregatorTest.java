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
package org.dkpro.tc.ml;

import static org.junit.Assert.*;

import java.io.File;

import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.util.ID2OutcomeCombiner;
import org.junit.Test;

public class Id2OutcomeAggregatorTest {
	
	@Test(expected=IllegalArgumentException.class) 
	public void testWrongFormat() throws Exception{
		ID2OutcomeCombiner<String> aggregator = new ID2OutcomeCombiner<>(Constants.LM_SINGLE_LABEL);
		aggregator.add(null, Constants.LM_MULTI_LABEL);
	}
	
	@Test
	public void testAggregatorSingleLabel() throws Exception{
		ID2OutcomeCombiner<String> aggregator = new ID2OutcomeCombiner<>(Constants.LM_SINGLE_LABEL);
		aggregator.add(new File("src/test/resources/id2outcome/singleLabelID2outcome_1.txt"), Constants.LM_SINGLE_LABEL);
		aggregator.add(new File("src/test/resources/id2outcome/singleLabelID2outcome_2.txt"), Constants.LM_SINGLE_LABEL);
		
		assertEquals(18, aggregator.generateId2OutcomeFile().split("\n").length);
	}
	
	@Test
	public void testAggregatorRegression() throws Exception{
		ID2OutcomeCombiner<String> aggregator = new ID2OutcomeCombiner<>(Constants.LM_REGRESSION);
		aggregator.add(new File("src/test/resources/id2outcome/regressionID2outcome_1.txt"), Constants.LM_REGRESSION);
		aggregator.add(new File("src/test/resources/id2outcome/regressionID2outcome_2.txt"), Constants.LM_REGRESSION);
		
		assertEquals(8, aggregator.generateId2OutcomeFile().split("\n").length);
		System.out.println(aggregator.generateId2OutcomeFile());
	}
	
	@Test
	public void testAggregatorMultilabel() throws Exception{
		ID2OutcomeCombiner<String> aggregator = new ID2OutcomeCombiner<>(Constants.LM_MULTI_LABEL);
		aggregator.add(new File("src/test/resources/id2outcome/multiLabelId2outcome_1.txt"), Constants.LM_MULTI_LABEL);
		aggregator.add(new File("src/test/resources/id2outcome/multiLabelId2outcome_2.txt"), Constants.LM_MULTI_LABEL);
		
		assertEquals(8, aggregator.generateId2OutcomeFile().split("\n").length);
	}

}
