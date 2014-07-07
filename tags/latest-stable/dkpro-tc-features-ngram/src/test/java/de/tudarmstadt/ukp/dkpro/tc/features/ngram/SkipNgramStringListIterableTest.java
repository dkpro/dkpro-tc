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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.SkipNgramStringListIterable;

public class SkipNgramStringListIterableTest
{

    @Test
    public void ngramTest_size2() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2, 2)) {
//            System.out.println(ngram);
            i++;
        }
        assertEquals(15, i);
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
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 3, 3, 2)) {
            String joined = StringUtils.join(ngram, " ");
            assertTrue(joined, ngrams.contains(joined));
//            System.out.println(ngram);
            i++;
        }
        assertEquals(ngrams.size(), i);
    }
    
    @Test
    public void ngramTest_size2_3() {
        
        String[] tokens = "A B C D E".split(" ");
       

        int i=0;
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 3, 2)) {
//            System.out.println(ngram);
            i++;
        }
        assertEquals(19, i);
    }
    @Test
    public void ngramTest_size2_tooBig(){

        String[] tokens = "A B C D E F G H I J K L M N O P".split(" ");
        
        for (List<String> ngram : new SkipNgramStringListIterable(tokens, 2, 2, 2)) {
            String joined = StringUtils.join(ngram, " ");
            assertTrue(joined, !joined.equals("A E"));
        }
    }
}
