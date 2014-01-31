package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class FeatureUtilTest
{
	private static String stopwordFileLoc  ="src/test/resources/data/MiniStopwordList.txt";
	
	@Test
    public void FeatureUtilTest()
        throws Exception
    {
		System.out.println(FeatureUtil.escapeFeatureName("mY&#@\\(_feature12"));
		assertTrue(FeatureUtil.escapeFeatureName("mY&#@\\(_feature12").equals("mYu38u35u64u92u40_feature12"));
		
		
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
