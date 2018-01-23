/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.core.task.deep.anno;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class FilterVocabularyByEmbeddingAnnotator
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_EMBEDDING = "embedding";
    @ConfigurationParameter(name = PARAM_EMBEDDING, mandatory = false)
    protected File embedding;

    Set<String> vocab = new HashSet<>();

    int droppedVocabulary = 0;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        if (embedding == null) {
            throw new ResourceInitializationException("The provided embedding file is null", null);
        }

        String line = null;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(embedding), "utf-8"));
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                vocab.add(split[0]);
            }
            reader.close();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        if (embedding == null) {
            return;
        }
        Collection<Token> select = JCasUtil.select(aJCas, Token.class);
        for (Token t : select) {
            if (vocab.contains(t.getCoveredText())) {
                continue;
            }
            POS pos = t.getPos();
            if (pos != null) {
                pos.removeFromIndexes();
                t.setPos(null);
            }
            t.removeFromIndexes();
            droppedVocabulary++;
        }
    }

    @Override
    public void collectionProcessComplete()
    {
        if (embedding == null) {
            return;
        }
        LogFactory.getLog(getClass()).info("Removed [" + droppedVocabulary
                + "] token from the vocabulary which did not occur in the provided word embedding");
    }

}
