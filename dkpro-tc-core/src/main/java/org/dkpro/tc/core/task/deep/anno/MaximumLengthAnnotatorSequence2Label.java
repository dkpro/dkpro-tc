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
package org.dkpro.tc.core.task.deep.anno;

import java.io.File;
import java.util.Collection;

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
import org.dkpro.tc.core.DeepLearningConstants;

public class MaximumLengthAnnotatorSequence2Label
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;

    public static final String PARAM_SEQUENCE_ANNOTATION = "sequenceAnnotation";
    @ConfigurationParameter(name = PARAM_SEQUENCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence")
    protected String sequenceSpanTypeName;

    public static final String PARAM_INSTANCE_ANNOTATION = "instanceAnnotation";
    @ConfigurationParameter(name = PARAM_INSTANCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    protected String instanceTypeName;

    File outputFile;
    Type instanceType;
    Type sequenceSpanType;

    int maximumLength = -1;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        outputFile = new File(targetFolder, DeepLearningConstants.FILENAME_MAXIMUM_LENGTH);

        try {
            JCas typeFactory = JCasFactory.createJCas();
            Type type = JCasUtil.getType(typeFactory, Class.forName(sequenceSpanTypeName));
            AnnotationFS sequenceAnno = typeFactory.getCas().createAnnotation(type, 0, 0);
            sequenceSpanType = sequenceAnno.getType();

            type = JCasUtil.getType(typeFactory, Class.forName(instanceTypeName));
            AnnotationFS tokenAnno = typeFactory.getCas().createAnnotation(type, 0, 0);
            instanceType = tokenAnno.getType();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<AnnotationFS> sequences = CasUtil.select(aJCas.getCas(), sequenceSpanType);

        for (AnnotationFS s : sequences) {
            int seqSize = CasUtil.selectCovered(aJCas.getCas(), instanceType, s).size();
            if (seqSize > maximumLength) {
                maximumLength = seqSize;
            }
        }
    }

    @Override
    public void collectionProcessComplete()
    {
        try {
            FileUtils.writeStringToFile(outputFile, maximumLength + "", "utf-8");
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
