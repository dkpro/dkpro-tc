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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.ComboUtils;

public class IdfPairMetaCollector
	extends LuceneBasedMetaCollector
    implements Constants
{

    private Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        stopwords = new HashSet<String>();
    }
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
        
        FrequencyDistribution<String> documentNGrams;
        try{
            documentNGrams = getNgramsFD(jcases);
        }catch(TextClassificationException e){
            throw new AnalysisEngineProcessException(e);
        }

        for (String ngram : documentNGrams.getKeys()) {
        	// Only add a term once per document, no matter how many times it occurs in the doc.  "Document Frequency".
        	// This is different than other metacollectors.
            addField(jcas, getFieldName(), ngram);
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    // This is an inherited artifact that is never used.
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        return null;
    }
    protected FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
            throws TextClassificationException
        {
            return ComboUtils.getMultipleViewNgrams(
            jcases, null, true, false, 1, 1, stopwords);
        }
    
    @Override
    protected String getFieldName(){
        return LuceneNGramDFE.LUCENE_NGRAM_FIELD;
    }
}
