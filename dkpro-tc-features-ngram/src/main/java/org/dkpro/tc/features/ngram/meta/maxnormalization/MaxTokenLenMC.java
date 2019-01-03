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
package org.dkpro.tc.features.ngram.meta.maxnormalization;

import java.util.Collection;
import java.util.Random;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.features.ngram.meta.LuceneMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MaxTokenLenMC
    extends LuceneMC
{

    public static final String LUCENE_FIELD = "maxTokenLen";
    Random r = new Random();

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {

        FrequencyDistribution<String> fd = new FrequencyDistribution<>();
        Collection<Token> select = JCasUtil.select(jcas, Token.class);
        for(Token t : select) {
            fd.addSample(t.getCoveredText().length()+ "_" + r.nextInt(), t.getCoveredText().length());
        }
        return fd;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_FIELD + featureExtractorName;
    }
}