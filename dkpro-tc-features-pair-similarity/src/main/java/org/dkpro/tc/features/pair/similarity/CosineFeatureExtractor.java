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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.dkpro.similarity.algorithms.lexical.string.CosineSimilarity;

/**
 * Extracts the document pair similarity using
 * {@link org.dkpro.similarity.algorithms.lexical.string.CosineSimilarity CosineSimilarity} (tokens)
 * measure. Please be aware this Cosine Similarity API has a history of bugginess.
 */
public class CosineFeatureExtractor<T extends Annotation>
    extends LuceneFeatureExtractorBase // FeatureExtractorResource_ImplBase ->
                                       // NGramFeatureExtractorBase -> LuceneFeatureExtractorBase
    implements PairFeatureExtractor
{
    /**
     * Minimum size n of ngrams from View 1's.
     */
    public static final String PARAM_WEIGHTING_MODE_TF = "weightingModeTf";
    @ConfigurationParameter(name = PARAM_WEIGHTING_MODE_TF, mandatory = false)
    private CosineSimilarity.WeightingModeTf weightingModeTf;

    public static final String PARAM_WEIGHTING_MODE_IDF = "weightingModeIdf";
    @ConfigurationParameter(name = PARAM_WEIGHTING_MODE_IDF, mandatory = false)
    private CosineSimilarity.WeightingModeIdf weightingModeIdf;

    /**
     * L1 is Manhattan norm; L2 is euclidean norm.
     * 
     * @see <a href=
     *      "http://en.wikipedia.org/wiki/Norm_(mathematics)#Taxicab_norm_or_Manhattan_norm">Manhattan
     *      norm (Wikipedia)</a>
     * @see <a href="http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">Euclidean Norm
     *      (Wikipedia)</a>
     */
    public static final String PARAM_NORMALIZATION_MODE = "normalizationMode";
    @ConfigurationParameter(name = PARAM_NORMALIZATION_MODE, mandatory = false)
    private CosineSimilarity.NormalizationMode normalizationMode;

    /**
     * This is the annotation type of the ngrams: usually Token.class, but possibly Lemma.class or
     * Stem.class,etc.
     */
    public static final String PARAM_NGRAM_ANNO_TYPE = "ngramAnnotationType";
    @ConfigurationParameter(name = PARAM_NGRAM_ANNO_TYPE, mandatory = false, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private Class<T> ngramAnnotationType;

    private CosineSimilarity measure;

    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(
                new MetaCollectorConfiguration(IdfPairMetaCollector.class, parameterSettings)
                        .addStorageMapping(IdfPairMetaCollector.PARAM_TARGET_LOCATION,
                                CosineFeatureExtractor.PARAM_SOURCE_LOCATION,
                                IdfPairMetaCollector.LUCENE_DIR));
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        if (weightingModeTf == null) {
            weightingModeTf = CosineSimilarity.WeightingModeTf.FREQUENCY_LOGPLUSONE;
        }
        if (weightingModeIdf == null) {
            weightingModeIdf = CosineSimilarity.WeightingModeIdf.PASSTHROUGH;
        }
        if (normalizationMode == null) {
            normalizationMode = CosineSimilarity.NormalizationMode.L2;
        }

        measure = new CosineSimilarity(weightingModeTf, weightingModeIdf, normalizationMode,
                fdToMap(topKSet)); // DF counts
        return true;
    }

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {
        try {

            TextClassificationTarget aTarget1 = JCasUtil.selectSingle(view1,
                    TextClassificationTarget.class);
            TextClassificationTarget aTarget2 = JCasUtil.selectSingle(view2,
                    TextClassificationTarget.class);

            // Note: getSimilarity(String, String) is *not* a convenience
            // method for getSimilarity(Collection<String>, Collection<String>).
            Set<String> text1 = NGramUtils.getDocumentNgrams(view1, aTarget1, true, false, 1, 1,
                    stopwords, ngramAnnotationType).getKeys();
            Set<String> text2 = NGramUtils.getDocumentNgrams(view2, aTarget2, true, false, 1, 1,
                    stopwords, ngramAnnotationType).getKeys();

            double similarity = measure.getSimilarity(text1, text2);

            // Temporary fix for DKPro Similarity Issue 30
            if (Double.isNaN(similarity)) {
                similarity = 0.0;
            }

            return new Feature("Similarity" + measure.getName(), similarity, FeatureType.NUMERIC)
                    .asSet();
        }
        catch (SimilarityException e) {
            throw new TextClassificationException(e);
        }
    }

    @Override
    protected String getFieldName()
    {
        return LuceneFeatureExtractorBase.LUCENE_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return 50000; // we want them all
    }

    @Override
    protected String getFeaturePrefix()
    {
        return null; // CosSim is a numeric feature, no prefix
    }

    private static Map<String, Double> fdToMap(FrequencyDistribution<String> fD)
    {
        Map<String, Double> map = new HashMap<String, Double>();
        for (String token : fD.getKeys()) {
            map.put(token, new Double(1.0 / fD.getCount(token)));
            // map.put(token, new Double(fD.getCount(token)));
        }
        return map;

    }
}