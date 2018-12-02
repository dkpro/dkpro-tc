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
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This reader reads a common data format for sequence classification tasks, for instance
 * Part-of-Speech tagging, where each token is associated with an own outcome e.g.:
 * 
 * <pre>
 *      The TAB DET
 *      car TAB NOUN
 *      drives TAB VERB
 *       
 *      The TAB DET
 *      sun TAB NOUN
 *      shines  TAB VERB
 * </pre>
 * 
 * Each token is annotated as {@link org.dkpro.tc.api.type.TextClassificationTarget} and the outcome
 * is annotated as {@link org.dkpro.tc.api.type.TextClassificationOutcome}. An empty lines separates
 * consecutive sequences which are annotated as
 * {@link org.dkpro.tc.api.type.TextClassificationSequence}. The tokens are additionally annotated
 * as {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token} and the sequence as
 * {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence} Each sequence is read into
 * an own JCas.
 */
public class DelimiterSeparatedSequenceValuesReader
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
    public static final String PARAM_TOKEN_INDEX = "PARAM_TOKEN_INDEX";
    @ConfigurationParameter(name = PARAM_TOKEN_INDEX, mandatory = true, defaultValue = "0")
    protected Integer tokenIdx;

    /**
     * Each line in the file is split by the delimiter character and the value at index one is
     * assumed to be the label/category of the token. This variable allows to change the default
     * value.
     */
    public static final String PARAM_OUTCOME_INDEX = "PARAM_OUTCOME_INDEX";
    @ConfigurationParameter(name = PARAM_OUTCOME_INDEX, mandatory = true, defaultValue = "1")
    protected Integer outcomeIdx;

    /**
     * Allows the alter the number of sequences that are read into a CAS. Many CAS objects with only
     * a few sequences in it might create additional overhead for CAS creation, which will slow down
     * the processing time. Likewise the creation of a single fat-CAS might also be expensive. This
     * variable allows to control the number of sequences in a CAS.
     */
    public static final String PARAM_SEQUENCES_PER_CAS = "PARAM_SEQUENCES_PER_CAS";
    @ConfigurationParameter(name = PARAM_SEQUENCES_PER_CAS, mandatory = true, defaultValue = "100")
    protected Integer sequencesPerCas;

    /**
	 * The reader annotates by default the token as {@link TextClassificationTarget}
	 * and the read category label as {@link TextClassificationOutcome} and the
	 * sequences as {@link TextClassificationSequence}. This switch turns off the
	 * automatic annotation.
	 */
    public static final String PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS = "PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS";
    @ConfigurationParameter(name = PARAM_ANNOTATE_TC_BACKEND_ANNOTATIONS, mandatory = true, defaultValue = "true")
    protected boolean addBackendTcAnnotations;

    protected BufferedReader reader;

    protected List<List<String>> sequenceBuffer = new ArrayList<>();
    protected List<String> nextSequence = null;
    protected String line = null;

    protected int runningId = 0;

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {

        initializeJCas(aJCas);

        StringBuilder documentText = new StringBuilder();

        for (List<String> sequence : sequenceBuffer) {
            int seqStart = documentText.length();
            for (int i = 0; i < sequence.size(); i++) {
                String e = sequence.get(i);
                String[] entry = e.split(delimiter);
                
                checkSplit(entry);

                String token = entry[tokenIdx];
                String outcome = entry[outcomeIdx];

                token = performAdditionalTokenOperation(token);
                outcome = performAdditionalOutcomeOperation(outcome);

                int tokStart = documentText.length();
                int tokEnd = tokStart + token.length();

                setToken(aJCas, tokStart, tokEnd);
                setTextClassificationTarget(aJCas, token, tokStart, tokEnd);
                setTextClassificationOutcome(aJCas, outcome, tokStart, tokEnd);

                documentText.append(token);
                if (i + 1 < sequence.size()) {
                    documentText.append(" ");
                }
            }

            setTextClassificationSequence(aJCas, seqStart, documentText.length());
            setSentence(aJCas, seqStart, documentText.length());
            documentText.append(" ");
        }

        aJCas.setDocumentText(documentText.toString());
        sequenceBuffer = new ArrayList<>();
    }

	protected void checkSplit(String[] entry) {
		if (entry.length < 2) {
			throw new IllegalStateException("Expected two values but got [" + entry.length
					+ "] after splitting with delimiter [" + delimiter + "]");
		}
	}

	protected String performAdditionalOutcomeOperation(String outcome)
    {
        // opportunity to modify token information by overloading
        return outcome;
    }

    protected String performAdditionalTokenOperation(String token)
    {
        // opportunity to modify token information by overloading
        return token;
    }

    protected void setToken(JCas aJCas, int begin, int end)
    {
        Token token = new Token(aJCas, begin, end);
        token.addToIndexes();
    }

    protected void setSentence(JCas aJCas, int begin, int end)
    {
        Sentence sentence = new Sentence(aJCas, begin, end);
        sentence.addToIndexes();
    }

    protected void setTextClassificationTarget(JCas aJCas, String token, int begin, int end)
    {
        if (!addBackendTcAnnotations) {
            return;
        }

        TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, begin, end);
        aTarget.setSuffix(token); // This improves readability of the id2outcome report
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

    protected void setTextClassificationSequence(JCas aJCas, int begin, int end)
    {
        if (!addBackendTcAnnotations) {
            return;
        }

        TextClassificationSequence aSequence = new TextClassificationSequence(aJCas, begin, end);
        aSequence.addToIndexes();
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
            if (!sequenceBuffer.isEmpty()) {
                return true; // data left to process
            }
            Resource nextFile = getNextResource();
            if (nextFile == null) {
                return false;
            }
            initReader(nextFile);
        }

        int sequencesRead = 0;

        while (sequencesRead < sequencesPerCas) {
            nextSequence = read();
            if (!nextSequence.isEmpty()) {
                sequenceBuffer.add(nextSequence);
                sequencesRead++;
            }
            if (nextSequence.isEmpty()) {
                close();
                reader = null;
                return hasNext();
            }
        }

        if (sequenceBuffer.isEmpty()) {
            close();
            reader = null;
            return hasNext();
        }

        return true;
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

    protected List<String> read()
    {

        List<String> buffer = new ArrayList<>();
        try {
            while (sequenceIncomplete()) {
                if (skipElement()) {
                    continue;
                }

                buffer.add(line);
            }
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        return buffer;
    }

    protected boolean skipElement()
    {
        return skipLinePrefix != null && line.startsWith(skipLinePrefix);
    }

    protected boolean sequenceIncomplete() throws IOException
    {
        return (line = reader.readLine()) != null && !line.isEmpty();
    }

    protected void initReader(Resource nextFile)
    {
        try {
            reader = new BufferedReader(new InputStreamReader(nextFile.getInputStream(), UTF_8));
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("deprecation")
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
