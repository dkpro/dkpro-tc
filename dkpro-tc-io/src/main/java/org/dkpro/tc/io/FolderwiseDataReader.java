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
package org.dkpro.tc.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This reader is suited when several text documents (without any labels) are placed in a folder and
 * the folder name is a suited label.
 * 
 * The text of a file in a folder is read as self-contained document into a JCas and the entire
 * text-span is the {@link org.dkpro.tc.api.type.TextClassificationTarget}. The folder name is set as the expected 
 * {@link org.dkpro.tc.api.type.TextClassificationOutcome}.
 */
public class FolderwiseDataReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * The reader annotates by default the token as {@link TextClassificationTarget} and the read
     * category label as {@link TextClassificationOutcome}. When using this reader together with a
     * trained model, it might be necessary to suppress these annotations as downstream components
     * provide them. This switch turns off the automatic annotation.
     */
    public static final String PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS = "PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS";
    @ConfigurationParameter(name = PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS, mandatory = true, defaultValue = "true")
    protected boolean addBackendTcAnnotations;

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {

        Resource currentFile = nextFile();

        DocumentMetaData metaData = new DocumentMetaData(aJCas);
        metaData.setDocumentTitle(currentFile.getResource().getFile().getName());
        metaData.setDocumentId(currentFile.getResource().getFile().getName());
        metaData.addToIndexes();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(currentFile.getInputStream(), "utf-8"))) {

            StringBuilder buffer = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            String text = buffer.toString().trim();
            text = performAdditionalTextOperation(text);

            setTextClassificationTarget(aJCas, currentFile, 0, text.length());
            setTextClassificationOutcome(aJCas, currentFile, 0, text.length());

            aJCas.setDocumentText(text.trim());
        }
    }

    protected String performAdditionalTextOperation(String text)
    {
        // opportunity to modify token information by overloading
        return text;
    }

    protected void setTextClassificationTarget(JCas aJCas, Resource currentFile, int begin, int end)
    {
        if(!addBackendTcAnnotations) {
            return;
        }
        
        TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, begin, end);
        aTarget.addToIndexes();
    }

    protected void setTextClassificationOutcome(JCas aJCas, Resource currentFile, int begin,
            int end)
        throws IOException
    {
        if(!addBackendTcAnnotations) {
            return;
        }
        
        TextClassificationOutcome tco = new TextClassificationOutcome(aJCas, begin, end);
        tco.setOutcome(currentFile.getResource().getFile().getParentFile().getName());
        tco.addToIndexes();
    }
}
