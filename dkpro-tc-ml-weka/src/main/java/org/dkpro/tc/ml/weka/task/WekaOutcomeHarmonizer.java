/**
 * Copyright 2019
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
package org.dkpro.tc.ml.weka.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dkpro.tc.core.Constants;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

public class WekaOutcomeHarmonizer
{
    private Instances trainData;
    private Instances testData;
    private boolean multiLabel;
    private boolean isRegression;

    /**
     * Suffix for class label names in the test data that have been adapted to match the training
     * data
     *
     */
    public static final String COMPATIBLE_OUTCOME_CLASS = "_Comp";

    public WekaOutcomeHarmonizer(Instances train, Instances test, String learningMode)
    {
        this.trainData = train;
        this.testData = test;
        this.multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        this.isRegression = learningMode.equals(Constants.LM_REGRESSION);
    }

    public Instances harmonize() throws Exception
    {
        if(isRegression) {
            return testData;
        }
    

        // ================ SINGLE LABEL BRANCH ======================
        if (!multiLabel) {
            return harmonizeSingleLabel();
        }
        else {
            return harmonizeMultiLabel();
        }
    }

    private Instances harmonizeMultiLabel() throws Exception
    {
        Instances compTestData = null;
        
        int numTrainLabels = trainData.classIndex();
        int numTestLabels = testData.classIndex();

        List<String> trainLabels = getLabels(trainData);

        // add new outcome class attributes to test data

        Add filter = new Add();
        for (int i = 0; i < numTrainLabels; i++) {
            // numTestLabels +i (because index starts from 0)
            filter.setAttributeIndex(Integer.toString(numTestLabels + i + 1));
            filter.setNominalLabels("0,1");
            filter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
            filter.setInputFormat(testData);
            testData = Filter.useFilter(testData, filter);
        }

        // fill NEW test data with values from old test data plus the new class attributes
        compTestData = new Instances(testData, testData.numInstances());
        for (int i = 0; i < testData.numInstances(); i++) {
            weka.core.Instance instance = testData.instance(i);
            // fullfill with 0.
            for (int j = 0; j < numTrainLabels; j++) {
                instance.setValue(j + numTestLabels, 0.);
            }
            // fill the real values:
            for (int j = 0; j < numTestLabels; j++) {
                // part of train data: forget labels which are not part of the train data
                if (trainLabels.indexOf(instance.attribute(j).name()) != -1) {
                    // class label found in test data
                    int index = trainLabels.indexOf(instance.attribute(j).name());
                    instance.setValue(index + numTestLabels, instance.value(j));
                }
            }
            compTestData.add(instance);
        }

        // remove old class attributes
        for (int i = 0; i < numTestLabels; i++) {
            Remove remove = new Remove();
            remove.setAttributeIndices("1");
            remove.setInvertSelection(false);
            remove.setInputFormat(compTestData);
            compTestData = Filter.useFilter(compTestData, remove);
        }

        // adapt header and set new class label
        String relationTag = compTestData.relationName();
        compTestData.setRelationName(relationTag.substring(0, relationTag.indexOf("-C") + 2)
                + " " + numTrainLabels + " ");
        compTestData.setClassIndex(numTrainLabels);
        
        return compTestData;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Instances harmonizeSingleLabel() throws Exception
    {
        Instances compTestData = null;

        // retrieve class labels
        Enumeration trainOutcomeValues = trainData.classAttribute().enumerateValues();
        Enumeration testOutcomeValues = testData.classAttribute().enumerateValues();
        List trainLabels = Collections.list(trainOutcomeValues);
        List testLabels = Collections.list(testOutcomeValues);

        // add new outcome class attribute to test data
        Add addFilter = new Add();
        addFilter.setNominalLabels(StringUtils.join(trainLabels, ','));
        addFilter.setAttributeName(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS);
        addFilter.setInputFormat(testData);
        testData = Filter.useFilter(testData, addFilter);

        // fill NEW test data with values from old test data plus the new class attribute
        compTestData = new Instances(testData, testData.numInstances());
        for (int i = 0; i < testData.numInstances(); i++) {
            weka.core.Instance instance = testData.instance(i);
            String label = (String) testLabels
                    .get((int) instance.value(testData.classAttribute()));
            if (trainLabels.indexOf(label) != -1) {
                instance.setValue(
                        testData.attribute(
                                Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS),
                        label);
            }
            else {
                instance.setMissing(testData.classIndex());
            }
            compTestData.add(instance);
        }

        // remove old class attribute
        Remove remove = new Remove();
        remove.setAttributeIndices(Integer
                .toString(compTestData.attribute(Constants.CLASS_ATTRIBUTE_NAME).index() + 1));
        remove.setInvertSelection(false);
        remove.setInputFormat(compTestData);
        compTestData = Filter.useFilter(compTestData, remove);

        // set new class attribute
        compTestData.setClass(compTestData
                .attribute(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS));
        return compTestData;
    }

    private static ArrayList<String> getLabels(Instances data)
    {
        int numLabels = data.classIndex();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < numLabels; i++) {
            list.add(data.attribute(i).name());
        }
        return list;
    }
}
