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
package org.dkpro.tc.core.task.deep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.DeepLearningConstants;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PruneEmbeddingAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_EMBEDDING_PATH = "inputEmbedding";
    @ConfigurationParameter(name = PARAM_EMBEDDING_PATH, mandatory = true)
    protected File inputEmbedding;

    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;

    TreeSet<String> token;

    File outputFile;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        token = new TreeSet<>();

        outputFile = new File(targetFolder, DeepLearningConstants.FILENAME_TOKEN);

        if (inputEmbedding != null && !inputEmbedding.exists()) {
            throw new IllegalArgumentException(
                    "Embedding path not found [" + inputEmbedding.getAbsolutePath() + "]");
        }
        
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        for (Token t : JCasUtil.select(aJCas, Token.class)) {
            token.add(t.getCoveredText());
        }
    }

    @Override
    public void collectionProcessComplete()
    {
        try {
            FileUtils.writeLines(outputFile, "utf-8", token);
            
            if (inputEmbedding == null) {
                LogFactory.getLog(getClass()).debug("No embedding was provided");
                return;
            }
            pruneEmbedding();
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private void pruneEmbedding()
        throws Exception
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputEmbedding), "utf-8"));

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(
                        new File(targetFolder, DeepLearningConstants.FILENAME_PRUNED_EMBEDDING)),
                "utf-8"));

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] split = line.split(" ");
            if (token.contains(split[0])) {
                writer.write(line + System.lineSeparator());
            }
        }

        writer.close();
        reader.close();
    }

}
