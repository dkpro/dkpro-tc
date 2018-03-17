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
package org.dkpro.tc.io;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

public class FolderwiseDataReaderTest
{

    @Test
    public void testReader() throws Exception
    {

        CollectionReader reader = CollectionReaderFactory.createReader(FolderwiseDataReader.class,
                FolderwiseDataReader.PARAM_SOURCE_LOCATION, "src/test/resources/folderwise/**/",
                FolderwiseDataReader.PARAM_PATTERNS, "*.txt");

        List<String> readDocuments = new ArrayList<>();
        List<String> readOutcomes = new ArrayList<>();

        while (reader.hasNext()) {
            JCas emptyCas = JCasFactory.createJCas();
            reader.getNext(emptyCas.getCas());

            readDocuments.add(JCasUtil.selectSingle(emptyCas, TextClassificationTarget.class)
                    .getCoveredText());
            readOutcomes.add(
                    JCasUtil.selectSingle(emptyCas, TextClassificationOutcome.class).getOutcome());
        }

        assertEquals(5, readDocuments.size());
        assertEquals("This is a really odd test tweet :-) #test #nonsense", readDocuments.get(0));
        assertEquals("it's raining all day and i don't care", readDocuments.get(1));
        assertEquals("This is another really odd test tweet :-) #moreTests #randomness",
                readDocuments.get(2));
        assertEquals("dkpro tc is a wonderful tool to classify tweets #LoveIt #MachineLearning",
                readDocuments.get(3));
        assertEquals("Not even close to Friday :( #IHateMonday", readDocuments.get(4));

        assertEquals(5, readOutcomes.size());
        assertEquals("neutral", readOutcomes.get(0));
        assertEquals("neutral", readOutcomes.get(1));
        assertEquals("neutral", readOutcomes.get(2));
        assertEquals("emotional", readOutcomes.get(3));
        assertEquals("emotional", readOutcomes.get(4));
    }

}
