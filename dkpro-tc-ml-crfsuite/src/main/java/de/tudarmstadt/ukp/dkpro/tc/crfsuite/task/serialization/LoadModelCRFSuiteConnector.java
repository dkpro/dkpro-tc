/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer.CRFSuiteDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotator;

public class LoadModelCRFSuiteConnector
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    private static File model = null;

    private TemporaryFolder tmpFolder = new TemporaryFolder();

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = new File(tcModelLocation, MODEL_CLASSIFIER);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        List<Instance> allInstances = new ArrayList<Instance>();

        try {
            for (TextClassificationSequence seq : JCasUtil.select(jcas,
                    TextClassificationSequence.class)) {

                List<Instance> instances = TaskUtils.getInstancesInSequence(featureExtractors,
                        jcas, seq, true, new Random().nextInt());
                allInstances.addAll(instances);
            }

            File tmpFolderForFeatureFile = tmpFolder.newFolder();
            File featureFile = CRFSuiteDataWriter.writeFeatureFile(allInstances,
                    tmpFolderForFeatureFile);
            classify(featureFile);

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private static void classify(File featureFile)
        throws Exception
    {
        List<String> command = CRFSuiteTestTask.wrapTestCommandAsList(featureFile,
                CRFSuiteTestTask.getExecutablePath(), model.getAbsolutePath());

        String output = CRFSuiteTestTask.runTest(command);
        System.out.println(output);
        int a=0;
    }
}