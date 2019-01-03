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
package org.dkpro.tc.features.pair.core.ngram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.base.NGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneKeywordPMetaCollector;
import org.dkpro.tc.features.pair.core.ngram.meta.LucenePFEBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Pair keyword ngram feature extractor for {@link org.dkpro.tc.features.ngram.KeywordNGram
 * KeywordNGramDFE} Can be used to extract ngrams from one or both documents in the pair, and
 * parameters for each document (view 1's, view 2's) can be set separately, or both documents can be
 * treated together as one extended document. <br>
 * Note that ngram features created by this class are each from a single document, i.e., not
 * combinations of ngrams from the pair of documents.
 */
public class LuceneKeywordPFE
    extends LucenePFEBase
    implements PairFeatureExtractor
{

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    public static final String KEYWORD_NGRAM_FIELD = "keywordngram";

    public static final String PARAM_KEYWORD_NGRAM_MIN_N = "keywordNgramMinN";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    protected int keywordMinN;

    public static final String PARAM_KEYWORD_NGRAM_MAX_N = "keywordNgramMaxN";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int keywordMaxN;

    public static final String PARAM_NGRAM_KEYWORDS_FILE = "keywordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
    protected String keywordsFile;

    public static final String PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY = "markSentenceBoundary";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
    protected boolean markSentenceBoundary;

    public static final String PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION = "markSentenceLocation";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
    protected boolean markSentenceLocation;

    public static final String PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS = "includeCommas";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
    protected boolean includeCommas;

    public static final String PARAM_KEYWORD_NGRAM_USE_TOP_K = "keywordNgramUseTopK";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int keywordNgramUseTopK;

    protected Set<String> keywords;
    /**
     * Minimum size n of ngrams from View 1's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MIN_N_VIEW1 = "keywordNgramMinNView1";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    protected int ngramMinN1;
    /**
     * Minimum size n of ngrams from View 2's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MIN_N_VIEW2 = "keywordNgramMinNView2";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    protected int ngramMinN2;
    /**
     * Maximum size n of ngrams from View 1's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MAX_N_VIEW1 = "keywordNgramMaxNView1";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    protected int ngramMaxN1;
    /**
     * Maximum size n of ngrams from View 2's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MAX_N_VIEW2 = "keywordNgramMaxNView2";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    protected int ngramMaxN2;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature. E.g.
     * Feature: view1NG_Dear
     */
    public static final String PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES = "useView1KeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView1NgramsAsFeatures;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature. E.g.
     * Feature: view2NG_Dear
     */
    public static final String PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES = "useView2KeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView2NgramsAsFeatures;
    /**
     * All qualifying ngrams from anywhere in either document are used as features. Feature does not
     * specify which view the ngram came from. E.g. Feature: allNG_Dear
     */
    public static final String PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES = "useViewBlindKeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useViewBlindNgramsAsFeatures;

    /**
     * This option collects a FrequencyDistribution of ngrams across both documents of all pairs,
     * but when writing features, the view where a particular ngram is found is recorded with the
     * ngram. For example, using a {@code PARAM_NGRAM_USE_TOP_K} value of 500, 400 of the ngrams in
     * the top 500 might happen to be from View 2's; and whenever an ngram from the 500 is seen in
     * any document, view 1 or 2, the document's view is recorded.<br>
     * E.g., Feature: view2allNG_Dear<br>
     * In order to use this option,
     * {@link LuceneKeywordPFE#PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES
     * PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES} must also be set to true.
     */
    public static final String PARAM_MARK_VIEWBLIND_KEYWORD_NGRAMS_WITH_LOCAL_VIEW = "markViewBlindKeywordNgramsWithLocalView";
    @ConfigurationParameter(name = PARAM_MARK_VIEWBLIND_KEYWORD_NGRAMS_WITH_LOCAL_VIEW, mandatory = false, defaultValue = "false")
    protected boolean markViewBlindNgramsWithLocalView;

    // These are only public so the MetaCollector can see them
    public static final String KEYWORD_NGRAM_FIELD1 = "keywordngram1";
    public static final String KEYWORD_NGRAM_FIELD2 = "keywordngram2";

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(
                new MetaCollectorConfiguration(LuceneKeywordPMetaCollector.class, parameterSettings)
                        .addStorageMapping(LuceneKeywordPMetaCollector.PARAM_TARGET_LOCATION,
                                LuceneKeywordPFE.PARAM_SOURCE_LOCATION,
                                LuceneKeywordPMetaCollector.LUCENE_DIR));
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        // FIXME This is a hack workaround because ngramUseTopK won't set until after
        // super.initialize,
        // but super.initialize tries to call getTopNgrams() anyways, which needs ngramUseTopK.
        fieldOfTheMoment = "";
        topNOfTheMoment = 1;
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        fieldOfTheMoment = KEYWORD_NGRAM_FIELD;
        topNOfTheMoment = ngramUseTopK;
        topKSet = getTopNgrams();

        fieldOfTheMoment = KEYWORD_NGRAM_FIELD1;
        topNOfTheMoment = ngramUseTopK1;
        topKSetView1 = getTopNgrams();

        fieldOfTheMoment = KEYWORD_NGRAM_FIELD2;
        topNOfTheMoment = ngramUseTopK2;
        topKSetView2 = getTopNgrams();

        try {
            keywords = FeatureUtil.getStopwords(keywordsFile, true);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        return true;
    }

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {
        TextClassificationTarget aTarget1 = JCasUtil.selectSingle(view1,
                TextClassificationTarget.class);
        TextClassificationTarget aTarget2 = JCasUtil.selectSingle(view2,
                TextClassificationTarget.class);
        FrequencyDistribution<String> view1Ngrams = KeywordNGramUtils.getDocumentKeywordNgrams(
                view1, aTarget1, ngramMinN1, ngramMaxN1, markSentenceBoundary, markSentenceLocation,
                includeCommas, keywords);
        FrequencyDistribution<String> view2Ngrams = KeywordNGramUtils.getDocumentKeywordNgrams(
                view2, aTarget2, ngramMinN2, ngramMaxN2, markSentenceBoundary, markSentenceLocation,
                includeCommas, keywords);
        FrequencyDistribution<String> allNgrams = getViewNgrams(view1, view2);

        Set<Feature> features = new HashSet<Feature>();
        if (useView1NgramsAsFeatures) {
            prefix = "keyNG1";
            features = addToFeatureArray(view1Ngrams, topKSetView1, features);
        }
        if (useView2NgramsAsFeatures) {
            prefix = "keyNG2";
            features = addToFeatureArray(view2Ngrams, topKSetView2, features);
        }
        if (useViewBlindNgramsAsFeatures && !markViewBlindNgramsWithLocalView) {
            prefix = "keyNG";
            features = addToFeatureArray(allNgrams, topKSet, features);
        }
        if (useViewBlindNgramsAsFeatures && markViewBlindNgramsWithLocalView) {
            prefix = "keyNGall1";
            features = addToFeatureArray(view1Ngrams, topKSet, features);
            prefix = "keyNGall2";
            features = addToFeatureArray(view2Ngrams, topKSet, features);
        }

        return features;
    }

    protected FrequencyDistribution<String> getViewNgrams(JCas view1, JCas view2)
    {
        List<JCas> jcases = new ArrayList<JCas>();
        jcases.add(view1);
        jcases.add(view2);
        return KeywordNGramUtils.getMultipleViewKeywordNgrams(jcases, ngramMinN, ngramMaxN,
                markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }

    @Override
    protected String getFieldName()
    {
        return fieldOfTheMoment;
    }

    @Override
    protected int getTopN()
    {
        return topNOfTheMoment;
    }

    // FIXME This class must be instantiated in NGramFeatureExtractorBase, but can't be anything
    // useful then.
    @Override
    protected String getFeaturePrefix()
    {
        return "ThisIsAnError";
    }
}
