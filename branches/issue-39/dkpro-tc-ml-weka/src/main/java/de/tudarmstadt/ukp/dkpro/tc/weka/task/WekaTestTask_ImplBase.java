/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.List;

import meka.classifiers.multilabel.MultilabelClassifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Writes the model
 * 
 */
public abstract class WekaTestTask_ImplBase
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> classificationArguments;
    @Discriminator
    protected List<String> featureSearcher;
    @Discriminator
    protected List<String> attributeEvaluator;
    @Discriminator
    protected String labelTransformationMethod;
    @Discriminator
    protected int numLabelsToKeep;
    @Discriminator
    protected boolean applySelection;
    @Discriminator
    protected String featureMode;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected String learningMode;
    @Discriminator
    protected String threshold;
    @Discriminator
    protected List<String> baselineClassificationArgs;
    @Discriminator
    protected List<String> baselineFeatureSet;
    @Discriminator
    protected List<Object> baselinePipelineParams;
    
    protected Classifier getClassifier()
    		throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        Classifier cl;
    	if (multiLabel) {
            List<String> mlArgs = classificationArguments.subList(1, classificationArguments.size());
            cl = AbstractClassifier.forName(classificationArguments.get(0), new String[] {});
            ((MultilabelClassifier) cl).setOptions(mlArgs.toArray(new String[0]));
        }
        else {
            cl = AbstractClassifier.forName(classificationArguments.get(0), classificationArguments
                    .subList(1, classificationArguments.size()).toArray(new String[0]));
        }
    	return cl;
    }
    
    protected void featureSelection(TaskContext aContext, Instances trainData) {
    
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        // FEATURE SELECTION
        if (!multiLabel && featureSearcher != null && attributeEvaluator != null) {
            try {
                AttributeSelection selector = WekaUtils.singleLabelAttributeSelection(trainData,
                        featureSearcher, attributeEvaluator);
                // Write the results of attribute selection
                FileUtils.writeStringToFile(
                        new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                                AccessMode.READWRITE)
                                .getAbsolutePath()
                                + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureSelectionFile)),
                        selector.toResultsString());
                if (applySelection) {
                    trainData = selector.reduceDimensionality(trainData);
                }
            }
            catch (Exception e) {
                LogFactory.getLog(getClass()).warn("Could not apply feature selection.", e);
            }
        }
        if (multiLabel && attributeEvaluator != null && labelTransformationMethod != null
                && numLabelsToKeep != 0) {
            try {
                // file to hold the results of attribute selection
                File fsResultsFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                        AccessMode.READWRITE).getAbsolutePath()
                        + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureSelectionFile));
                // filter for reducing dimension of attributes
                Remove removeFilter = WekaUtils.multiLabelAttributeSelection(trainData,
                        labelTransformationMethod, attributeEvaluator, numLabelsToKeep,
                        fsResultsFile);
                if (removeFilter != null && applySelection) {
                    trainData = WekaUtils.applyAttributeSelectionFilter(trainData, removeFilter);
                }
            }
            catch (Exception e) {
                LogFactory.getLog(getClass()).warn(
                        "Could not apply multi-label feature selection.", e);
            }
        }
    }
}