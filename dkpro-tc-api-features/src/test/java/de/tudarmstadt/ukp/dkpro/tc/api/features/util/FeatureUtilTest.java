/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class FeatureUtilTest
{
	private static String stopwordFileLoc  ="src/test/resources/data/MiniStopwordList.txt";
	
	@Test
    public void EscapeFeatureNameTest()
        throws Exception
    {
		assertTrue(FeatureUtil.escapeFeatureName("mY&#@\\(_feature12").equals("mYu38u35u64u92u40_feature12"));
    }
	
	@Test
    public void StopwordsListTest()
        throws Exception
    {
		Set<String> stopwords = FeatureUtil.getStopwords(stopwordFileLoc, false);
		assertEquals(stopwords.size(), 5);
		assertTrue(stopwords.contains("The"));
		assertTrue(stopwords.contains("I"));
		assertTrue(stopwords.contains("and"));
		assertTrue(stopwords.contains("ARE"));
		assertTrue(stopwords.contains("iS"));

		stopwords = FeatureUtil.getStopwords(stopwordFileLoc, true);
		assertEquals(stopwords.size(), 5);
		assertTrue(stopwords.contains("the"));
		assertTrue(stopwords.contains("i"));
		assertTrue(stopwords.contains("and"));
		assertTrue(stopwords.contains("are"));
		assertTrue(stopwords.contains("is"));
    }
}
