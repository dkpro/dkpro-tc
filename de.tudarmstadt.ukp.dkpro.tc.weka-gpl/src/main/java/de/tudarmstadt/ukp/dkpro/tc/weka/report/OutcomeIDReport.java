package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 * 
 * @author zesch
 * 
 */
public class OutcomeIDReport
    extends ReportBase
{
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);
        File arff = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);
        Instances predictions = TaskUtils.getInstances(arff, TestTask.MULTILABEL);
        Properties props = generateProperties(predictions, TestTask.MULTILABEL);
        getContext().storeBinary(ID_OUTCOME_KEY, new PropertiesAdapter(props));
    }

    public static Properties generateProperties(Instances predictions, boolean isMultilabel)
    {
        Properties props = new Properties();
        String[] classValues = new String[predictions.numClasses()];

        for (int i = 0; i < predictions.numClasses(); i++) {
            classValues[i] = predictions.classAttribute().value(i);
        }

        int attOffset = predictions.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index();

        for (Instance inst : predictions) {
            if (isMultilabel) {
                List<String> predictionOutcomes = new ArrayList<String>();
                List<String> goldOutcomes = new ArrayList<String>();
                for (int i = 0; i < predictions.classIndex(); i++) {
                    if (inst.value(predictions.attribute(i)) == 1.) {
                        goldOutcomes.add(predictions.attribute(i).name());
                    }
                    if (inst.value(predictions.attribute(i + predictions.classIndex())) == 1.) {
                        predictionOutcomes.add(predictions.attribute(i).name());
                    }
                }
                String s = (StringUtils.join(predictionOutcomes, ",") + " (is "
                        + StringUtils.join(goldOutcomes, ",") + ")");
                props.setProperty(inst.stringValue(attOffset), s);
            }
            else {
                Double gold;

                try {
                    gold = new Double(inst.value(predictions
                            .attribute(Constants.CLASS_ATTRIBUTE_NAME
                                    + WekaUtils.COMPATIBLE_OUTCOME_CLASS)));
                }
                catch (NullPointerException e) {
                    // if train and test data have not been balanced
                    gold = new Double(inst.value(predictions
                            .attribute(Constants.CLASS_ATTRIBUTE_NAME)));
                }
                Attribute gsAtt = predictions.attribute(TestTask.PREDICTION_CLASS_LABEL_NAME);
                Double prediction = new Double(inst.value(gsAtt));
                try {
                    props.setProperty(
                            inst.stringValue(attOffset),
                            gsAtt.value(prediction.intValue()) + " (is "
                                    + classValues[gold.intValue()] + ")");
                }
                catch (Exception e) {
                    // if the outcome is numeric
                    props.setProperty(inst.stringValue(attOffset), prediction + " (is " + gold
                            + ")");
                }
            }
        }
        return props;
    }
}