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
package org.dkpro.tc.features.pair.core.ngram.meta;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.ngram.meta.LuceneMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public abstract class LucenePMetaCollectorBase
    extends LuceneMC
    implements Constants
{
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {
        JCas view1;
        JCas view2;
        try {
            view1 = jcas.getView(PART_ONE);
            view2 = jcas.getView(PART_TWO);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        List<JCas> jcases = new ArrayList<JCas>();
        jcases.add(view1);
        jcases.add(view2);

        FrequencyDistribution<String> view1NGrams;
        FrequencyDistribution<String> view2NGrams;
        FrequencyDistribution<String> documentNGrams;
        try {
            TextClassificationTarget aTarget1 = JCasUtil.selectSingle(view1,
                    TextClassificationTarget.class);
            TextClassificationTarget aTarget2 = JCasUtil.selectSingle(view2,
                    TextClassificationTarget.class);
            view1NGrams = getNgramsFDView1(view1, aTarget1);
            view2NGrams = getNgramsFDView2(view2, aTarget2);
            documentNGrams = getNgramsFD(jcases);
        }
        catch (TextClassificationException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (String ngram : documentNGrams.getKeys()) {
            for (int i = 0; i < documentNGrams.getCount(ngram); i++) {
                addField(getFieldName(), ngram);
            }
        }
        for (String ngram : view1NGrams.getKeys()) {
            for (int i = 0; i < view1NGrams.getCount(ngram); i++) {
                addField(getFieldNameView1(), ngram);
            }
        }
        for (String ngram : view2NGrams.getKeys()) {
            for (int i = 0; i < view2NGrams.getCount(ngram); i++) {
                addField(getFieldNameView2(), ngram);
            }
        }

    }

    protected void addField(String fieldName, String ngram)
    {
        Field field = new Field(fieldName, ngram, fieldType);
        currentDocument.add(field);
    }

    protected abstract FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
        throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView1(JCas view1,
            TextClassificationTarget aTarget)
        throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView2(JCas view2,
            TextClassificationTarget aTarget)
        throws TextClassificationException;

    protected abstract String getFieldNameView1();

    protected abstract String getFieldNameView2();

}
