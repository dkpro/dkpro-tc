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
package org.dkpro.tc.features.entityrecognition;

import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Extracts the ratio of named entities per sentence
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NamedEntityPerSentenceRatio
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget aTarget)
        throws TextClassificationException
    {

        Set<Feature> featList = new TreeSet<Feature>();

        int numOrgaNE = JCasUtil.selectCovered(view, Organization.class, aTarget).size();
        int numPersonNE = JCasUtil.selectCovered(view, Person.class, aTarget).size();
        int numLocNE = JCasUtil.selectCovered(view, Location.class, aTarget).size();
        int numSentences = JCasUtil.selectCovered(view, Sentence.class, aTarget).size();

        if (numSentences > 0) {
            featList.add(new Feature("NrOfOrganizationEntities", numOrgaNE, FeatureType.NUMERIC));
            featList.add(new Feature("NrOfPersonEntities", numPersonNE, FeatureType.NUMERIC));
            featList.add(new Feature("NrOfLocationEntities", numLocNE, FeatureType.NUMERIC));

            featList.add(new Feature("NrOfOrganizationEntitiesPerSent",
                    Math.round(((float) numOrgaNE / numSentences) * 100f) / 100f,
                    FeatureType.NUMERIC));
            featList.add(new Feature("NrOfPersonEntitiesPerSent",
                    Math.round(((float) numPersonNE / numSentences) * 100f) / 100f,
                    FeatureType.NUMERIC));
            featList.add(new Feature("NrOfLocationEntitiesPerSent",
                    Math.round(((float) numLocNE / numSentences) * 100f) / 100f,
                    FeatureType.NUMERIC));
        }

        return featList;
    }

}
