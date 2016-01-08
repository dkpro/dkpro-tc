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
package de.tudarmstadt.ukp.dkpro.tc.ml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DefaultBatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;

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
    protected TCMachineLearningAdapter mlAdapter;
    protected boolean dropInvalidCases;

    Log log = LogFactory.getLog(Experiment_ImplBase.class);

    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);

        try {
            if (getPreprocessing() == null) {
                log.warn("Preprocessing engine has not been set, will use "
                        + NoOpAnnotator.class.getSimpleName() + " as default");
                preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
            }
        }
        catch (ResourceInitializationException e) {
            throw new RuntimeException(e);
        }

        init();
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

    public void setMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
        throws IllegalArgumentException
    {
        try {
            this.mlAdapter = mlAdapter.newInstance();
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
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

    public void setDropInvalidCases(boolean dropInvalidCases)
    {
        this.dropInvalidCases = dropInvalidCases;
    }
}