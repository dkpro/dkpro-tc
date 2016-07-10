/*******************************************************************************
 * Copyright 2016
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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;

/**
 * Document or Pair are a special kind of unit processing in which only one target which spans over
 * the entire text span from 0 to documentTextLength(). If the feature mode is either document or
 * pair we set such an annotation automatically
 */
public class DocumentModeAnnotator
    extends JCasAnnotator_ImplBase
    implements Constants
{
    public static String PARAM_FEATURE_MODE = "PARAM_FEATURE_MODE";
    @ConfigurationParameter(name = "PARAM_FEATURE_MODE", mandatory = true)
    private String featureMode;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        if (featureMode.equals(Constants.FM_DOCUMENT)) {
            if (!JCasUtil.exists(aJCas, TextClassificationTarget.class)) {
                TextClassificationTarget target = new TextClassificationTarget(aJCas, 0,
                        aJCas.getDocumentText().length());
                target.addToIndexes();
            }
        }
    }

}