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
package org.dkpro.tc.features.pair.similarity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.ngram.meta.LuceneMC;
import org.dkpro.tc.features.ngram.meta.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

public class IdfPairMetaCollector<T extends Annotation>
    extends LuceneMC
    implements Constants
{
    /**
     * This is the annotation type of the ngrams: usually Token.class, but possibly Lemma.class or
     * Stem.class,etc.
     */
    @ConfigurationParameter(name = CosineFeatureExtractor.PARAM_NGRAM_ANNO_TYPE, mandatory = false, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private Class<T> ngramAnnotationType;

    private Set<String> stopwords;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
        stopwords = new HashSet<String>();
    }

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

        FrequencyDistribution<String> document1NGrams;
        FrequencyDistribution<String> document2NGrams;
        try {
            document1NGrams = getNgramsFD(view1);
            document2NGrams = getNgramsFD(view2);
        }
        catch (TextClassificationException e) {
            throw new AnalysisEngineProcessException(e);
        }

        FrequencyDistribution<String> documentNGrams = new FrequencyDistribution<String>();
        // Only add a term once per document, no matter how many times it occurs in the doc.
        // "Document Frequency".
        // This is different than other metacollectors.
        for (String key : document1NGrams.getKeys()) {
            documentNGrams.addSample(key, 1);
        }
        for (String key : document2NGrams.getKeys()) {
            documentNGrams.addSample(key, 1);
        }

        for (String ngram : documentNGrams.getKeys()) {
            for (int i = 0; i < documentNGrams.getCount(ngram); i++) {
                Field field = new Field(getFieldName(), ngram, fieldType);
                currentDocument.add(field);
            }
        }

        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        TextClassificationTarget aTarget = JCasUtil.selectSingle(jcas,
                TextClassificationTarget.class);

        FrequencyDistribution<String> toReturn = NGramUtils.getDocumentNgrams(jcas, aTarget, true,
                false, 1, 1, stopwords, ngramAnnotationType);
        return toReturn;
    }

    @Override
    protected String getFieldName()
    {
        return LuceneFeatureExtractorBase.LUCENE_NGRAM_FIELD;
    }
}