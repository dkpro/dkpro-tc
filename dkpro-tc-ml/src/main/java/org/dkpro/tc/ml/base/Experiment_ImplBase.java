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
package org.dkpro.tc.ml.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.tc.ml.DiscriminableNameConverter;

/**
 * Base class for TC experiment setups
 * 
 */
public abstract class Experiment_ImplBase
    extends DefaultBatchTask
{

    protected String experimentName;
    private AnalysisEngineDescription preprocessing; // private as this might be overridden by a
                                                     // batch task to allow preprocessing as
                                                     // discriminator
    protected List<String> operativeViews;
    protected List<Class<? extends Report>> innerReports;

    Log log = LogFactory.getLog(Experiment_ImplBase.class);
    
    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);
        addConversionServiceEntries(aContext);

        try {
            if (getPreprocessing() == null) {
                log.warn("Preprocessing engine has not been set, will use ["
                        + NoOpAnnotator.class.getSimpleName() + "] as default");
                preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
            }
        }
        catch (ResourceInitializationException e) {
            throw new RuntimeException(e);
        }

        init();
    }

    private void addConversionServiceEntries(TaskContext aContext)
    {
        
        ParameterSpace pSpace = getParameterSpace();
        Dimension<?>[] dimensions = pSpace.getDimensions();
        for (Dimension<?> d : dimensions) {
            if (d.getName().equals("readers")) {
                addConversionForCollectionReader(aContext, d);
            }
        }        
    }

    private void addConversionForCollectionReader(TaskContext aContext, Dimension<?> d)
    {
        if(!d.hasNext()){
            return;
        }
        @SuppressWarnings("rawtypes")
        Map readers = (Map) d.next();
        for (Object k : readers.keySet()) {
            CollectionReaderDescription crd = (CollectionReaderDescription) readers.get(k);
            String description = DiscriminableNameConverter
                    .getCollectionReaderDescription(crd);
            aContext.getConversionService().registerDiscriminable(crd, description);
        }
        
    }

    protected abstract void init()
        throws IllegalStateException;

    public void setExperimentName(String experimentName)
    {
        boolean invalidCharsFound = experimentNameContainsInvalidCharacters(experimentName);
        if (invalidCharsFound) {
            throw new IllegalArgumentException("Your experiment name [" + experimentName
                    + "] contains illegal characters. Permitted are [a-zA-Z0-9-_]");
        }
        this.experimentName = experimentName;
    }

    private boolean experimentNameContainsInvalidCharacters(String experimentName)
    {
        String result = experimentName.replaceAll("[a-zA-Z0-9-_]+", "");
        if (result.isEmpty()) {
            return false;
        }
        return true;
    }

    public void setPreprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
    }

    public AnalysisEngineDescription getPreprocessing()
    {
        return preprocessing;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    /**
     * Adds a report for the inner test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void addInnerReport(Class<? extends Report> innerReport)
    {
        if (innerReports == null) {
            innerReports = new ArrayList<Class<? extends Report>>();
        }
        innerReports.add(innerReport);
    }

}