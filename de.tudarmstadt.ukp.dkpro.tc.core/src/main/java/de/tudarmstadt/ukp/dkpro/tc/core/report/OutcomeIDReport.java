package de.tudarmstadt.ukp.dkpro.tc.core.report;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.cleartk.classifier.weka.singlelabel.AbstractWekaDataWriter;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.core.util.WekaUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.meta.AddIdFeatureExtractor;

public class OutcomeIDReport
    extends ReportBase
{
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";

    @Override
    public void execute()
        throws Exception
    {

        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);

        Properties props = new Properties();

        File arff = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);

        Instances predictions = TaskUtils.getInstances(arff, TestTask.MULTILABEL);

        String[] classValues = new String[predictions.numClasses()];

        for (int i = 0; i < predictions.numClasses(); i++) {
            classValues[i] = predictions.classAttribute().value(i);
        }

        @SuppressWarnings("unchecked")
        Enumeration<Attribute> enumeration = predictions.enumerateAttributes();
        int attOffset = 0;
        while (enumeration.hasMoreElements()) {
            Attribute att = enumeration.nextElement();
            if (att.name().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
                break;
            }

            attOffset++;
        }

        for (Instance inst : predictions) {
            int classification = new Double(inst.value(predictions
                    .attribute(AbstractWekaDataWriter.classAttributeName
                            + WekaUtils.COMPATIBLE_OUTCOME_CLASS))).intValue();
            props.setProperty(inst.stringValue(attOffset), classValues[classification]);
        }

        getContext().storeBinary(ID_OUTCOME_KEY, new PropertiesAdapter(props));

    }

    // private Instances getInstances(File instancesFile)
    // throws FileNotFoundException, IOException
    // {
    // Instances data = null;
    // Reader reader = new BufferedReader(new FileReader(instancesFile));
    //
    // try {
    // data = new Instances(reader);
    // data.setClass(data.attribute("outcome"));
    // }
    // finally {
    // reader.close();
    // }
    //
    // return data;
    // }
}