package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class NGramUtilsTest
{

    @Test
    public void passesNGramFilterTest() {
        Set<String> stopwords = new HashSet<String>();
        stopwords.add("a");
        stopwords.add("the");
        
        List<String> list1 = Arrays.asList("a", "house");
        
        assertTrue(NGramUtils.passesNgramFilter(
                list1, stopwords, false));
        assertFalse(NGramUtils.passesNgramFilter(
                list1, stopwords, true));

        List<String> list2 = Arrays.asList("a", "the");
        
        assertFalse(NGramUtils.passesNgramFilter(
                list2, stopwords, false));
        assertFalse(NGramUtils.passesNgramFilter(
                list2, stopwords, true));
        
        List<String> list3 = Arrays.asList("green", "house");
        
        assertTrue(NGramUtils.passesNgramFilter(
                list3, stopwords, false));
        assertTrue(NGramUtils.passesNgramFilter(
                list3, stopwords, true));
    }
    
    @Test
    public void passesNGramFilterTest_emptyStopwords() {
        Set<String> stopwords = new HashSet<String>();
        
        List<String> list1 = Arrays.asList("A", "house");
        
        assertTrue(NGramUtils.passesNgramFilter(
                list1, stopwords, false));
        assertTrue(NGramUtils.passesNgramFilter(
                list1, stopwords, true));
        
        List<String> list2 = Arrays.asList("a", "the");
        
        assertTrue(NGramUtils.passesNgramFilter(
                list2, stopwords, false));
        assertTrue(NGramUtils.passesNgramFilter(
                list2, stopwords, true));
        
    }
}
