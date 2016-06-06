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
package org.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;

public abstract class LucenePMetaCollectorBase
    extends LuceneBasedMetaCollector
    implements Constants
{
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
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

        initializeDocument(jcas);

        List<JCas> jcases = new ArrayList<JCas>();
        jcases.add(view1);
        jcases.add(view2);

        FrequencyDistribution<String> view1NGrams;
        FrequencyDistribution<String> view2NGrams;
        FrequencyDistribution<String> documentNGrams;
        try {
            TextClassificationUnit target1 = JCasUtil.selectSingle(view1, TextClassificationUnit.class);
            TextClassificationUnit target2 = JCasUtil.selectSingle(view2, TextClassificationUnit.class);
            view1NGrams = getNgramsFDView1(view1,target1);
            view2NGrams = getNgramsFDView2(view2,target2);
            documentNGrams = getNgramsFD(jcases);
        }
        catch (TextClassificationException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (String ngram : documentNGrams.getKeys()) {
            for (int i = 0; i < documentNGrams.getCount(ngram); i++) {
                addField(jcas, getFieldName(), ngram);
            }
        }
        for (String ngram : view1NGrams.getKeys()) {
            for (int i = 0; i < view1NGrams.getCount(ngram); i++) {
                addField(jcas, getFieldNameView1(), ngram);
            }
        }
        for (String ngram : view2NGrams.getKeys()) {
            for (int i = 0; i < view2NGrams.getCount(ngram); i++) {
                addField(jcas, getFieldNameView2(), ngram);
            }
        }

        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    protected abstract FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
        throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView1(JCas view1, TextClassificationUnit target)
        throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView2(JCas view2, TextClassificationUnit target)
        throws TextClassificationException;

    protected abstract String getFieldNameView1();

    protected abstract String getFieldNameView2();

}
