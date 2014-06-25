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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

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
    
    @Test
    public void phoneticNgramsTest() throws Exception {
    	String text = "This is a big house";
    	JCas jcas = JCasFactory.createJCas();
    	jcas.setDocumentLanguage("en");
    	jcas.setDocumentText(text);
        JCasBuilder cb = new JCasBuilder(jcas);
        for (String token : text.split(" ")) {
            cb.add(token, Token.class);
        }
        cb.add(0, Sentence.class);
                
        FrequencyDistribution<String> ngrams = NGramUtils.getDocumentPhoneticNgrams(jcas, 1, 3);

        assertEquals(12, ngrams.getN());
        assertTrue(ngrams.contains("I000"));
        assertTrue(ngrams.contains("T200"));
    }
}