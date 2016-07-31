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
package org.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.tc.core.Constants.DIM_FEATURE_MODE;
import static org.dkpro.tc.core.Constants.DIM_FEATURE_SET;
import static org.dkpro.tc.core.Constants.DIM_FILES_ROOT;
import static org.dkpro.tc.core.Constants.DIM_FILES_TRAINING;
import static org.dkpro.tc.core.Constants.DIM_RECORD_CONTEXT;
import static org.dkpro.tc.core.Constants.FM_SEQUENCE;
import static org.dkpro.tc.core.Constants.FM_UNIT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.core.feature.SequenceContextMetaCollector;
import org.dkpro.tc.core.feature.UnitContextMetaCollector;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Iterates over all documents and stores required collection-level meta data, e.g. which n-grams
 * appear in the documents.
 * 
 */
public class MetaInfoTask
    extends UimaTaskBase
{

    /**
     * Public name of the task key
     */
    public static final String META_KEY = "meta";
    /**
     * Public name of the folder where meta information will be stored within the task
     */
    public static final String INPUT_KEY = "input";

    private List<String> operativeViews;

    @Discriminator(name = DIM_FEATURE_SET)
    private TcFeatureSet featureExtractors;

    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;

    @Discriminator(name = DIM_FILES_ROOT)
    private File filesRoot;

    @Discriminator(name = DIM_FILES_TRAINING)
    private Collection<String> files_training;

    @Discriminator(name = DIM_RECORD_CONTEXT)
    private boolean recordContext;

    private Set<String> featureExtractorNames = new HashSet<>();

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null || files_training == null) {
            File root = aContext.getFolder(INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);

            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files_training);
        }
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        featureExtractorNames = new HashSet<>();
        // check for error conditions
        if (featureExtractors == null) {
            throw new ResourceInitializationException(new TextClassificationException(
                    "No feature extractors have been added to the experiment."));
        }

        // Resolve the feature extractor closures to actual descritors
        List<ExternalResourceDescription> featureExtractorDescriptions = new ArrayList<>();
        for (TcFeature fc : featureExtractors) {
            featureExtractorDescriptions.add(fc.getActualValue());
        }

        List<AnalysisEngineDescription> metaCollectors = new ArrayList<>();

        if (recordContext) {
            AnalysisEngineDescription aed = injectContextMetaCollector(aContext);
            if (aed == null) {
                throw new NullPointerException(
                        "Initializing a ContextMetaCollector returned an AnalysisEngineDescription which was [NULL]");
            }
            metaCollectors.add(aed);
        }

        try {

            // Configure the meta collectors for each feature extractor individually
            for (TcFeature feClosure : featureExtractors) {
                ExternalResourceDescription feDesc = feClosure.getActualValue();

                Class<?> feClass = getClass(feDesc);

                // Skip feature extractors that are not dependent on meta collectors
                if (!MetaDependent.class.isAssignableFrom(feClass)) {
                    continue;
                }

                MetaDependent feInstance = (MetaDependent) feClass.newInstance();
                Map<String, Object> parameterSettings = ConfigurationParameterFactory
                        .getParameterSettings(feDesc.getResourceSpecifier());

                validateUniqueFeatureExtractorNames(parameterSettings);

                // Tell the meta collectors where to store their data
                for (MetaCollectorConfiguration conf : feInstance
                        .getMetaCollectorClasses(parameterSettings)) {
                    configureStorageLocations(aContext, conf.descriptor,
                            (String) feClosure.getId(), conf.collectorOverrides,
                            AccessMode.READWRITE);
                    metaCollectors.add(conf.descriptor);
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        // make sure that the meta key import can be resolved (even when no meta features have been
        // extracted, as in the regression demo)
        aContext.getFolder(META_KEY, AccessMode.READONLY);

        AggregateBuilder builder = new AggregateBuilder();

        for (AnalysisEngineDescription metaCollector : metaCollectors) {
            if (operativeViews != null) {
                for (String viewName : operativeViews) {
                    builder.add(metaCollector, CAS.NAME_DEFAULT_SOFA, viewName);
                }
            }
            else {
                builder.add(metaCollector);
            }
        }
        return builder.createAggregateDescription();
    }

    public static Class<?> getClass(ExternalResourceDescription feDesc)
        throws ClassNotFoundException
    {
        String implName;
        if (feDesc.getResourceSpecifier() instanceof CustomResourceSpecifier) {
            implName = ((CustomResourceSpecifier) feDesc.getResourceSpecifier())
                    .getResourceClassName();
        }
        else {
            implName = feDesc.getImplementationName();
        }

        Class<?> feClass = Class.forName(implName);
        return feClass;
    }

    private AnalysisEngineDescription injectContextMetaCollector(TaskContext aContext)
        throws ResourceInitializationException
    {
        try {
            if (featureMode.equals(FM_UNIT)) {
                // add additional unit context meta collector that extracts the context around text
                // classification units
                // mainly used for error analysis purposes
                Map<String, Object> empty = new HashMap<>();
                MetaCollectorConfiguration conf = new MetaCollectorConfiguration(
                        UnitContextMetaCollector.class, empty).addStorageMapping(
                                UnitContextMetaCollector.PARAM_CONTEXT_FILE, null,
                                UnitContextMetaCollector.CONTEXT_KEY);
                configureStorageLocations(aContext, conf.descriptor, null, conf.collectorOverrides,
                        AccessMode.READWRITE);

                return conf.descriptor;
            }

            if (featureMode.equals(FM_SEQUENCE)) {
                Map<String, Object> empty = new HashMap<>();
                MetaCollectorConfiguration conf = new MetaCollectorConfiguration(
                        SequenceContextMetaCollector.class, empty).addStorageMapping(
                                SequenceContextMetaCollector.PARAM_CONTEXT_FILE, null,
                                SequenceContextMetaCollector.CONTEXT_KEY);

                configureStorageLocations(aContext, conf.descriptor, null, conf.collectorOverrides,
                        AccessMode.READWRITE);
                return conf.descriptor;
            }
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        return null;
    }

    private void validateUniqueFeatureExtractorNames(Map<String, Object> parameterSettings)
    {
        String name = (String) parameterSettings
                .get(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME);
        if (featureExtractorNames.contains(name)) {
            throw new IllegalArgumentException("The feature extractor name [" + name
                    + "] has been used before. The name has to be unique among all used feature extractors.");
        }
        featureExtractorNames.add(name);
    }

    public static void configureStorageLocations(TaskContext aContext, ResourceSpecifier aDesc,
            String aExtractorName, Map<String, String> aOverrides, AccessMode aMode)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        // We assume for the moment that we only have primitive analysis engines for meta
        // collection, not aggregates. If there were aggregates, we'd have to do this
        // recursively
        if (aDesc instanceof AnalysisEngineDescription) {
            // Analysis engines are ok
            if (!((AnalysisEngineDescription) aDesc).isPrimitive()) {
                throw new IllegalArgumentException(
                        "Only primitive meta collectors currently supported.");
            }
        }
        else if (aDesc instanceof CustomResourceSpecifier_impl) {
            // Feature extractors are ok
        }
        else {
            throw new IllegalArgumentException(
                    "Descriptors of type " + aDesc.getClass() + " not supported.");
        }

        for (Entry<String, String> e : aOverrides.entrySet()) {
            if (aExtractorName != null) {
                // We generate a storage location from the feature extractor discriminator value
                // and the preferred value specified by the meta collector
                String parameterName = e.getKey();
                ConfigurationParameterFactory.setParameter(aDesc, parameterName,
                        new File(aContext.getFolder(MetaInfoTask.META_KEY, aMode), e.getValue())
                                .getAbsolutePath());
            }
            else {
                // If there is no associated feature extractor, then just use the preferred name
                ConfigurationParameterFactory.setParameter(aDesc, e.getKey(),
                        new File(aContext.getFolder(MetaInfoTask.META_KEY, aMode), e.getValue())
                                .getAbsolutePath());
            }
        }
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

}