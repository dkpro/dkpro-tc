/**
 * Copyright 2018
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
package org.dkpro.tc.examples.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class Wassa2017Reader extends JCasResourceCollectionReader_ImplBase {

	String text = null;
	String ad_score = null;
	String id = null;
	private BufferedReader br;

	
	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {
		DocumentMetaData dmd = new DocumentMetaData(aJCas);
		dmd.setDocumentTitle("");
		dmd.setDocumentId(id);
		dmd.addToIndexes();

		aJCas.setDocumentText(text);

		TextClassificationOutcome o = new TextClassificationOutcome(aJCas, 0, aJCas.getDocumentText().length());
		o.setOutcome(ad_score);
		o.addToIndexes();
	}

	@Override
	public boolean hasNext() {
		try {
			if (br == null) {
				br = new BufferedReader(new InputStreamReader(nextFile().getInputStream(), "utf-8"));
			}

			String entry = br.readLine();

			if (entry == null || entry.isEmpty()) {
				br.close();
				
				try {
					Resource nf = nextFile();
					br = new BufferedReader(new InputStreamReader(nf.getInputStream(), "utf-8"));
					entry = br.readLine();
				} catch (NoSuchElementException e) {
					return false;
				}
				return true;
			}

			String[] split = entry.split("\t");
			
			id = split[0];
			text = split[1];
			ad_score = split[3];
			
			return true;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}
}
