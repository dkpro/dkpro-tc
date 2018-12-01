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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import static java.nio.charset.StandardCharsets.UTF_8;
public class MappingAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_INSTANCE_ANNOTATION = "instanceAnnotation";
    @ConfigurationParameter(name = PARAM_INSTANCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    protected String instanceTypeName;

    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;

    public static final String PARAM_START_INDEX_INSTANCES = "startIndexInstances";
    @ConfigurationParameter(name = PARAM_START_INDEX_INSTANCES, mandatory = true)
    protected int startIndexInstances;

    public static final String PARAM_START_INDEX_OUTCOMES = "startIndexOutcomes";
    @ConfigurationParameter(name = PARAM_START_INDEX_OUTCOMES, mandatory = true)
    protected int startIndexOutcomes;

    TreeSet<String> token;

    File instanceMappingFile;
    File outcomeMappingFile;
    Type instanceType;

    Map<String, Integer> instanceMap;
    Set<String> outcomeSet;

    // We start to count at 1 as zero might be reserved
    int instanceIdx = -1;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        instanceIdx = startIndexInstances;

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
        outcomeSet = new HashSet<>();

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        mapInstances(aJCas);
        collectOutcomes(aJCas);
    }

    private void collectOutcomes(JCas aJCas)
    {
        Collection<TextClassificationOutcome> tcos = JCasUtil.select(aJCas,
                TextClassificationOutcome.class);
        for (TextClassificationOutcome o : tcos) {
            String outcome = o.getOutcome();
            outcomeSet.add(outcome);
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
            FileUtils.writeStringToFile(instanceMappingFile, toString(instanceMap), UTF_8);
            FileUtils.writeStringToFile(outcomeMappingFile, toString(outcomeSet, startIndexOutcomes), UTF_8);
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private String toString(Set<String> aSet, int startAt)
    { 
        
        StringBuilder sb = new StringBuilder();

        List<String> keys = new ArrayList<>(aSet);
        Collections.sort(keys);
        
        int idx = startAt;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            sb.append(key + "\t" + idx++ + "\n");
        }

        return sb.toString();
    }

    private String toString(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();

        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            sb.append(key + "\t" + map.get(key));
            if (i + 1 < keys.size()) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}
