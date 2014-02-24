package de.tudarmstadt.ukp.dkpro.tc.weka.task;


import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;

/**
 * Crossvalidation setup
 * 
 * @author daxenberger
 * @author zesch
 * @author jamison
 * 
 */
public class BatchTaskCrossValidationWithFoldControl
    extends BatchTaskCrossValidation
{

    protected Comparator<String> comparator;//EJ

    public BatchTaskCrossValidationWithFoldControl()
    {/* needed for Groovy */
    }

    /**
     * EJ added
     * @param aExperimentName
     * @param aAggregate
     * @param aNumFolds
     * @param comparator
     */
    public BatchTaskCrossValidationWithFoldControl(String aExperimentName, AnalysisEngineDescription aAggregate,
            int aNumFolds, Comparator<String> aComparator)
    {
        setExperimentName(aExperimentName);
        setAggregate(aAggregate);
        setNumFolds(aNumFolds);
        setComparator(aComparator);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }
 
    @Override
    protected FoldDimensionBundle<String> getFoldDim(String[] fileNames) {
    	return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), numFolds, comparator);
    }
  
    public void setComparator(Comparator<String> aComparator)
    {
    	this.comparator = aComparator;
    }
}