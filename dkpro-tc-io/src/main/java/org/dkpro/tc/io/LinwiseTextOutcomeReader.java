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
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This is a line-wise reader which reads textual information and an associated
 * outcome from a single line of text that is separated by a character, which
 * allows separating both information. The text is annotated as
 * {@link org.dkpro.tc.api.type.TextClassificationTarget} and the outcome is
 * annotated as {@link org.dkpro.tc.api.type.TextClassificationOutcome}, which
 * are the data types DKPro TC requires for processing the read data.
 */
public class LinwiseTextOutcomeReader extends JCasResourceCollectionReader_ImplBase {

	/**
	 * The separating character that separates textual information from its
	 * outcome, i.e. a label or a numerical value. Defaults to TAB
	 */
	public static final String PARAM_SEPARATING_CHAR = "PARAM_SEPARATING_CHAR";
	@ConfigurationParameter(name = PARAM_SEPARATING_CHAR, mandatory = true, defaultValue = "\t")
	private String separatingChar;

	public static final String PARAM_TEXT_INDEX = "PARAM_TEXT_INDEX";
	@ConfigurationParameter(name = PARAM_TEXT_INDEX, mandatory = true, defaultValue = "0")
	private Integer textIdx;

	public static final String PARAM_OUTCOME_INDEX = "PARAM_OUTCOME_INDEX";
	@ConfigurationParameter(name = PARAM_OUTCOME_INDEX, mandatory = true, defaultValue = "1")
	private Integer outcomeIdx;
	
	public static final String PARAM_SKIP_LINES_START_WITH_STRING = "PARAM_SKIP_LINES_START_WITH_STRING";
	@ConfigurationParameter(name = PARAM_SKIP_LINES_START_WITH_STRING, mandatory = false)
	private String skipLinePrefix;

	private BufferedReader reader;

	private String nextDocument = null;

	private int runningId = 0;

	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {

		initializeJCas(aJCas);

		StringBuilder documentText = new StringBuilder();

		String[] split = nextDocument.split(separatingChar);

		String text = split[textIdx].trim();
		String outcome = split[outcomeIdx].trim();

		int entryStart = documentText.length();
		int entryEnd = documentText.length() + text.length();

		TextClassificationTarget classificationTarget = new TextClassificationTarget(aJCas, entryStart, entryEnd);
		classificationTarget.addToIndexes();

		TextClassificationOutcome classificationOutcome = new TextClassificationOutcome(aJCas, entryStart, entryEnd);
		classificationOutcome.setOutcome(outcome);
		classificationOutcome.addToIndexes();

		documentText.append(text + " ");

		aJCas.setDocumentText(documentText.toString().trim());
	}

	private void initializeJCas(JCas aJCas) {
		DocumentMetaData data = new DocumentMetaData(aJCas);
		data.setDocumentId(runningId + "");
		data.addToIndexes();

		runningId++;
	}

	@Override
	public boolean hasNext() {

		if (reader == null) {
			Resource nextFile = getNextResource();
			if (nextFile == null) {
				return false;
			}
			initReader(nextFile);
		}

		do {
			nextDocument = read();
		} while (skipEmptyLines());
		

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

	private boolean skipEmptyLines() {
		return nextDocument != null && nextDocument.isEmpty();
	}

	private boolean skipLine() {
		return skipLinePrefix != null && 
				nextDocument.split(separatingChar)[textIdx].startsWith(skipLinePrefix);
	}

	private Resource getNextResource() {
		Resource next = null;
		try {
			next = nextFile();
		} catch (Exception e) {
			// catch silently
		}
		return next;
	}

	private String read() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	private void initReader(Resource nextFile) {
		try {
			reader = new BufferedReader(new InputStreamReader(nextFile.getInputStream(), "utf-8"));
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public void close() {
		try {
			IOUtils.closeQuietly(reader);
			super.close();
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
