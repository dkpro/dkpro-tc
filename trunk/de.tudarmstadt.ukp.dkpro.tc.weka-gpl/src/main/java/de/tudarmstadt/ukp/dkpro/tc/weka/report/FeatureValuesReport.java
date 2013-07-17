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
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/*
 * Currently implemented for SINGLELABEL only.
 * Reports a table of average feature (attribute) values 
 * for a typical (averaged) instance in each outcome group
 * (e.g. positives and negatives for binary classification, more lines for multiclass)
 * First line lists feature names, next lines class labels and class averages for each feature.
 * 
 * */
public class FeatureValuesReport
extends ReportBase
{
	public static final String ID_OUTCOME_KEY = "featurevalues.csv";

	@Override
	public void execute()
			throws Exception
			{

		File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);

		Properties props = new Properties();

		File arff = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);

		Instances predictions = TaskUtils.getInstances(arff, TestTask.MULTILABEL);

		String[] classValues = new String[predictions.numClasses()];

		if (TestTask.MULTILABEL) {
			//TODO: Write multilabel version
			
			/*
			for (Instance inst : predictions) {
				List<String> predictionOutcomes = new ArrayList<String>();
				for (int i = 0; i < predictions.classIndex(); i++) {
					if (inst.value(predictions.attribute(i)) == 1.) {
						predictionOutcomes.add(predictions.attribute(i).name());
					}
				}
			}
			 
			...
			
			*/
		}
		else {
			//-----SINGLE LABEL-----------	
			for (int i = 0; i < predictions.numClasses(); i++) {
				classValues[i] = predictions.classAttribute().value(i); //distinct outcome classes
			}
			List<String[]> attrClassAverages = new ArrayList<String[]>();
			List<String> attrNames = new ArrayList<String>();
			for (int classindex = 0; classindex < predictions.numClasses(); classindex++) { //loops twice for binary class. etc.
				String[] attrAverages = new String[predictions.numAttributes()]; //average value for each attribute in one outcome class 
				for	(int attrIndex = 0; attrIndex < predictions.numAttributes(); attrIndex++) {
					Attribute att = predictions.attribute(attrIndex);
					if (att.isNumeric()) {
						attrNames.add(att.name());
						double sumOverSelectedInst = 0.0;
						int numInstances = 0;
						for (Instance inst : predictions) {
							int classification = new Double(inst.value(predictions //outcome class number
									.attribute(Constants.CLASS_ATTRIBUTE_NAME
											+ WekaUtils.COMPATIBLE_OUTCOME_CLASS))).intValue();
							if (classification == classindex) { //positive or negative for binary
								double val = inst.value(att);
								if (val!=0 && val!=Double.NaN) { sumOverSelectedInst += val; }
								numInstances++;
							}
						} 
						//sumOverSelectedInst here contains sum of one attribute per in-class instances
						if (numInstances>0) {
							attrAverages[att.index()] = ((Double)(sumOverSelectedInst/numInstances)).toString();			
						}
						else {
							attrAverages[att.index()] = "0.0";
						}
					}					
				}
				attrClassAverages.add(attrAverages);
			}
			props.setProperty("class_values", StringUtils.join(attrNames, ","));//column titles
			for (int classindex = 0; classindex < predictions.numClasses(); classindex++) {
				props.setProperty(classValues[classindex], StringUtils.join(attrClassAverages.get(classindex), ","));
			}

			getContext().storeBinary(ID_OUTCOME_KEY, new PropertiesAdapter(props));
		}
	}

}