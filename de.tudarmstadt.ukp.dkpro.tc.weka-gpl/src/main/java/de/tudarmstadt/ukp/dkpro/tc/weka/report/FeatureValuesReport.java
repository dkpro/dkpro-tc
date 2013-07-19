package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Result;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Reports a table of average feature (attribute) values for a typical (averaged) instance in each
 * outcome group (e.g. positives and negatives for binary classification, more lines for multiclass)
 * First line lists feature names, next lines class labels and class averages for each feature.
 * Currently implemented for numeric attributes/features only.
 */
public class FeatureValuesReport
    extends ReportBase
{
    public static final String ID_OUTCOME_KEY = "featurevalues.csv";

    // FIXME instead of iterating the data various time (takes long for large datasets), maybe a
    // matrix or map should be created while iterating only once

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);
        Properties props = new Properties();
        File arff = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);
        Instances predictions = TaskUtils.getInstances(arff, TestTask.MULTILABEL);
        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_KEY);
        String[] classValues;
        List<String> attrNames = new ArrayList<String>();
        List<String[]> attrClassAverages = new ArrayList<String[]>();

        // -----MULTI LABEL-----------
        if (TestTask.MULTILABEL) {
            Result r = Result.readResultFromFile(evaluationFile.getAbsolutePath());
            classValues = new String[predictions.classIndex()];
            for (int i = 0; i < predictions.classIndex(); i++) {
                classValues[i] = predictions.attribute(i).name()
                        .split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
            }
            String threshold = r.getInfo("Threshold");
            double[] t = TaskUtils.getMekaThreshold(threshold, r, predictions);

            // loop over labels
            for (int classindex = 0; classindex < predictions.classIndex(); classindex++) {
                // attribute average val for each label
                String[] attrAverages = new String[predictions.numAttributes()
                        - (predictions.classIndex() * 2)];

                // loop over attributes (jumping outcome and predicted labels)
                for (int attrIndex = predictions.classIndex() * 2; attrIndex < predictions
                        .numAttributes(); attrIndex++) {
                    Attribute att = predictions.attribute(attrIndex);

                    // only numeric attributes
                    if (att.isNumeric()) {
                        // add att names only once
                        if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                        double sumOverSelectedInst = 0.0;
                        int numInstances = 0;
                        for (Instance inst : predictions) {
                            // outcome class
                            if (t[classindex] <= inst.value(classindex)) {
                                double val = inst.value(att);
                                if (val != 0 && val != Double.NaN) {
                                    sumOverSelectedInst += val;
                                }
                                numInstances++;
                            }
                        }
                        // sumOverSelectedInst here contains sum of one attribute per in-class
                        // instances
                        if (numInstances > 0) {
                            attrAverages[att.index() - predictions.classIndex() * 2] = ((Double) (sumOverSelectedInst / numInstances))
                                    .toString();
                        }
                        else {
                            attrAverages[att.index() - predictions.classIndex() * 2] = "0.0";
                        }
                    }
                }
                attrClassAverages.add(attrAverages);
            }
            // FIXME transpose table
            props.setProperty("class_values", StringUtils.join(attrNames, ","));// column titles
            for (int classindex = 0; classindex < predictions
                    .classIndex(); classindex++) {
                props.setProperty(classValues[classindex],
                        StringUtils.join(attrClassAverages.get(classindex), ","));
            }
        }
        // -----SINGLE LABEL-----------
        else {
            classValues = new String[predictions.numClasses()];
            for (int i = 0; i < predictions.numClasses(); i++) {
                classValues[i] = predictions.classAttribute().value(i); // distinct outcome classes
            }
            // loops twice for binary class etc.
            for (int classindex = 0; classindex < predictions.numClasses(); classindex++) {
                // average value for each attribute in one outcome class
                String[] attrAverages = new String[predictions.numAttributes()];
                for (int attrIndex = 0; attrIndex < predictions.numAttributes(); attrIndex++) {
                    Attribute att = predictions.attribute(attrIndex);
                    if (att.isNumeric()) {
                        // add att names only once
                        if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                        double sumOverSelectedInst = 0.0;
                        int numInstances = 0;
                        for (Instance inst : predictions) {
                            // outcome class number
                            int classification = new Double(inst.value(predictions
                                    .attribute(Constants.CLASS_ATTRIBUTE_NAME
                                            + WekaUtils.COMPATIBLE_OUTCOME_CLASS))).intValue();
                            if (classification == classindex) { // positive or negative for binary
                                double val = inst.value(att);
                                if (val != 0 && val != Double.NaN) {
                                    sumOverSelectedInst += val;
                                }
                                numInstances++;
                            }
                        }
                        // sumOverSelectedInst here contains sum of one attribute per in-class
                        // instances
                        if (numInstances > 0) {
                            attrAverages[att.index()] = ((Double) (sumOverSelectedInst / numInstances))
                                    .toString();
                        }
                        else {
                            attrAverages[att.index()] = "0.0";
                        }
                    }
                }
                attrClassAverages.add(attrAverages);
            }
            // FIXME transpose table
            props.setProperty("class_values", StringUtils.join(attrNames, ","));// column titles
            for (int classindex = 0; classindex < predictions.numClasses(); classindex++) {
                props.setProperty(classValues[classindex],
                        StringUtils.join(attrClassAverages.get(classindex), ","));
            }
        }

        getContext().storeBinary(ID_OUTCOME_KEY, new PropertiesAdapter(props));
    }

}