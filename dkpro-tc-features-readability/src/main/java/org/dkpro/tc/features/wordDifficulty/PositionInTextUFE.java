/*******************************************************************************
 * Copyright 2016
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class PositionInTextUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     *         In this feature extractor we simply return the absolute position of the word by
     *         counting the tokens. The relative position is then calculated by dividing by the
     *         number of words). Alternative solutions could also take the sentences into account.
     */
    public static final String ABS_POSITION = "PositionOfWord";
    public static final String REL_POSITION = "RelativePositionOfWord";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        Set<Feature> featSet = new HashSet<Feature>();

        Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
        int position = 0;
        for (Token tok : tokens) {
            position++;
            if (tok.getBegin() == classificationUnit.getBegin()) {
                break;
            }
        }
        double relPosition = position / (double) Math.max(tokens.size(), 1);
        featSet.add(new Feature(ABS_POSITION, position));
        featSet.add(new Feature(REL_POSITION, relPosition));
        return featSet;
    }
}