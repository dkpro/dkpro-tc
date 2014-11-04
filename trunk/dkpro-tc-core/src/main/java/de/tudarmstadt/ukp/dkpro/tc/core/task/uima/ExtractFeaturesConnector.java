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
package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.fstore.filter.FeatureStoreFilter;

/**
 * UIMA analysis engine that is used in the {@link ExtractFeaturesTask} to apply the feature
 * extractors on each CAS.
 *
 * @author zesch
 */
public class ExtractFeaturesConnector
        extends ConnectorBase
{

    /**
     * Directory in which the extracted features will be stored
     */
    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    /**
     * Whether an ID should be added to each instance in the feature file
     */
    public static final String PARAM_ADD_INSTANCE_ID = "addInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true, defaultValue = "true")
    private boolean addInstanceId;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_FEATURE_FILTERS, mandatory = true)
    private String[] featureFilters;
    
    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriterClass;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true,
            defaultValue = Constants.LM_SINGLE_LABEL)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true,
            defaultValue = Constants.FM_DOCUMENT)
    private String featureMode;

    @ConfigurationParameter(name = PARAM_DEVELOPER_MODE, mandatory = true, defaultValue = "false")
    private boolean developerMode;

    @ConfigurationParameter(name = PARAM_IS_TESTING, mandatory = true)
    private boolean isTesting;
    
    protected FeatureStore featureStore;

    /*
     * Default value as String; see https://code.google.com/p/dkpro-tc/issues/detail?id=200#c9
     */
    @ConfigurationParameter(name = PARAM_FEATURE_STORE_CLASS, mandatory = true,
            defaultValue = "de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore")
    private String featureStoreClass;

    private int sequenceId;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            featureStore = (FeatureStore) Class.forName(featureStoreClass).newInstance();
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        sequenceId = 0;

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }
    }

    @Override
    public void process(JCas jcas)
            throws AnalysisEngineProcessException
    {

        List<Instance> instances = new ArrayList<Instance>();
        if (featureMode.equals(Constants.FM_SEQUENCE)) {
            instances = TaskUtils.getMultipleInstances(featureExtractors, jcas, addInstanceId, sequenceId);
            sequenceId++;
        }
        else {
            instances.add(TaskUtils.getSingleInstance(featureMode, featureExtractors, jcas,
                    developerMode, addInstanceId));
        }

        for (Instance instance : instances) {
            try {
                this.featureStore.addInstance(instance);
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        // apply filters that influence the whole feature store
        // filters are applied in the order that they appear as parameters
        for (String filterString : featureFilters) {
        	FeatureStoreFilter filter = null;
			try {
				filter = (FeatureStoreFilter) Class.forName(filterString).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new AnalysisEngineProcessException(e);
			}
			
        	if (filter.isApplicableForTraining() && !isTesting || filter.isApplicableForTesting() && isTesting) {
                filter.applyFilter(featureStore);
            }
        }
        
        // write feature names file if in training m ode
        if (!isTesting) {
        	String featureNames = StringUtils.join(featureStore.getFeatureNames(), "\n");
        	try {
				FileUtils.writeStringToFile(new File(outputDirectory, Constants.FILENAME_FEATURES), featureNames);
			} catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
        }

        // FIXME if the feature store now determines whether to use dense or sparse instances, 
        // we might get rid of the corresponding parameter here
        // addInstanceId requires dense instances
        try {
            DataWriter writer = (DataWriter) Class.forName(dataWriterClass).newInstance();
            writer.write(outputDirectory, featureStore, true, learningMode);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}