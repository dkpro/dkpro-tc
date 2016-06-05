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
package org.dkpro.tc.features.readability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;

public class TraditionalReadabilityMeasuresFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * Computes the readability measures ari, coleman_liau, flesch, fog, kincaid, lix and smog as
     * implemented in de.tudarmstadt.ukp.dkpro.core.readability-asl s
     */
    // having all these parameters is not nice
    // better: several instances of extractor with measure as resource
    public static final String PARAM_ADD_KINCAID = "kincaid";
    @ConfigurationParameter(name = PARAM_ADD_KINCAID, mandatory = false, defaultValue = "true")
    protected boolean kincaid;

    public static final String PARAM_ADD_ARI = "ari";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = false)
    protected boolean ari;

    public static final String PARAM_ADD_COLEMANLIAU = "coleman_liau";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = true, defaultValue = "true")
    protected boolean colemanLiau;

    public static final String PARAM_ADD_FLESH = "flesch";
    @ConfigurationParameter(name = PARAM_ADD_FLESH, mandatory = false)
    protected boolean flesh;

    public static final String PARAM_ADD_FOG = "fog";
    @ConfigurationParameter(name = PARAM_ADD_FOG, mandatory = false)
    protected boolean fog;

    public static final String PARAM_ADD_LIX = "lix";
    @ConfigurationParameter(name = PARAM_ADD_LIX, mandatory = false)
    protected boolean lix;

    public static final String PARAM_ADD_SMOG = "smog";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = false)
    protected boolean smog;

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationUnit target)
        throws TextClassificationException
    {

        ReadabilityMeasures readability = new ReadabilityMeasures();
        Set<Feature> featSet = new HashSet<Feature>();
        if (jcas.getDocumentLanguage() != null) {
            readability.setLanguage(jcas.getDocumentLanguage());
        }

        int nrOfSentences = JCasUtil.selectCovered(jcas, Sentence.class, target).size();
        List<String> words = new ArrayList<String>();
        for (Token t : JCasUtil.selectCovered(jcas, Token.class, target)) {
            words.add(t.getCoveredText());
        }

        // only add features for selected readability measures
        Measures measure;
        if (ari) {
            measure = Measures.valueOf(PARAM_ADD_ARI);
            featSet.add(new Feature(PARAM_ADD_ARI, readability.getReadabilityScore(measure, words,
                    nrOfSentences)));
        }
        if (kincaid) {
            measure = Measures.valueOf(PARAM_ADD_KINCAID);
            featSet.add(new Feature(PARAM_ADD_KINCAID, readability.getReadabilityScore(measure,
                    words, nrOfSentences)));
        }
        if (colemanLiau) {
            measure = Measures.valueOf(PARAM_ADD_COLEMANLIAU);
            featSet.add(new Feature(PARAM_ADD_COLEMANLIAU, readability.getReadabilityScore(
                    measure, words, nrOfSentences)));
        }
        if (flesh) {
            measure = Measures.valueOf(PARAM_ADD_FLESH);
            featSet.add(new Feature(PARAM_ADD_FLESH, readability.getReadabilityScore(measure,
                    words, nrOfSentences)));
        }
        if (fog) {
            measure = Measures.valueOf(PARAM_ADD_FOG);
            featSet.add(new Feature(PARAM_ADD_FOG, readability.getReadabilityScore(measure, words,
                    nrOfSentences)));
        }
        if (smog) {
            measure = Measures.valueOf(PARAM_ADD_SMOG);
            featSet.add(new Feature(PARAM_ADD_SMOG, readability.getReadabilityScore(measure,
                    words, nrOfSentences)));
        }
        if (lix) {
            measure = Measures.valueOf(PARAM_ADD_LIX);
            featSet.add(new Feature(PARAM_ADD_LIX, readability.getReadabilityScore(measure, words,
                    nrOfSentences)));
        }
        return featSet;
    }
}