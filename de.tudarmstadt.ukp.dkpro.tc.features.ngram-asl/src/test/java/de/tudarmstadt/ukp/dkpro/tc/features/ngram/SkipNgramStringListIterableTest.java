package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SkipNgramStringListIterableTest
{

    @Test
    public void ngramTest_size2() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2)) {
            if (i==0) {
                assertEquals(2, ngram.size());
                assertEquals("This is", StringUtils.join(ngram, " "));
            }
            
            System.out.println(ngram);
            i++;
        }
        assertEquals(21, i);
    }
    
    @Test
    public void ngramTest_size3() {
        
        String[] tokens = "A B C D E".split(" ");
        
        Set<String> ngrams = new HashSet<String>();
        ngrams.add("A B C");
        ngrams.add("B C D");
        ngrams.add("C D E");
        ngrams.add("A B D");
        ngrams.add("A B E");
        ngrams.add("A C D");
        ngrams.add("A D E");
        ngrams.add("B C E");
        ngrams.add("B D E");
        ngrams.add("A C E");

        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 3, 3)) {
            String joined = StringUtils.join(ngram, " ");
            assertTrue(joined, ngrams.contains(joined));
            System.out.println(ngram);
            i++;
        }
        assertEquals(ngrams.size(), i);
    }
    
    @Test
    public void ngramTest_size2_3() {
        
        String[] tokens = "A B C D E".split(" ");
       

        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 3)) {
            System.out.println(ngram);
            i++;
        }
        assertEquals(20, i);
    }
    @Test
    public void ngramTest_size2_tooBig(){

        String[] tokens = "A B C D E F G H I J K L M N O P".split(" ");
        
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2)) {
        	List<String> aAndP = new ArrayList<String>();
        	aAndP.add("A");
        	aAndP.add("P");
        	if(ngram.equals(aAndP)){
        		System.out.println(ngram);
        		System.out.println("Tokens = " + StringUtils.join(tokens));
        		System.out.println("Do we really want A and P together in an ngram?");
        		System.out.println("Guthrie et al provide an adjustable limit of 1- or 2-skips.");
        	}
        }
    }
}
