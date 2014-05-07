package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    /**
     * Name of the file where the results of feature value analysis are stored
     */
    public static final String FEATURE_VALUE_KEY = "featureValues.csv";

    // FIXME instead of iterating the data various time (takes long for large datasets), maybe a
    // matrix or map should be created while iterating only once

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);
        boolean multiLabel = getDiscriminators().get(TestTask.class.getName() + "|learningMode")
                .equals(Constants.LM_MULTI_LABEL);
        Properties props = new Properties();
        File arff = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);
        Instances predictions = TaskUtils.getInstances(arff, multiLabel);
        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_KEY);
        String[] classValues;
        List<String> attrNames = new ArrayList<String>();

        Map<PairKey<Integer, Integer>, ArrayList<Double>> map = new HashMap<PairKey<Integer, Integer>, ArrayList<Double>>();
        // hashmap storing an array list of values per attribute per class label

        PairKey<Integer, Integer> pk;
        // to hold a temporary pair key

        int predictionsClassIndex = predictions.classIndex();
        int predictionsNumAttributes = predictions.numAttributes();

        // -----MULTI LABEL-----------
        if (multiLabel) {
            Result r = Result.readResultFromFile(evaluationFile.getAbsolutePath());
            classValues = new String[predictions.classIndex()];
            for (int i = 0; i < predictions.classIndex(); i++) {
                classValues[i] = predictions.attribute(i).name()
                        .split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
            }
            String threshold = r.getInfo("Threshold");
            double[] t = TaskUtils.getMekaThreshold(threshold, r, predictions);

            // iterate over instances
            for (Instance inst : predictions) {
                // iterate over attributes
                for (int attrIndex = predictionsClassIndex * 2; attrIndex < predictionsNumAttributes; attrIndex++) {
                    Attribute att = predictions.attribute(attrIndex);
                    // only numeric attributes involved in average calculation
                    if (att.isNumeric()) {
                        if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                        // iterate over class labels
                        for (int classindex = 0; classindex < predictionsClassIndex; classindex++) {
                            pk = new PairKey<Integer, Integer>(att.index() - predictionsClassIndex
                                    * 2, classindex);
                            // check if label confidence is above threshold
                            if (t[classindex] <= inst.value(classindex)) {
                                if (!map.containsKey(pk)) {
                                    map.put(pk, new ArrayList<Double>());
                                }
                                ArrayList<Double> existing = map.get(pk);
                                existing.add(inst.value(att));
                                map.put(pk, existing);
                            }
                            // ensuring that a numeric attribute and class label pair always have a
                            // key (even if empty)
                            else if (!map.containsKey(pk)) {
                                map.put(pk, new ArrayList<Double>());
                            }
                        }
                    }
                }
            }
            // FIXME transpose table
            props.setProperty("class_values", StringUtils.join(attrNames, ","));// column titles
            for (int classindex = 0; classindex < predictionsClassIndex; classindex++) {
                String str = "";
                for (int i = predictionsClassIndex * 2; i < predictionsNumAttributes; i++, str += ",") {
                    Attribute att = predictions.attribute(i);
                    pk = new PairKey<Integer, Integer>(att.index() - predictionsClassIndex * 2,
                            classindex);
                    if (map.containsKey(pk)) {
                        Iterator<Double> it = map.get(pk).iterator();
                        double sum = 0.0;
                        int count = 0;
                        if (!it.hasNext()) {
                            count = 1;
                        }
                        while (it.hasNext()) {
                            sum += it.next();
                            count++;
                        }
                        str = str + (new Double(sum / count)).toString();
                    }
                }
                str = str.substring(0, str.length() - 1);
                props.setProperty(classValues[classindex], str);
            }
        }
        // -----SINGLE LABEL-----------
        else {
            classValues = new String[predictions.numClasses()];
            for (int i = 0; i < predictions.numClasses(); i++) {
                classValues[i] = predictions.classAttribute().value(i); // distinct outcome classes
            }

            // iterate over instances
            for (Instance inst : predictions) {
                // iterate over attributes
                for (int attrIndex = 0; attrIndex < predictionsNumAttributes; attrIndex++) {
                    Attribute att = predictions.attribute(attrIndex);

                    int classification;
                    try {
                        classification = new Double(inst.value(predictions
                                .attribute(Constants.CLASS_ATTRIBUTE_NAME
                                        + WekaUtils.COMPATIBLE_OUTCOME_CLASS))).intValue();
                    }
                    catch (NullPointerException e) {
                        // if train and test data have not been balanced
                        classification = new Double(inst.value(predictions
                                .attribute(Constants.CLASS_ATTRIBUTE_NAME))).intValue();
                    }

                    // only numeric attributes involved in average calculation
                    if (att.isNumeric()) {
                        if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                        pk = new PairKey<Integer, Integer>(attrIndex, classification);
                        if (!map.containsKey(pk)) {
                            map.put(pk, new ArrayList<Double>());
                        }
                        ArrayList<Double> existing = map.get(pk);
                        existing.add(inst.value(att));
                        map.put(pk, existing);
                    }
                }
            }
        }
        // create table
        List<List<String>> finalTable = new ArrayList<List<String>>();
        String classValuesString = "";
        for (String className : classValues) {
            classValuesString = classValuesString + "," + className;
        }
        props.setProperty("class_values,", classValuesString.substring(1));
        for (int classindex = 0; classindex < predictions.numClasses(); classindex++) {
            List<String> aLine = new ArrayList<String>();

            for (int i = 0; i < predictionsNumAttributes; i++) {
                pk = new PairKey<Integer, Integer>(i, classindex);
                if (map.containsKey(pk)) {
                    Iterator<Double> it = map.get(pk).iterator();
                    double sum = 0.0;
                    int count = 0;
                    if (!it.hasNext()) {
                        count = 1;
                    }
                    while (it.hasNext()) {
                        sum += it.next();
                        count++;
                    }
                    String field = (new Double(sum / count)).toString();
                    if (!field.isEmpty()) {
                        aLine.add(field);
                    }
                }
            }
            if (aLine.size() > 0) {
                finalTable.add(aLine);
            }

        }
        // transposed for easier reading
        for (int newRow = 0; newRow < finalTable.get(0).size(); newRow++) { // like, 2000
            String newLine = "";
            for (int newColumn = 0; newColumn < finalTable.size(); newColumn++) { // like, 3
                newLine = newLine + "," + finalTable.get(newColumn).get(newRow);
            }
            if (!newLine.isEmpty()) {
                props.setProperty(attrNames.get(newRow), newLine);
            }
        }

        getContext().storeBinary(FEATURE_VALUE_KEY, new PropertiesAdapter(props));
    }

}

// FIXME consider adding class as public to a separate file under a relevant package

class PairKey<A, B>
{
    public final A a;
    public final B b;

    PairKey(A a, B b)
    {
        this.a = a;
        this.b = b;
    }

    public static <A, B> PairKey<A, B> make(A a, B b)
    {
        return new PairKey<A, B>(a, b);
    }

    @Override
    public int hashCode()
    {
        return (a != null ? a.hashCode() : 0) + 31 * (b != null ? b.hashCode() : 0);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        PairKey that = (PairKey) o;
        return (a == null ? that.a == null : a.equals(that.a))
                && (b == null ? that.b == null : b.equals(that.b));
    }
}