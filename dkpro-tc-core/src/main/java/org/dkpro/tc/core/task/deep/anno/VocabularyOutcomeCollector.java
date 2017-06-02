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

public class VocabularyOutcomeCollector
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_INSTANCE_ANNOTATION = "instanceAnnotation";
    @ConfigurationParameter(name = PARAM_INSTANCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    protected String instanceTypeName;

    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;
    
    TreeSet<String> token;
    TreeSet<String> outcomes;

    Type instanceType;
	
    private File vocabularyFile;
	private File outcomeFile;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        token = new TreeSet<>();
        outcomes = new TreeSet<>();
        
        vocabularyFile = new File(targetFolder, DeepLearningConstants.FILENAME_VOCABULARY);
		outcomeFile = new File(targetFolder, DeepLearningConstants.FILENAME_OUTCOMES);

        try {
            JCas typeFactory = JCasFactory.createJCas();
            Type type = JCasUtil.getType(typeFactory, Class.forName(instanceTypeName));
            AnnotationFS createAnnotation = typeFactory.getCas().createAnnotation(type, 0, 0);
            instanceType = createAnnotation.getType();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        collectInstances(aJCas);
        collectOutcomes(aJCas);
    }

    private void collectOutcomes(JCas aJCas)
    {
        Collection<TextClassificationOutcome> tcos = JCasUtil.select(aJCas,
                TextClassificationOutcome.class);
        for (TextClassificationOutcome o : tcos) {
            String outcome = o.getOutcome();
            outcomes.add(outcome);
        }
    }

    private void collectInstances(JCas aJCas)
    {
        Collection<AnnotationFS> select = CasUtil.select(aJCas.getCas(), instanceType);
        for (AnnotationFS afs : select) {
            String instance = afs.getCoveredText();
            token.add(instance);
        }
    }

    @Override
    public void collectionProcessComplete()
    {
        try {
            FileUtils.writeStringToFile(vocabularyFile, toString(token), "utf-8");
            FileUtils.writeStringToFile(outcomeFile, toString(outcomes), "utf-8");
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }
    
    private String toString(TreeSet<String> tokens)
    {
        StringBuilder sb = new StringBuilder();

        for (String e : tokens) {
            sb.append(e + System.lineSeparator());
        }
        return sb.toString();
    }
}
