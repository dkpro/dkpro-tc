/**
 * Copyright 2016
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
package org.dkpro.tc.weka.task;

import static org.dkpro.tc.core.task.MetaInfoTask.META_KEY;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import org.dkpro.tc.api.features.meta.MetaCollector;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.weka.task.uima.WekaExtractFeaturesAndPredictConnector;
import org.dkpro.tc.weka.util.WekaUtils;

/**
 * Executes all feature extractors and classifies instances with a previously trained model.
 */
public class WekaExtractFeaturesAndPredictTask
    extends UimaTaskBase
    implements Constants
{

    /**
     * Public name of the input folder within the task
     */
    public static final String INPUT_KEY = "input";
    /**
     * Public name of the folder where the extracted features are stored within the task
     */
    public static final String OUTPUT_KEY = "output";

    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    private String learningMode;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String threshold;
    @Discriminator
    private List<String> classificationArguments;
    @Discriminator
    private boolean developerMode;

    private Set<Class<? extends MetaCollector>> metaCollectorClasses;

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {

        File outputDir = aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE)
                .getParentFile();
        File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA, AdapterNameEntries.featureVectorsFile,AccessMode.READONLY);
                

        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        try {
            metaCollectorClasses = TaskUtils.getMetaCollectorsFromFeatureExtractors(featureSet);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        // collect parameter/key pairs that need to be set
        Map<String, String> parameterKeyPairs = new HashMap<String, String>();
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            try {
                parameterKeyPairs.putAll(metaCollectorClass.newInstance().getParameterKeyPairs());
            }
            catch (InstantiationException e) {
                throw new ResourceInitializationException(e);
            }
            catch (IllegalAccessException e) {
                throw new ResourceInitializationException(e);
            }
        }

        List<Object> parametersCopy = new ArrayList<Object>();
        if (pipelineParameters != null) {
            parametersCopy.addAll(pipelineParameters);
        }

        for (Entry<String, String> entry : parameterKeyPairs.entrySet()) {
            File file = new File(aContext.getFile(META_KEY, AccessMode.READONLY),
                    entry.getValue());
            parametersCopy.addAll(Arrays.asList(entry.getKey(), file.getAbsolutePath()));
        }

        // convert parameters to string as external resources only take string parameters
        List<Object> convertedParameters = new ArrayList<Object>();
        if (pipelineParameters != null) {
            for (Object parameter : parametersCopy) {
                convertedParameters.add(parameter.toString());
            }
        }
        List<ExternalResourceDescription> extractorResources = new ArrayList<ExternalResourceDescription>();
        for (String featureExtractor : featureSet) {
            try {
                extractorResources.add(ExternalResourceFactory.createExternalResourceDescription(
                        Class.forName(featureExtractor).asSubclass(Resource.class),
                        convertedParameters.toArray()));
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            }
        }

        // add the rest of the necessary parameters with the correct types
        parametersCopy.addAll(Arrays.asList(
                WekaExtractFeaturesAndPredictConnector.PARAM_FEATURE_MODE, featureMode,
                WekaExtractFeaturesAndPredictConnector.PARAM_LEARNING_MODE, learningMode,
                WekaExtractFeaturesAndPredictConnector.PARAM_FEATURE_EXTRACTORS, extractorResources,
                WekaExtractFeaturesAndPredictConnector.PARAM_ARFF_FILE_TRAINING, arffFileTrain,
                WekaExtractFeaturesAndPredictConnector.PARAM_BIPARTITION_THRESHOLD, threshold,
                WekaExtractFeaturesAndPredictConnector.PARAM_OUTPUT_DIRECTORY, outputDir,
                WekaExtractFeaturesAndPredictConnector.PARAM_DEVELOPER_MODE, developerMode,
                WekaExtractFeaturesAndPredictConnector.PARAM_CLASSIFICATION_ARGUMENTS,
                classificationArguments));

        return AnalysisEngineFactory.createEngineDescription(
                WekaExtractFeaturesAndPredictConnector.class, parametersCopy.toArray());

    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        File root = aContext.getFolder(INPUT_KEY, AccessMode.READONLY);
        Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
        return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                files);

    }
}