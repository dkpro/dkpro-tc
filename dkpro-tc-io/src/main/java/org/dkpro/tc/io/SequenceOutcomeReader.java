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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This reader reads a common data format for sequence classification tasks, for
 * instance Part-of-Speech tagging, where each token is associated with an own
 * outcome e.g.:
 * 
 * <pre>
 * 		The    TAB DET
 * 		car    TAB NOUN
 * 		drives TAB VERB
 * 		<NEWLINE>
 * 		The		TAB DET
 * 		sun		TAB NOUN
 * 		shines	TAB VERB
 * </pre>
 * 
 * Each token is annotated as
 * {@link org.dkpro.tc.api.type.TextClassificationTarget} and the outcome is
 * annotated as {@link org.dkpro.tc.api.type.TextClassificationOutcome}. An
 * empty lines separates consecutive sequences which are annotated as
 * {@link org.dkpro.tc.api.type.TextClassificationSequence}. Each sequence is
 * read into an own JCas.
 */
public class SequenceOutcomeReader extends JCasResourceCollectionReader_ImplBase {

	/**
	 * The separating character that separates textual information from its
	 * outcome, i.e. a label or a numerical value. Defaults to TAB
	 */
	public static final String PARAM_SEPARATING_CHAR = "PARAM_SEPARATING_CHAR";
	@ConfigurationParameter(name = PARAM_SEPARATING_CHAR, mandatory = true, defaultValue = "\t")
	private String separatingChar;

	public static final String PARAM_SKIP_LINES_START_WITH_STRING = "PARAM_SKIP_LINES_START_WITH_STRING";
	@ConfigurationParameter(name = PARAM_SKIP_LINES_START_WITH_STRING, mandatory = false)
	private String skipLinePrefix;

	private BufferedReader reader;

	private List<String> nextSequence = null;

	private int runningId = 0;

	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {

		initializeJCas(aJCas);

		StringBuilder documentText = new StringBuilder();

		int seqStart = documentText.length();
		for(int i=0; i < nextSequence.size(); i++){
			String e = nextSequence.get(i);
			String[] entry = e.split(separatingChar);
			
			String token = entry[0];
			String outcome = entry[1];
			
			int tokStart = documentText.length();
			int tokEnd = tokStart + token.length();
			
			setTextClassificationTarget(aJCas, tokStart, tokEnd);
			setTextClassificationOutcome(aJCas, outcome, tokStart, tokEnd);
			
			documentText.append(token);
			if (i + 1 < nextSequence.size()) {
				documentText.append(" ");
			}
		}
		
		setTextClassificationSequence(aJCas, seqStart, documentText.length());
		aJCas.setDocumentText(documentText.toString());
	}

	protected void setTextClassificationTarget(JCas aJCas, int begin, int end) {
		TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, begin, end);
		aTarget.addToIndexes();
	}

	protected void setTextClassificationOutcome(JCas aJCas, String outcome, int begin, int end) throws IOException {
		TextClassificationOutcome tco = new TextClassificationOutcome(aJCas, begin, end);
		tco.setOutcome(outcome);
		tco.addToIndexes();
	}
	
	protected void setTextClassificationSequence(JCas aJCas, int begin, int end) {
		TextClassificationSequence aSequence = new TextClassificationSequence(aJCas, begin, end);
		aSequence.addToIndexes();
	}
	

	protected void initializeJCas(JCas aJCas) {
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

		nextSequence = read();

		if (nextSequence.isEmpty()) {
			close();
			reader = null;
			return hasNext();
		}

		return true;
	}

	protected Resource getNextResource() {
		Resource next = null;
		try {
			next = nextFile();
		} catch (Exception e) {
			// catch silently
		}
		return next;
	}

	protected List<String> read() {
		
		List<String> buffer = new ArrayList<>();
		
		String line = null;
		try {
			while (((line = reader.readLine()) != null && !line.isEmpty())) {
				
				if(skipLinePrefix != null && !skipLinePrefix.isEmpty()){
					if(line.startsWith(skipLinePrefix)){
						continue;
					}
				}
				
				buffer.add(line);
			}
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
		
		return buffer;
	}

	protected void initReader(Resource nextFile) {
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
