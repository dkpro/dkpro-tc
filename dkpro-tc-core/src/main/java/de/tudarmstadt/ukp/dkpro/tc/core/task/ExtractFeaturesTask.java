/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.core.lab.DynamicDiscriminableFunctionBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

/**
 * Executes all feature extractors and stores the feature representation (usually an Weka ARFF file)
 * on disk.
 * 
 * @author zesch
 * 
 */
public class ExtractFeaturesTask
    extends UimaTaskBase
{

    /**
     * Public name of the folder where the extracted features are stored within the task
     */
    public static final String OUTPUT_KEY = "output";
    /**
     * Public name of the folder where the input documents are stored within the task
     */
    public static final String INPUT_KEY = "input";

//    @Discriminator
//    protected List<String> featureSet;
    @Discriminator
    private List<String> featureFilters;
//    @Discriminator
//    protected List<Object> pipelineParameters;
    @Discriminator
    private File filesRoot;
    @Discriminator
    private Collection<String> files_training;
    @Discriminator
    private Collection<String> files_validation;
    @Discriminator
    private String learningMode;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String featureStore;
    @Discriminator
    private boolean developerMode;
    @Discriminator
    private List<DynamicDiscriminableFunctionBase<ExternalResourceDescription>> featureExtractors;

    private boolean isTesting = false;
    private Set<MetaCollector> metaCollectors;
    // TODO Issue 121: this is already prepared, but not used
    // collects annotation types required by FEs (source code annotations need to be inserted in
    // each FE)
    // could be used to automatically configure preprocessing
    private Set<String> requiredTypes;
    
    private TCMachineLearningAdapter mlAdapter;

	public void setMlAdapter(TCMachineLearningAdapter mlAdapter) {
		this.mlAdapter = mlAdapter;
	}

	/**
     * @param isTesting
     */
    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE);

        // Resolve the feature extractor closures to actual descritors
        List<ExternalResourceDescription> featureExtractorDescriptions = new ArrayList<>();
        
        // Configure the meta collectors for each feature extractor individually
        try {
            for (DynamicDiscriminableFunctionBase<ExternalResourceDescription> feClosure : featureExtractors) {
                ExternalResourceDescription feDesc = feClosure.getActualValue(aContext);
                featureExtractorDescriptions.add(feDesc);
                
                Class<?> feClass = Class.forName(feDesc.getImplementationName());
                
                // Skip feature extractors that are not dependent on meta collectors
                if (!MetaDependent.class.isAssignableFrom(feClass)) {
                    continue;
                }
    
                MetaDependent feInstance = (MetaDependent) feClass.newInstance();
                
                // Tell the meta collectors where to store their data
                for (MetaCollectorConfiguration conf : feInstance.getMetaCollectorClasses()) {
                    MetaInfoTask.configureStorageLocations(feDesc.getResourceSpecifier(), 
                            (String) feClosure.getDiscriminatorValue(), conf.extractorOverrides);
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }
                
        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        try {
            metaCollectors = TaskUtils.getMetaCollectorsFromFeatureExtractors(featureExtractorDescriptions);
            requiredTypes = TaskUtils.getRequiredTypesFromFeatureExtractors(featureExtractorDescriptions);
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
        
        // as feature filters are optional, check for null
        if (featureFilters == null) {
        	featureFilters = Collections.<String>emptyList();
        }
        
        AnalysisEngineDescription connector = TaskUtils.getFeatureExtractorConnector(
                outputDir.getAbsolutePath(), mlAdapter.getDataWriterClass().getName(), learningMode, featureMode, featureStore,
                true, developerMode, isTesting, featureFilters, featureExtractorDescriptions,
                aContext);

        return connector;
    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null) {
            File root = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {

            Collection<String> files = isTesting ? files_validation : files_training;
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
    }
}