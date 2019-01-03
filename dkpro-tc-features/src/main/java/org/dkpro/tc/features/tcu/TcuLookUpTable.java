/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.tcu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Provides speedy access to the TextClassificationUnits (TCU) covered by a
 * TextClassificationSequence. Enables faster access to the previous/next TCU The look-up tables
 * provided here are build for each new sequence.
 */
public class TcuLookUpTable
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    private String lastSeenDocumentId = "";

    protected HashMap<Integer, Boolean> idx2SequenceBegin = new HashMap<Integer, Boolean>();
    protected HashMap<Integer, Boolean> idx2SequenceEnd = new HashMap<Integer, Boolean>();

    protected HashMap<Integer, TextClassificationTarget> begin2target = new HashMap<Integer, TextClassificationTarget>();
    protected HashMap<Integer, Integer> targetBegin2Idx = new HashMap<Integer, Integer>();
    protected HashMap<Integer, Integer> targetEnd2Idx = new HashMap<Integer, Integer>();
    protected List<TextClassificationTarget> units = new ArrayList<TextClassificationTarget>();

    public Set<Feature> extract(JCas aJCas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        if (isTheSameDocument(aJCas)) {
            return null;
        }
        begin2target = new HashMap<Integer, TextClassificationTarget>();
        targetBegin2Idx = new HashMap<Integer, Integer>();
        idx2SequenceBegin = new HashMap<Integer, Boolean>();
        idx2SequenceEnd = new HashMap<Integer, Boolean>();
        units = new ArrayList<TextClassificationTarget>();

        int i = 0;
        for (TextClassificationTarget t : JCasUtil.select(aJCas, TextClassificationTarget.class)) {
            Integer begin = t.getBegin();
            Integer end = t.getEnd();
            begin2target.put(begin, t);
            targetBegin2Idx.put(begin, i);
            targetEnd2Idx.put(end, i);
            units.add(t);
            i++;
        }
        for (TextClassificationSequence sequence : JCasUtil.select(aJCas,
                TextClassificationSequence.class)) {
            Integer begin = sequence.getBegin();
            Integer end = sequence.getEnd();
            Integer idxStartUnit = targetBegin2Idx.get(begin);
            Integer idxEndUnit = targetEnd2Idx.get(end);
            idx2SequenceBegin.put(idxStartUnit, true);
            idx2SequenceEnd.put(idxEndUnit, true);
        }
        return null;
    }

    private boolean isTheSameDocument(JCas aJCas)
    {
        JCasId casId = JCasUtil.selectSingle(aJCas, JCasId.class);
        String currentId = casId.getId() + "";
        boolean isSame = currentId.equals(lastSeenDocumentId);
        lastSeenDocumentId = currentId;
        return isSame;
    }

}
