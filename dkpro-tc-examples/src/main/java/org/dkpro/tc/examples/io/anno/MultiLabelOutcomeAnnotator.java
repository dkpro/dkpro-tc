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
package org.dkpro.tc.examples.io.anno;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class MultiLabelOutcomeAnnotator
    extends JCasAnnotator_ImplBase
{

    /**
     * Path to the file containing the gold standard labels.
     */
    public static final String PARAM_GOLD_LABEL_FILE = "GoldLabelFile";
    @ConfigurationParameter(name = PARAM_GOLD_LABEL_FILE, mandatory = true)
    private String goldLabelFile;

    private Map<String, List<String>> goldLabelMap;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        goldLabelMap = new HashMap<String, List<String>>();

        try {
            URL resourceUrl = ResourceUtils.resolveLocation(goldLabelFile, this, context);

            for (String line : FileUtils.readLines(new File(resourceUrl.toURI()))) {
                String[] parts = line.split(" ");

                if (parts.length < 2) {
                    throw new IOException("Wrong file format in line: " + line);
                }
                String fileId = parts[0].split("/")[1];

                List<String> labels = new ArrayList<String>();
                for (int i=1; i<parts.length; i++) {
                    labels.add(parts[i]);
                }

                goldLabelMap.put(fileId, labels);
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (URISyntaxException ex) {
            throw new ResourceInitializationException(ex);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            for (String outcomeValue : getTextClassificationOutcomes(jcas)) {
                TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
                outcome.setOutcome(outcomeValue);
                outcome.addToIndexes();
            }
        }
        catch (CollectionException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    public Set<String> getTextClassificationOutcomes(JCas jcas)
        throws CollectionException
    {
        Set<String> outcomes = new HashSet<String>();
    
        DocumentMetaData dmd = DocumentMetaData.get(jcas);
        String titleWithoutExtension = FilenameUtils.removeExtension(dmd.getDocumentTitle());

        if (!goldLabelMap.containsKey(titleWithoutExtension)) {
            throw new CollectionException(new Throwable("No gold label for document: " + dmd.getDocumentTitle()));
        }

        for (String label : goldLabelMap.get(titleWithoutExtension)) {
            outcomes.add(label);
        }
        return outcomes;
    }

}
