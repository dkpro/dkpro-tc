/*******************************************************************************
 * Copyright 2015
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.SequenceContextMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.UnitContextMetaCollector;

/**
 * Iterates over all test documents and stores the context and the actual text
 * for each TC unit. Creates an output file with the instance ID and context.
 * This task is required for the detailed classification report to work.
 * 
 */
public class TestContextMetaTask
    extends AbstractMetaInfoTask
{

    /**
     * Public name of the folder where meta information will be stored within the task
     */
    public static final String INPUT_KEY = "input";

    @Discriminator
    protected List<String> featureSet;
    
    @Discriminator
    private String featureMode;

    @Discriminator
    private File filesRoot;

    @Discriminator
    private Collection<String> files_test;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null || files_test == null) {
            File root = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // CV setup: filesRoot and files_test have to be set as dimension
        else {
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files_test);
        }
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // check for error conditions
        if (featureSet == null) {
            throw new ResourceInitializationException(new TextClassificationException(
                    "No feature extractors have been added to the experiment."));
        }
        
        // Init
        if(metaCollectorClasses == null)
        	metaCollectorClasses = new HashSet<Class<? extends MetaCollector>>();
        
        // Use ContextMetaCollector as the only meta collector for this task
        // TODO MW: We probably don't need the whole builder & metaCollectorClasses thing.
        //          Simply create an engine with the ContextMetaCollector as the sole component.
        if (featureMode.equals(Constants.FM_UNIT)) {
            // add additional unit context meta collector that extracts the context around text classification units
            // mainly used for error analysis purposes
            metaCollectorClasses.add(UnitContextMetaCollector.class);
        }
        else if (featureMode.equals(Constants.FM_SEQUENCE)) {
            metaCollectorClasses.add(SequenceContextMetaCollector.class);
        }

        List<Object> parameters = setParameters(aContext);

        AggregateBuilder builder = getAnalysisEngineBuilder(parameters);
        
        AnalysisEngineDescription aggregateAnalysisDescription = builder.createAggregateDescription();
        
        return aggregateAnalysisDescription;
    }

	/**
     * @param operativeViews
     */
    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }
}