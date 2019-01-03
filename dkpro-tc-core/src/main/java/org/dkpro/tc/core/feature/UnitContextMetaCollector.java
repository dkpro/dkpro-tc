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
package org.dkpro.tc.core.feature;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * A dummy meta-collector which merely sets up the correct file path for the context file. This is
 * required by the {@link ContextCollectorUFE}, which in turn is a prerequisite for the
 * BatchTrainTestDetailedOutcomeReport.
 */
public class UnitContextMetaCollector
    extends ContextMetaCollector_ImplBase
{

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        DocumentMetaData dmd = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
        try {
            bw.write(dmd.getDocumentId() + "\t" + aJCas.getDocumentText() + "\n");
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}