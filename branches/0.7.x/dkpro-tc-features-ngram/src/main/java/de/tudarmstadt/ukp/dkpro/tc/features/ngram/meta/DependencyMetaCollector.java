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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.DependencyDFE;

public class DependencyMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String DEP_FD_KEY = "dep.ser";

    @ConfigurationParameter(name = DependencyDFE.PARAM_DEP_FD_FILE, mandatory = true)
    private File depFdFile;

    @ConfigurationParameter(name = DependencyDFE.PARAM_LOWER_CASE_DEPS, mandatory = false, defaultValue = "true")
    private boolean lowerCaseDeps;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        for (Dependency dep : JCasUtil.select(jcas, Dependency.class)) {
            String type = dep.getDependencyType();
            String governor = dep.getGovernor().getCoveredText();
            String dependent = dep.getDependent().getCoveredText();

            String dependencyString = getDependencyString(governor, dependent, type, lowerCaseDeps);
            fd.inc(dependencyString);
        }
    }

    public static String getDependencyString(String governor, String dependent, String type,
            boolean lowerCase)
    {
        if (lowerCase) {
            governor = governor.toLowerCase();
            dependent = dependent.toLowerCase();
        }

        return governor + "-" + type + "-" + dependent;
    }

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(DependencyDFE.PARAM_DEP_FD_FILE, DEP_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile()
    {
        return depFdFile;
    }
}