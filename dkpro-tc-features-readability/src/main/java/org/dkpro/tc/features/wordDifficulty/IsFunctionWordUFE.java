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

package org.dkpro.tc.features.wordDifficulty;

import java.util.Set;


import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos" })
public class IsFunctionWordUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * @author beinborn
     * 
     *         In this feature, all articles, prepositions, conjunctions and pronouns are considered
     *         to be function words following this definition:
     *         http://dictionary.cambridge.org/dictionary/british/function-word
     * 
     *         Wider definitions often also count modal verbs, auxiliary verbs and quantifiers as
     *         function words. e.g. http://en.wikipedia.org/wiki/Function_word
     */
    public static final String IS_FUNC_WORD = "IsFunctionWord";
    

    @Override
    public Set<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        POS pos = JCasUtil.selectCovered(POS.class, classificationUnit).get(0);
       
        boolean isFunctionWord = (pos.getClass().getName().equals(ART.class.getName()) || pos.getClass().getName().equals(PP.class.getName()) || pos.getClass().getName().equals(PR.class.getName()) || pos.getClass().getName().equals(CONJ.class.getName()));

        return new Feature(IS_FUNC_WORD, isFunctionWord).asSet();
    }
}