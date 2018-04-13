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

public class DelimiterSeparatedValuesReaderTest
{

    @Test
    public void testReader() throws Exception
    {

        CollectionReader reader = CollectionReaderFactory.createReader(
                DelimiterSeparatedValuesReader.class, DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 2,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/semEval2017Task4/", DelimiterSeparatedValuesReader.PARAM_PATTERNS,
                "*.txt");

        List<String> readDocumentSpans = new ArrayList<>();
        List<String> readOutcomes = new ArrayList<>();

        while (reader.hasNext()) {
            JCas emptyCas = JCasFactory.createJCas();
            reader.getNext(emptyCas.getCas());

            readDocumentSpans.add(JCasUtil.selectSingle(emptyCas, TextClassificationTarget.class)
                    .getCoveredText());
            readOutcomes.add(
                    JCasUtil.selectSingle(emptyCas, TextClassificationOutcome.class).getOutcome());
        }

        assertEquals(15, readDocumentSpans.size());
        assertEquals(15, readOutcomes.size());
    }

}
