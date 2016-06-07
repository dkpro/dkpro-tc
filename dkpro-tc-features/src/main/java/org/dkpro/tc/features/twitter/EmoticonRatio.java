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
package org.dkpro.tc.features.twitter;

import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet.EMO;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * A feature extracting the ratio of emoticons to tokens in tweets.
 * 
 * This example is taken from the paper:
 * 
 * <pre>
 * Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014).
 * DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data.
 * In: Proceedings of the 52nd Annual Meeting of the ACL.
 * </pre>
 * 
 */
public class EmoticonRatio
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    @Override
    public Set<Feature> extract(JCas jCas, TextClassificationTarget target)
        throws TextClassificationException
    {
        int nrOfEmoticons = JCasUtil.selectCovered(jCas, EMO.class, target).size();
        int nrOfTokens = JCasUtil.selectCovered(jCas, Token.class, target).size();
        double ratio = (double) nrOfEmoticons / nrOfTokens;
        return new Feature(EmoticonRatio.class.getSimpleName(), ratio).asSet();
    }
}
