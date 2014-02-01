package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SkipNgramStringListIterableTest
{

    @Test
    public void ngramTest_size2_skip1() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2, 1)) {
            if (i==0) {
                assertEquals(2, ngram.size());
                assertEquals("This a", StringUtils.join(ngram, " "));
            }
            
            System.out.println(ngram);
            i++;
        }
        assertEquals(4, i);
    }
   
    @Test
    public void ngramTest_size2_skip2() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2, 2)) {
            System.out.println(ngram);
            i++;
        }
        assertEquals(7, i);
    }
    
    @Test
    public void ngramTest2() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 3, 2)) {
            
            System.out.println(ngram);
            i++;
        }
        assertEquals(12, i);
    }
}
