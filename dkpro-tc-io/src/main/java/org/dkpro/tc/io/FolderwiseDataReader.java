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

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;

/**
 * This reader is suited when several text documents (without any labels) are
 * placed in a folder and the folder name is a suited label. 
 * 
 * The text of a file in a folder is read as self-contained document into a JCas and the entire
 * text-span is {@link org.dkpro.tc.api.type.TextClassificationTarget}. The folder name is
 * set as {@link org.dkpro.tc.api.type.TextClassificationOutcome}.
 */
public class FolderwiseDataReader extends JCasResourceCollectionReader_ImplBase {

	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {

		Resource currentFile = nextFile();

		initCas(aJCas, currentFile);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentFile.getInputStream(), "utf-8"))) {

			StringBuilder buffer = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + System.lineSeparator());
			}

			String text = buffer.toString().trim();
			
			setTextClassificationTarget(aJCas, currentFile, 0, text.length());
			setTextClassificationOutcome(aJCas, currentFile, 0, text.length());

			aJCas.setDocumentText(text.trim());
		}
	}

	protected void setTextClassificationTarget(JCas aJCas, Resource currentFile, int begin, int end) {
		TextClassificationTarget target = new TextClassificationTarget(aJCas, begin, end);
		target.addToIndexes();
	}

	protected void setTextClassificationOutcome(JCas aJCas, Resource currentFile, int begin, int end)
			throws IOException {
		TextClassificationOutcome tco = new TextClassificationOutcome(aJCas, begin, end);
		tco.setOutcome(currentFile.getResource().getFile().getParentFile().getName());
		tco.addToIndexes();
	}
}
