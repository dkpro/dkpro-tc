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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.DeepLearningConstants;

public class MappingAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_INSTANCE_ANNOTATION = "instanceAnnotation";
    @ConfigurationParameter(name = PARAM_INSTANCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    protected String instanceTypeName;

    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;
    
    public static final String PARAM_START_INDEX = "startIndexMllowestIdx";
    @ConfigurationParameter(name = PARAM_START_INDEX, mandatory = true)
    protected int startIndex;

    TreeSet<String> token;

    File instanceMappingFile;
    File outcomeMappingFile;
    Type instanceType;

    Map<String, Integer> instanceMap;
    Map<String, Integer> outcomeMap;

    // We start to count at 1 as zero might be reserved
    int instanceIdx = -1;
    int outcomeIdx = -1;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        instanceIdx = startIndex;
        outcomeIdx = startIndex;

        instanceMappingFile = new File(targetFolder,
                DeepLearningConstants.FILENAME_INSTANCE_MAPPING);
        outcomeMappingFile = new File(targetFolder, DeepLearningConstants.FILENAME_OUTCOME_MAPPING);

        try {
            JCas typeFactory = JCasFactory.createJCas();
            Type type = JCasUtil.getType(typeFactory, Class.forName(instanceTypeName));
            AnnotationFS createAnnotation = typeFactory.getCas().createAnnotation(type, 0, 0);
            instanceType = createAnnotation.getType();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        instanceMap = new HashMap<>();
        outcomeMap = new HashMap<>();

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        mapInstances(aJCas);
        mapOutcomes(aJCas);
    }

    private void mapOutcomes(JCas aJCas)
    {
        Collection<TextClassificationOutcome> tcos = JCasUtil.select(aJCas,
                TextClassificationOutcome.class);
        for (TextClassificationOutcome o : tcos) {
            String outcome = o.getOutcome();
            if (outcomeMap.containsKey(outcome)) {
                continue;
            }
            outcomeMap.put(outcome, outcomeIdx++);
        }
    }

    private void mapInstances(JCas aJCas)
    {
        Collection<AnnotationFS> select = CasUtil.select(aJCas.getCas(), instanceType);
        for (AnnotationFS afs : select) {
            String instance = afs.getCoveredText();
            if (instanceMap.containsKey(instance)) {
                continue;
            }
            instanceMap.put(afs.getCoveredText(), instanceIdx++);
        }
    }

    @Override
    public void collectionProcessComplete()
    {
        try {
            FileUtils.writeStringToFile(instanceMappingFile, toString(instanceMap), "utf-8");
            FileUtils.writeStringToFile(outcomeMappingFile, toString(outcomeMap), "utf-8");
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private String toString(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();

        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            sb.append(key + "\t" + map.get(key) + System.lineSeparator());
        }

        return sb.toString();
    }

}
