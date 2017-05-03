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

    public static final String PARAM_FEATURE_CONNECTOR_CONFIGURATION = "featureConnectorConfiguration";
    @ConfigurationParameter(name = PARAM_FEATURE_CONNECTOR_CONFIGURATION, mandatory = true)
    private String jsonConfiguration;

    FeatureConnectorConfiguration fcc;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    DataStreamWriter dsw;

    TreeSet<String> featureNames;
    Set<String> uniqueOutcomes;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            loadConfiguration();

            featureNames = new TreeSet<>();
            uniqueOutcomes = new HashSet<>();

            if (fcc.isTesting) {
                File featureNamesFile = new File(fcc.outputDir, Constants.FILENAME_FEATURES);
                featureNames = new TreeSet<>(FileUtils.readLines(featureNamesFile, "utf-8"));
            }

            if (featureExtractors.length == 0) {
                context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
                throw new ResourceInitializationException();
            }

            // FIXME hardcoded at the moment for testing
            dsw = (DataStreamWriter) Class
                    .forName("org.dkpro.tc.ml.crfsuite.writer.CRFSuiteDataStreamWriter")
                    .newInstance();
            dsw.init(fcc.outputDir, fcc.useSparseFeatures, fcc.learningMode, fcc.applyWeighting);
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
                        fcc.setInstanceId, fcc.useSparseFeatures);
            }
            else if (fcc.featureMode.equals(Constants.FM_UNIT)) {
                instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas,
                        fcc.setInstanceId, fcc.useSparseFeatures);
            }
            else {
                instances.add(TaskUtils.getSingleInstance(fcc.featureMode, featureExtractors, jcas,
                        fcc.developerMode, fcc.setInstanceId, fcc.useSparseFeatures));
            }

            /*
             * filter-out feature names which did not occur during training if we are in the testing
             * stage
             */
            instances = enforceMatchingFeatures(instances);

            if (fcc.featureFilters.size() > 0 || !dsw.canStream()) {
                dsw.writeGenericFormat(instances);
            }
            else {
                dsw.writeClassifierFormat(instances, dsw.classiferReadsCompressed());
            }

        }
        catch (Exception e1) {
            throw new AnalysisEngineProcessException(e1);
        }

        trackFeatureNamesAndOutcomes(instances);
    }

    private void trackFeatureNamesAndOutcomes(List<Instance> instances)
    {
        for (Instance i : instances) {
            if (!fcc.isTesting) {
                for (Feature f : i.getFeatures()) {
                    featureNames.add(f.getName());
                }
            }
            for (String o : i.getOutcomes()) {
                uniqueOutcomes.add(o);
            }
        }
    }

    private List<Instance> enforceMatchingFeatures(List<Instance> instances)
    {
        if (!fcc.isTesting) {
            return instances;
        }

        List<Instance> out = new ArrayList<>();

        for (Instance i : instances) {
            List<Feature> newFeatures = new ArrayList<>();
            for (Feature feat : i.getFeatures()) {
                if (!featureNames.contains(feat.getName())) {
                    continue;
                }
                newFeatures.add(feat);
            }
            i.setFeatures(newFeatures);
            out.add(i);
        }
        return out;
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            dsw.close();

            // FIXME: The filters depend on the data format which is not checked - the generic data
            // format the filter expects might be different to the one of the classifier
            if (fcc.featureFilters.size() > 0) {
                applyFilter(new File(fcc.outputDir, dsw.getGenericFileName()));
            }

            writeOutcomes();

            if (!fcc.isTesting) {
                writeFeatureNames();
            }

            if (fcc.featureFilters.size() > 0 || !dsw.canStream()) {
                // if we use generic mode we have to finalize the feature extraction by transforming
                // the generic file into the classifier-specific data format
                dsw.transformFromGeneric();
            }
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