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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This is a line-wise reader which reads textual information and an associated outcome from a
 * single line of text that is separated by a character, which allows separating both information.
 * Each line in a text file is read as own document into a JCas
 * 
 * The text is annotated as {@link org.dkpro.tc.api.type.TextClassificationTarget} and the outcome
 * is annotated as {@link org.dkpro.tc.api.type.TextClassificationOutcome}, which are the data types
 * DKPro TC requires for processing the read data.
 */
public class DelimiterSeparatedValuesReader
    extends JCasResourceCollectionReader_ImplBase
{

    /**
     * The separating character that separates textual information from its outcome, i.e. a label or
     * a numerical value. Defaults to TAB
     */
    public static final String PARAM_DELIMITER_CHAR = "PARAM_DELIMITER_CHAR";
    @ConfigurationParameter(name = PARAM_DELIMITER_CHAR, mandatory = true, defaultValue = "\t")
    protected String delimiter;

    /**
     * Sets a character sequence which is checked for every read token and skips an entry if found
     */
    public static final String PARAM_SKIP_LINES_START_WITH_STRING = "PARAM_SKIP_LINES_START_WITH_STRING";
    @ConfigurationParameter(name = PARAM_SKIP_LINES_START_WITH_STRING, mandatory = false)
    protected String skipLinePrefix;

    /**
     * Each line in the file is split by the delimiter character and the token at index zero is
     * assumed to be the token. This variable allows to change the default value.
     */
    public static final String PARAM_TEXT_INDEX = "PARAM_TEXT_INDEX";
    @ConfigurationParameter(name = PARAM_TEXT_INDEX, mandatory = true, defaultValue = "0")
    protected Integer textIdx;

    /**
     * Each line in the file is split by the delimiter character and the value at index one is
     * assumed to be the label/category of the token. This variable allows to change the default
     * value.
     */
    public static final String PARAM_OUTCOME_INDEX = "PARAM_OUTCOME_INDEX";
    @ConfigurationParameter(name = PARAM_OUTCOME_INDEX, mandatory = true, defaultValue = "1")
    protected Integer outcomeIdx;

    /**
     * The reader annotates by default the token as {@link TextClassificationTarget} and the read
     * category label as {@link TextClassificationOutcome}. When using this reader together with a
     * trained model, it might be necessary to suppress these annotations as downstream components
     * provide them. This switch turns off the automatic annotation.
     */
    public static final String PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS = "PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS";
    @ConfigurationParameter(name = PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS, mandatory = true, defaultValue = "true")
    protected boolean addBackendTcAnnotations;

    protected BufferedReader reader;

    protected String nextDocument = null;

    protected int runningId = 0;

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {

        initializeJCas(aJCas);

        StringBuilder documentText = new StringBuilder();

        String[] split = nextDocument.split(delimiter);

        String text = split[textIdx].trim();
        String outcome = split[outcomeIdx].trim();

        text = performAdditionalTextOperation(text);
        outcome = performAdditionalOutcomeOperation(outcome);

        int entryStart = documentText.length();
        int entryEnd = documentText.length() + text.length();

        setTextClassificationTarget(aJCas, entryStart, entryEnd);
        setTextClassificationOutcome(aJCas, outcome, entryStart, entryEnd);

        documentText.append(text + " ");

        aJCas.setDocumentText(documentText.toString().trim());
    }

    protected String performAdditionalOutcomeOperation(String outcome)
    {
        // opportunity to modify token information by overloading
        return outcome;
    }

    protected String performAdditionalTextOperation(String text)
    {
        // opportunity to modify token information by overloading
        return text;
    }

    protected void setTextClassificationTarget(JCas aJCas, int begin, int end)
    {
        if (!addBackendTcAnnotations) {
            return;
        }

        TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, begin, end);
        aTarget.addToIndexes();
    }

    protected void setTextClassificationOutcome(JCas aJCas, String outcome, int begin, int end)
        throws IOException
    {
        if (!addBackendTcAnnotations) {
            return;
        }

        TextClassificationOutcome tco = new TextClassificationOutcome(aJCas, begin, end);
        tco.setOutcome(outcome);
        tco.addToIndexes();
    }

    protected void initializeJCas(JCas aJCas)
    {
        DocumentMetaData data = new DocumentMetaData(aJCas);
        data.setDocumentId(runningId + "");
        data.setDocumentTitle("Title_" + runningId);
        data.addToIndexes();

        runningId++;
    }

    @Override
    public boolean hasNext()
    {

        if (reader == null) {
            Resource nextFile = getNextResource();
            if (nextFile == null) {
                return false;
            }
            initReader(nextFile);
        }

        do {
            nextDocument = read();
        }
        while (skipEmptyLines());

        if (nextDocument == null) {
            close();
            reader = null;
            return hasNext();
        }

        if (skipLine()) {
            return hasNext();
        }

        return true;
    }

    protected boolean skipEmptyLines()
    {
        return nextDocument != null && nextDocument.isEmpty();
    }

    protected boolean skipLine()
    {
        return skipLinePrefix != null
                && nextDocument.split(delimiter)[textIdx].startsWith(skipLinePrefix);
    }

    protected Resource getNextResource()
    {
        Resource next = null;
        try {
            next = nextFile();
        }
        catch (Exception e) {
            LogFactory.getLog(getClass()).debug("No more resources to be read");
        }
        return next;
    }

    protected String read()
    {
        try {
            return reader.readLine();
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    protected void initReader(Resource nextFile)
    {
        try {
            reader = new BufferedReader(new InputStreamReader(nextFile.getInputStream(), "utf-8"));
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public void close()
    {
        try {
            IOUtils.closeQuietly(reader);
            super.close();
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
