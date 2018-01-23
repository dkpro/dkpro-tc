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
package org.dkpro.tc.features.ngram.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

import org.dkpro.tc.api.io.TCReaderSingleLabel;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class TestReaderSentenceToDocument
    extends JCasCollectionReader_ImplBase
    implements TCReaderSingleLabel
{
    /**
     * Path to the file containing the sentences
     */
    public static final String PARAM_SENTENCES_FILE = "SentencesFile";
    @ConfigurationParameter(name = PARAM_SENTENCES_FILE, mandatory = true)
    private String sentencesFile;
	
	public static final String LANGUAGE_CODE = "en";
	private int offset;
    private List<String> texts;    
    
    int jcasId=0;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        texts = new ArrayList<String>();
        try {
            URL resourceUrl = ResourceUtils.resolveLocation(sentencesFile, this, context);
            InputStream is = resourceUrl.openStream();
            for (String sentence : IOUtils.readLines(is, "UTF-8")) {
                texts.add(sentence);
            }
            is.close();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        offset = 0;
    }
    

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {        
    	// setting the document text
        aJCas.setDocumentText(texts.get(offset));
        aJCas.setDocumentLanguage(LANGUAGE_CODE);

        // as we are creating more than one CAS out of a single file, we need to have different
        // document titles and URIs for each CAS
        // otherwise, serialized CASes will be overwritten
        DocumentMetaData dmd = DocumentMetaData.create(aJCas);
        dmd.setDocumentTitle("Sentence" + offset);
        dmd.setDocumentUri("Sentence" + offset);
        dmd.setDocumentId(String.valueOf(offset));
        
        JCasId id = new JCasId(aJCas);
        id.setId(jcasId);
        id.addToIndexes();
        

        // setting the outcome / label for this document
        TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
        outcome.setOutcome(getTextClassificationOutcome(aJCas));
        outcome.addToIndexes();
        
        new TextClassificationTarget(aJCas, 0, aJCas.getDocumentText().length()).addToIndexes();

        offset++;
    }
    

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return "test";
    }
    

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return offset < texts.size();
	}
	

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(offset, texts.size(), "sentences") };
	}
}