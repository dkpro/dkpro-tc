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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;

public abstract class LuceneNgramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
    @Override
    protected String getFieldName()
    {
        return LUCENE_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "ngram";
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
    
    @Override
    public List<AnalysisEngineDescription> getMetaCollectorClasses()
        throws ResourceInitializationException
    {
        return asList(createEngineDescription(LuceneNGramMetaCollector.class));
    }
}