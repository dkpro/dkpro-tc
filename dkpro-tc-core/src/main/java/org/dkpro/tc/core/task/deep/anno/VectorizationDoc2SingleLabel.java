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
package org.dkpro.tc.core.task.deep.anno;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
public class VectorizationDoc2SingleLabel
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;

    public static final String PARAM_INSTANCE_ANNOTATION = "instanceAnnotation";
    @ConfigurationParameter(name = PARAM_INSTANCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    protected String instanceTypeName;

    public static final String PARAM_PREPARATION_DIRECTORY = "mappingDirectory";
    @ConfigurationParameter(name = PARAM_PREPARATION_DIRECTORY, mandatory = true)
    protected File preparationFolder;

    public static final String PARAM_TO_INTEGER = "mapToInteger";
    @ConfigurationParameter(name = PARAM_TO_INTEGER, mandatory = true, defaultValue = "false")
    protected boolean toInteger;

    File instanceVectorFile;
    File outcomeVectorFile;

    Map<String, Integer> instanceMap = new HashMap<>();
    Map<String, Integer> outcomeMap = new HashMap<>();
    private Type instanceType;

    BufferedWriter writerInstance;
    BufferedWriter writerOutcome;

    int maximumLength = 0;

    StringBuilder outcomeVector = new StringBuilder();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        instanceVectorFile = new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_VECTOR);
        outcomeVectorFile = new File(targetFolder, DeepLearningConstants.FILENAME_OUTCOME_VECTOR);

        try {
            if (toInteger) {
                loadMapping(instanceMap, DeepLearningConstants.FILENAME_INSTANCE_MAPPING);
                loadMapping(outcomeMap, DeepLearningConstants.FILENAME_OUTCOME_MAPPING);
            }

            // load the type of the annotation that holds the instances
            JCas typeFactory = JCasFactory.createJCas();
            Type type = JCasUtil.getType(typeFactory, Class.forName(instanceTypeName));
            AnnotationFS createAnnotation = typeFactory.getCas().createAnnotation(type, 0, 0);
            instanceType = createAnnotation.getType();

            writerInstance = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(instanceVectorFile), UTF_8));
            writerOutcome = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outcomeVectorFile), UTF_8));

            maximumLength = getMaximumLength();

        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    private int getMaximumLength() throws IOException
    {
        String text = FileUtils.readFileToString(
                new File(preparationFolder, DeepLearningConstants.FILENAME_MAXIMUM_LENGTH),
                UTF_8);
        return Integer.parseInt(text);
    }

    private void loadMapping(Map<String, Integer> m, String f) throws IOException
    {
        List<String> lines = FileUtils.readLines(new File(preparationFolder, f), UTF_8);
        for (String s : lines) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("\t");
            m.put(split[0], Integer.valueOf(split[1]));
        }

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        try {
            processInstances(aJCas);
            processOutcome(aJCas);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void processOutcome(JCas aJCas) throws Exception
    {
        List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(aJCas, TextClassificationOutcome.class));

        for (int i = 0; i < outcomes.size(); i++) {
            String outcome = outcomes.get(i).getOutcome();

            if (toInteger) {
                outcomeVector.append(outcomeMap.get(outcome).toString());
            }
            else {
                outcomeVector.append(outcome);
            }
        }
        outcomeVector.append(" ");
    }

    private void processInstances(JCas aJCas) throws Exception
    {

        List<AnnotationFS> annos = new ArrayList<AnnotationFS>(
                CasUtil.select(aJCas.getCas(), instanceType));
        for (int i = 0; i < annos.size(); i++) {
            AnnotationFS a = annos.get(i);

            if (toInteger) {
                Integer intIdOfInstance = instanceMap.get(a.getCoveredText());
                writerInstance.write(intIdOfInstance.toString());
            }
            else {
                writerInstance.write(a.getCoveredText());
            }

            if (i + 1 < annos.size() && i + 1 < maximumLength) {
                writerInstance.write(" ");
            }

            if (i + 1 >= maximumLength) {
                break;
            }
        }

        writerInstance.write("\n");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void collectionProcessComplete()
    {
        try {
            writerOutcome.write(outcomeVector.toString().trim());
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        IOUtils.closeQuietly(writerInstance);
        IOUtils.closeQuietly(writerOutcome);
    }

}
