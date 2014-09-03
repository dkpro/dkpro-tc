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
package de.tudarmstadt.ukp.dkpro.tc.testing.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

/**
 * Single label reader that imports test files where each instance is on a new line.
 * Outcome is set to dummy values.
 */
public class LineInstanceReader
    extends JCasCollectionReader_ImplBase
    implements TCReaderSingleLabel 
{
        private static final String DEFAULT_LANGUAGE = "en";

        /**
         * Location of the input file
         */
        public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
        @ConfigurationParameter(name=PARAM_SOURCE_LOCATION, mandatory=true)
        private String inputFileString;

        /**
         * Language of the input data
         */
        public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
        @ConfigurationParameter(name=PARAM_LANGUAGE, mandatory=false, defaultValue=DEFAULT_LANGUAGE)
        private String language;

        /**
         * Encoding of the input file
         */
        public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
        @ConfigurationParameter(name=PARAM_ENCODING, mandatory=false, defaultValue="UTF-8")
        private String encoding;

        private int currentIndex;
        private String nextLine;

        private BufferedReader bufferedReader;
                        
        @Override
        public void initialize(UimaContext aContext) throws ResourceInitializationException {
            
            try {
                URL resolvedURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);

                if (resolvedURL.getFile().endsWith(".gz")) {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(
                                    new GZIPInputStream(resolvedURL.openStream()),
                                    encoding
                            )
                    );
                }
                else {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(
                                    resolvedURL.openStream(),
                                    encoding
                            )
                    );
                }
            }
            catch (Exception e) {
                throw new ResourceInitializationException(e);
            }

            currentIndex = 0;
        }

    @Override
    public boolean hasNext()
        throws IOException 
    {
        if (nextLine == null) {
            nextLine = bufferedReader.readLine();
        }
        
        return nextLine != null;
    }

    @Override
    public void getNext(JCas jcas) throws IOException, CollectionException {

        // set language if it was explicitly specified as a configuration parameter
        if (language != null) {
            jcas.setDocumentLanguage(language);
        }

        jcas.setDocumentText(nextLine);

        DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
        docMetaData.setDocumentTitle(Integer.toString(currentIndex));
        docMetaData.setDocumentUri(inputFileString);
        docMetaData.setDocumentId(Integer.toString(currentIndex));
        docMetaData.setCollectionId(Integer.toString(currentIndex));

        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(getTextClassificationOutcome(jcas));
        outcome.addToIndexes();
        
        currentIndex++;
        
        // set to null, so that next call of hasNext() tries to fill the item again
        nextLine = null;
    }
    
    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return Integer.toString(currentIndex / 2);
    }
}