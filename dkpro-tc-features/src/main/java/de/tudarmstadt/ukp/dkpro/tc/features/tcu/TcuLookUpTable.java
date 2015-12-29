/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.features.tcu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Provides speedy access to the TextClassificationUnits (TCU) covered by a TextClassificationSequence.
 * Enables faster access to the previous/next TCU
 * The look-up tables provided here are build for each new sequence. 
 */
public class TcuLookUpTable 
	extends FeatureExtractorResource_ImplBase
	implements ClassificationUnitFeatureExtractor
{
	private   String lastSeenDocumentId = "";

	protected   HashMap<Integer, Boolean> idx2SequenceBegin = new HashMap<Integer, Boolean>();
	protected   HashMap<Integer, Boolean> idx2SequenceEnd = new HashMap<Integer, Boolean>();

	protected   HashMap<Integer, TextClassificationUnit> begin2Unit = new HashMap<Integer, TextClassificationUnit>();
	protected   HashMap<Integer, Integer> unitBegin2Idx = new HashMap<Integer, Integer>();
	protected   HashMap<Integer, Integer> unitEnd2Idx = new HashMap<Integer, Integer>();
	protected   List<String> units = new ArrayList<String>();

	public Set<Feature> extract(JCas aView,
			TextClassificationUnit aClassificationUnit)
			throws TextClassificationException {
		if (isTheSameDocument(aView)) {
			return null;
		}
		begin2Unit = new HashMap<Integer, TextClassificationUnit>();
		unitBegin2Idx = new HashMap<Integer, Integer>();
		idx2SequenceBegin = new HashMap<Integer, Boolean>();
		idx2SequenceEnd = new HashMap<Integer, Boolean>();
		units = new ArrayList<String>();

		int i = 0;
		for (TextClassificationUnit t : JCasUtil.select(aView, TextClassificationUnit.class)) {
			Integer begin = t.getBegin();
			Integer end = t.getEnd();
			begin2Unit.put(begin, t);
			unitBegin2Idx.put(begin, i);
			unitEnd2Idx.put(end, i);
			units.add(t.getCoveredText());
			i++;
		}
		for (TextClassificationSequence sequence : JCasUtil.select(aView, TextClassificationSequence.class)) {
			Integer begin = sequence.getBegin();
			Integer end = sequence.getEnd();
			Integer idxStartUnit = unitBegin2Idx.get(begin);
			Integer idxEndUnit = unitEnd2Idx.get(end);
			idx2SequenceBegin.put(idxStartUnit, true);
			idx2SequenceEnd.put(idxEndUnit, true);
		}
		return null;
	}

	private boolean isTheSameDocument(JCas aView) {
		DocumentMetaData meta = JCasUtil.selectSingle(aView,
				DocumentMetaData.class);
		String currentId = meta.getDocumentId();
		boolean isSame = currentId.equals(lastSeenDocumentId);
		lastSeenDocumentId = currentId;
		return isSame;
	}

}
