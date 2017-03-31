/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.core.task.uima;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataStreamWriter;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.fstore.filter.AdaptTestToTrainingFeaturesFilter;
import org.dkpro.tc.fstore.filter.FeatureFilter;

import com.google.gson.Gson;

/**
 * UIMA analysis engine that is used in the {@link ExtractFeaturesTask} to apply the feature
 * extractors on each CAS.
 */
public class ExtractFeaturesStreamConnector
    extends ConnectorBase
{

    /**
     * Directory in which the extracted features will be stored
     */
    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

    /**
     * Whether an ID should be added to each instance in the feature file
     */
    public static final String PARAM_FEATURE_CONNECTOR_CONFIGURATION = "featureConnectorConfiguration";
    @ConfigurationParameter(name = PARAM_FEATURE_CONNECTOR_CONFIGURATION, mandatory = true)
    private String jsonConfiguration;

    FeatureConnectorConfiguration fcc;
    
    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    public static final String JSON = "json.txt";

    /*
     * Default value as String; see https://code.google.com/p/dkpro-tc/issues/detail?id=200#c9
     */
    @ConfigurationParameter(name = PARAM_USE_SPARSE_FEATURES, mandatory = true, defaultValue = "false")
    private boolean useSparseFeatures;

    DataStreamWriter dsw;

    TreeSet<String> featureNames;
    Set<String> uniqueOutcomes;

    File jsonTempFile;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        loadConfiguration();

        featureNames = new TreeSet<>();
        uniqueOutcomes = new HashSet<>();

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }

        jsonTempFile = new File(fcc.outputDir, JSON);

        try {
            dsw = (DataStreamWriter) Class
                    .forName("org.dkpro.tc.ml.weka.writer.WekaStreamDataWriter").newInstance();
            dsw.init(jsonTempFile);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void loadConfiguration()
    {
        fcc = new Gson().fromJson(jsonConfiguration, FeatureConnectorConfiguration.class);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        getLogger().log(Level.INFO, "--- feature extraction for CAS with id ["
                + JCasUtil.selectSingle(jcas, JCasId.class).getId() + "] ---");

        List<Instance> instances = new ArrayList<Instance>();
        try {
            if (fcc.featureMode.equals(Constants.FM_SEQUENCE)) {
                instances = TaskUtils.getMultipleInstancesSequenceMode(featureExtractors, jcas,
                        fcc.setInstanceId, useSparseFeatures);
            }
            else if (fcc.featureMode.equals(Constants.FM_UNIT)) {
                instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas,
                        fcc.setInstanceId, useSparseFeatures);
            }
            else {
                instances.add(TaskUtils.getSingleInstance(fcc.featureMode, featureExtractors, jcas,
                        fcc.developerMode, fcc.setInstanceId, useSparseFeatures));
            }

            dsw.write(instances);

        }
        catch (Exception e1) {
            throw new AnalysisEngineProcessException(e1);
        }

        for (Instance i : instances) {
            for (Feature f : i.getFeatures()) {
                featureNames.add(f.getName());
            }
            for (String o : i.getOutcomes()) {
                uniqueOutcomes.add(o);
            }
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            dsw.close();

            // FIXME: How to implement filtering
            applyFilter(jsonTempFile);

            writeOutcomes();

            if (!fcc.isTesting) {
                writeFeatureNames();
            }
            else {
                applyFeatureNameFilter();
            }

            dsw.transform(fcc.outputDir, useSparseFeatures, fcc.learningMode, fcc.applyWeighting);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void writeOutcomes()
        throws AnalysisEngineProcessException
    {
        File outcomesFile = new File(fcc.outputDir, Constants.FILENAME_OUTCOMES);
        try {
            FileUtils.writeLines(outcomesFile, "utf-8", uniqueOutcomes);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void applyFeatureNameFilter()
        throws AnalysisEngineProcessException
    {
        try {
            File featureNamesFile = new File(fcc.trainFolder, Constants.FILENAME_FEATURES);
            TreeSet<String> trainFeatureNames;

            trainFeatureNames = new TreeSet<>(FileUtils.readLines(featureNamesFile));

            AdaptTestToTrainingFeaturesFilter filter = new AdaptTestToTrainingFeaturesFilter(trainFeatureNames);
            if (!trainFeatureNames.equals(featureNames)) {
                filter.applyFilter(new File(fcc.outputDir, JSON));

            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void writeFeatureNames()
        throws AnalysisEngineProcessException
    {
        try {
            FileUtils.writeLines(new File(fcc.outputDir, Constants.FILENAME_FEATURES),
                    featureNames);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void applyFilter(File jsonTempFile)
        throws AnalysisEngineProcessException
    {
        // apply filters that influence the whole feature store
        // filters are applied in the order that they appear as parameters
        for (String filterString : fcc.featureFilters) {
            FeatureFilter filter;
            try {
                filter = (FeatureFilter) Class.forName(filterString).newInstance();

                if (filter.isApplicableForTraining() && !fcc.isTesting
                        || filter.isApplicableForTesting() && fcc.isTesting) {
                    filter.applyFilter(jsonTempFile);
                }
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }
}