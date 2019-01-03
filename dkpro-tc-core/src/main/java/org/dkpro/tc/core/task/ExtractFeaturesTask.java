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
package org.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.ConnectorConstants;
import org.dkpro.tc.core.task.uima.ExtractFeaturesConnector;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Executes all feature extractors and stores the feature representation (usually an Weka ARFF file)
 * on disk.
 */
public class ExtractFeaturesTask
    extends UimaTaskBase
    implements Constants, ConnectorConstants
{

    /**
     * Public name of the folder where the extracted features are stored within the task
     */
    public static final String OUTPUT_KEY = "output";
    /**
     * Public name of the folder where the input documents are stored within the task
     */
    public static final String INPUT_KEY = "input";
    public static final String COLLECTION_INPUT_KEY = "collectionInput";

    @Discriminator(name = DIM_FEATURE_FILTERS)
    private List<String> featureFilters = Collections.<String> emptyList();

    @Discriminator(name = DIM_FILES_ROOT)
    private File filesRoot;

    @Discriminator(name = DIM_FILES_TRAINING)
    private Collection<String> files_training;

    @Discriminator(name = DIM_FILES_VALIDATION)
    private Collection<String> files_validation;

    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;

    @Discriminator(name = DIM_APPLY_INSTANCE_WEIGHTING)
    private boolean applyWeighting;

    @Discriminator(name = DIM_DATA_WRITER)
    private String dataWriter;
    
    @Discriminator(name = DIM_FEATURE_USE_SPARSE)
    private boolean useSparse;

    @Discriminator(name = DIM_FEATURE_SET)
    private TcFeatureSet featureExtractors;
    
    @Discriminator(name = DIM_ENFORCE_MATCHING_FEATURES_DURING_TESTING)
    private boolean enforceMatchingFeatures;

    private boolean isTesting = false;

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException
    {

        File outputDir = aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE);

        // Resolve the feature extractor closures to actual descritors
        List<ExternalResourceDescription> featureExtractorDescriptions = new ArrayList<>();

        // Configure the meta collectors for each feature extractor individually
        try {
            for (TcFeature feClosure : featureExtractors) {
                ExternalResourceDescription feDesc = feClosure.getActualValue();
                featureExtractorDescriptions.add(feDesc);

                Class<?> feClass = MetaInfoTask.getClass(feDesc);

                // Skip feature extractors that are not dependent on meta
                // collectors
                if (!MetaDependent.class.isAssignableFrom(feClass)) {
                    continue;
                }

                MetaDependent feInstance = (MetaDependent) feClass.newInstance();
                Map<String, Object> parameterSettings = ConfigurationParameterFactory
                        .getParameterSettings(feDesc.getResourceSpecifier());

                // Tell the meta collectors where to store their data
                for (MetaCollectorConfiguration conf : feInstance
                        .getMetaCollectorClasses(parameterSettings)) {
                    MetaInfoTask.configureStorageLocations(aContext, feDesc.getResourceSpecifier(),
                            (String) feClosure.getId(), conf.extractorOverrides,
                            AccessMode.READONLY);
                }
            }

            Set<String> requiredTypes = getRequiredTypesFromFeatureExtractors(
                    featureExtractorDescriptions);

            // as feature filters are optional, check for null
            if (featureFilters == null) {
                featureFilters = Collections.<String> emptyList();
            }

            // ensure that outcomes file is copied into this folder
            File folder = aContext.getFolder(COLLECTION_INPUT_KEY, AccessMode.READONLY);
            File file = new File(folder, Constants.FILENAME_OUTCOMES);
            String[] outcomes = FileUtils.readLines(file, "utf-8").toArray(new String[0]);

            List<Object> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, true,
                    ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputDir,
                    PARAM_APPLY_WEIGHTING, applyWeighting, PARAM_DATA_WRITER_CLASS,
                    dataWriter, PARAM_FEATURE_FILTERS, featureFilters,
                    PARAM_FEATURE_MODE, featureMode, PARAM_LEARNING_MODE, learningMode,
                    PARAM_IS_TESTING, isTesting, PARAM_USE_SPARSE_FEATURES,
                    useSparse, PARAM_OUTCOMES, outcomes, PARAM_FEATURE_EXTRACTORS,
                    featureExtractorDescriptions, PARAM_REQUIRED_TYPES, requiredTypes,
                    PARAM_ENFORCE_MATCHING_FEATURES, enforceMatchingFeatures));

            return AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
                    parameters.toArray());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
    	if(isLearningCurveTaskWithFixedTestSet()) {
    		if(isTesting) {
    			 File root = aContext.getFolder(INPUT_KEY, AccessMode.READONLY);
    	            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
    	            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
    	                    files);
    		}else {
    	            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
    	            		files_training);
    		}
    	}
    	//Train-test setups
    	else if (filesRoot == null) {
            File root = aContext.getFolder(INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // Cross-validation setup: filesRoot and files_training / validation have both to be set as dimension
        else {
            Collection<String> files = isTesting ? files_validation : files_training;
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
    }

    private boolean isLearningCurveTaskWithFixedTestSet() {
		// This a hybrid that has N CV folds which are exclusively used for training but
		// without validation set (from the CV dimension) but defines the validation set
		// as in a normal train-test setup.
    	return filesRoot != null && files_training != null && files_validation == null;
	}

	private Set<String> getRequiredTypesFromFeatureExtractors(
            List<ExternalResourceDescription> featureSet)
        throws Exception
    {

        Set<String> requirements = new HashSet<>();
        for (ExternalResourceDescription element : featureSet) {

            String implName;
            if (element.getResourceSpecifier() instanceof CustomResourceSpecifier) {
                implName = ((CustomResourceSpecifier) element.getResourceSpecifier())
                        .getResourceClassName();
            }
            else {
                implName = element.getImplementationName();
            }

            TypeCapability annotation = ReflectionUtil.getAnnotation(Class.forName(implName),
                    TypeCapability.class);

            if (annotation == null) {
                continue;
            }

            String[] inputs = annotation.inputs();
            StringBuilder sb = new StringBuilder();
            sb.append(implName + "|");
            for (int i = 0; i < inputs.length; i++) {
                sb.append(inputs[i]);
                if (i + 1 < inputs.length) {
                    sb.append("|");
                }
            }
            requirements.add(sb.toString());
        }

        return requirements;
    }
}