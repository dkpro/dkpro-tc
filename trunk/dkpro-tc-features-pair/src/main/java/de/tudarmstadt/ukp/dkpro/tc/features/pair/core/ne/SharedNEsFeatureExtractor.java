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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor Returns if two views share the same named entities.
 * 
 * @author nico.erbs@gmail.com
 * 
 */
public class SharedNEsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(new Feature("SharedNEs", !Collections.disjoint(getNEs(view1),
                getNEs(view2))));

    }

    /**
     * 
     * @param view
     *            the view to be processed
     * @return all named entities in this view
     */
    private Collection<String> getNEs(JCas view)
    {
        List<String> entities = new ArrayList<String>();
        for (NamedEntity entity : JCasUtil.select(view, NamedEntity.class)) {
            entities.add(entity.getCoveredText());
        }
        return entities;
    }
}
