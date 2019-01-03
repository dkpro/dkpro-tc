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
package org.dkpro.tc.core.task.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.JCasId;

public class PreprocessConnector
    extends JCasAnnotator_ImplBase
{
    private int nrofProcessCalls;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
        nrofProcessCalls = 0;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        getLogger().log(Level.FINE, "--- preprocessing of CAS with id ["
                + JCasUtil.selectSingle(aJCas, JCasId.class).getId() + "] ---");

        nrofProcessCalls++;

        getLogger().log(Level.FINE, "--- preprocessing of CAS with id ["
                + JCasUtil.selectSingle(aJCas, JCasId.class).getId() + "] complete ---");
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        if (nrofProcessCalls == 0) {
            throw new AnalysisEngineProcessException(
                    new TextClassificationException("There are no documents to process."));
        }

    }
}