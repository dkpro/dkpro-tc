/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.io;

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
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.examples.single.document.SimpleDkproTCReaderDemo;

/**
 * A very basic DKPro TC reader, which reads sentences from a text file and labels from another text
 * file. It is used in {@link SimpleDkproTCReaderDemo}.
 * 
 */
public class SimpleDkproTCReader
    extends JCasCollectionReader_ImplBase
    implements TCReaderSingleLabel

{
    /**
     * Character encoding of the input data
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Language of the input data
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    /**
     * Path to the file containing the sentences
     */
    public static final String PARAM_SENTENCES_FILE = "SentencesFile";
    @ConfigurationParameter(name = PARAM_SENTENCES_FILE, mandatory = true)
    private String sentencesFile;

    /**
     * Path to the file containing the gold standard labels
     */
    public static final String PARAM_GOLD_LABEL_FILE = "GoldLabelFile";
    @ConfigurationParameter(name = PARAM_GOLD_LABEL_FILE, mandatory = true)
    private String goldLabelFile;

    private List<String> golds;
    private List<String> texts;

    private int offset;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        // read file with gold labels
        golds = new ArrayList<String>();
        try {
            URL resourceUrl = ResourceUtils.resolveLocation(goldLabelFile, this, context);
            InputStream is = resourceUrl.openStream();
            for (String label : IOUtils.readLines(is, encoding)) {
                golds.add(label);
            }
            is.close();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        texts = new ArrayList<String>();
        try {
            URL resourceUrl = ResourceUtils.resolveLocation(sentencesFile, this, context);
            InputStream is = resourceUrl.openStream();
            for (String sentence : IOUtils.readLines(is, encoding)) {
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
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return offset < texts.size();
    }

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        // setting the document text
        aJCas.setDocumentText(texts.get(offset));
        aJCas.setDocumentLanguage(language);

        // as we are creating more than one CAS out of a single file, we need to have different
        // document titles and URIs for each CAS
        // otherwise, serialized CASes will be overwritten
        DocumentMetaData dmd = DocumentMetaData.create(aJCas);
        dmd.setDocumentTitle("Sentence" + offset);
        dmd.setDocumentUri("Sentence" + offset);
        dmd.setDocumentId(String.valueOf(offset));

        // setting the outcome / label for this document
        TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
        outcome.setOutcome(getTextClassificationOutcome(aJCas));
        outcome.addToIndexes();

        offset++;
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return golds.get(offset);
    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(offset, texts.size(), "sentences") };
    }
}