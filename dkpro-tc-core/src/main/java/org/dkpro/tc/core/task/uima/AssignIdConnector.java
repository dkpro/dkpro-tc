/*******************************************************************************
 * Copyright 2017
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

import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;

public class AssignIdConnector
    extends JCasAnnotator_ImplBase
{
    private int jcasId;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        LogFactory.getLog(getClass()).info("--- validating CAS with id [" + jcasId + "] ---");

        boolean exists = JCasUtil.exists(aJCas, JCasId.class);
        if (!exists) {
            JCasId id = new JCasId(aJCas);
            id.setId(jcasId++);
            id.addToIndexes();
        }
    }

}