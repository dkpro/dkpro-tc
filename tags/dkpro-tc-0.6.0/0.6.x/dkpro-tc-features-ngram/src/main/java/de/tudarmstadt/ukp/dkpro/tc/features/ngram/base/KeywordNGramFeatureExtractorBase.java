/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.KeywordNGramMetaCollector;

/**
 * This class extracts lists of specified keywords from a text. <br />
 * The lists are similar to ngrams, except that instead of using all tokens, only the specified
 * keywords are eligible to appear in a list. These "keyword ngrams" may be useful for tasks such
 * as sentence ordering (Barzilay and Lapata 2008). The concept is similar to strings of entity
 * mentions in Centering Theory, except since the user defines the permissible tokens, finite lists
 * are preferred. Keyword ngrams are extracted from an entire document, not just a single sentence.<br />
 * <br />
 * Example: keyword ngrams of discourse markers:<br />
 * Text: Although apples are red, I prefer blueberries. Furthermore, bananas are green, if only when
 * unripe.<br />
 * Keyword ngrams: <br />
 * although_furthermore, <br />
 * furthermore_if_only, <br />
 * although_furthermore_if_only, etc.<br />
 * <br />
 * Parameters are available to include sentence boundary markers, sentence text location, commas,
 * etc.
 * 
 * @author jamison
 * 
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class KeywordNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
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

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            keywords = FeatureUtil.getStopwords(keywordsFile, true);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(KeywordNGramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return KEYWORD_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "keyNG";
    }

    @Override
    protected int getTopN()
    {
        return keywordNgramUseTopK;
    }
}
