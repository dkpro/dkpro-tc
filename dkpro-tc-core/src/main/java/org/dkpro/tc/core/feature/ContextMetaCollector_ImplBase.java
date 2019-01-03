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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.meta.MetaCollector;

/**
 * Extract the context of each unit (in a sequence) and write it to a special file.
 *
 */
public abstract class ContextMetaCollector_ImplBase
    extends MetaCollector
{
    public static final String PARAM_CONTEXT_FOLDER = "contextFolder";
    @ConfigurationParameter(name = PARAM_CONTEXT_FOLDER, mandatory = true)
    protected File folder;

    public static final String CONTEXT_KEY = "id2context.txt";

    protected BufferedWriter bw;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(folder, CONTEXT_KEY)), "utf-8"));
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            bw.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
