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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.NormalizationMode;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.WeightingModeIdf;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.WeightingModeTf;

/**
 * Extracts the document pair similarity using {@link dkpro.similarity.algorithms.lexical.string.CosineSimilarity
 * CosineSimilarity} (tokens) measure.
 * Please be aware this Cosine Similarity API has a history of bugginess.
 */
public class CosineFeatureExtractor
    extends LuceneFeatureExtractorBase // FeatureExtractorResource_ImplBase -> NGramFeatureExtractorBase -> LuceneFeatureExtractorBase
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
     * @see <a href="http://en.wikipedia.org/wiki/Norm_(mathematics)#Taxicab_norm_or_Manhattan_norm">Manhattan norm (Wikipedia)</a>
     * @see <a href="http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">Euclidean Norm (Wikipedia)</a>
     */
    public static final String PARAM_NORMALIZATION_MODE = "normalizationMode";
    @ConfigurationParameter(name = PARAM_NORMALIZATION_MODE, mandatory = false)
    private CosineSimilarity.NormalizationMode normalizationMode;

    private CosineSimilarity measure;
    
    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(IdfPairMetaCollector.class);

        return metaCollectorClasses;
    }
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        
        if(weightingModeTf == null){
        	weightingModeTf = CosineSimilarity.WeightingModeTf.FREQUENCY_LOGPLUSONE;
        }
        if(weightingModeIdf == null){
        	weightingModeIdf = CosineSimilarity.WeightingModeIdf.PASSTHROUGH;
        }
        if(normalizationMode == null){
        	normalizationMode = CosineSimilarity.NormalizationMode.L2;
        }
        
        
        measure = new CosineSimilarity(weightingModeTf, 
        		weightingModeIdf,
        		normalizationMode, 
				fdToMap(topKSet)); //DF counts
        return true;
    }
    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        try {
        	//Note: getSimilarity(String, String) is *not* a convenience 
        	// method for getSimilarity(Collection<String>, Collection<String>).
            List<String> text1 = new ArrayList<String>();
            List<String> text2 = new ArrayList<String>();
            for(Token token: JCasUtil.select(view1, Token.class)){
            	text1.add(token.getCoveredText().toLowerCase());
            }
            for(Token token: JCasUtil.select(view2, Token.class)){
            	text2.add(token.getCoveredText().toLowerCase());
            }
            double similarity = measure.getSimilarity(text1,
                  text2);

            
            // Temporary fix for DKPro Similarity Issue 30
            if (Double.isNaN(similarity)){
            	similarity = 0.0;
            }
            
            return Arrays.asList(new Feature("Similarity" + measure.getName(), similarity));
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
		return null; //CosSim is a numeric feature, no prefix
	}
    private static Map<String, Double> fdToMap(FrequencyDistribution<String> fD){
    	Map<String, Double> map = new HashMap<String, Double>();
    	for(String token: fD.getKeys()){
    		map.put(token, new Double(1.0 / fD.getCount(token)));
//    		map.put(token, new Double(fD.getCount(token)));
    	}
    	return map;
    	
    }
}