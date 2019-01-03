/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.ngram.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class TestReaderSingleLabelUnitReader
    extends TextReader
{
    public static final String PARAM_SUPPRESS_DOCUMENT_ANNOTATION = "PARAM_SUPPRESS_DOCUMENT_ANNOTATION";
    @ConfigurationParameter(name = "PARAM_SUPPRESS_DOCUMENT_ANNOTATION", mandatory = true, defaultValue = "false")
    private boolean suppress;

    int jcasId;

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        super.getNext(aCAS);

        JCas jcas;
        try {
            jcas = aCAS.getJCas();
            JCasId id = new JCasId(jcas);
            id.setId(jcasId++);
            id.addToIndexes();
        }
        catch (CASException e) {
            throw new CollectionException();
        }
        
        String documentText = aCAS.getDocumentText();
        
        int s=0;
        for(String t : documentText.split(" ")) {
            int e = documentText.indexOf(t,s) + t.length();
            new TextClassificationTarget(jcas, s, e).addToIndexes();
            new TextClassificationOutcome(jcas, s, e).addToIndexes();
            s+=1;
        }
    }
}