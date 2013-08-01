package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

        Map<Integer,HashMap<Integer,Double>> map =new HashMap<Integer,HashMap<Integer,Double>>(); // hashmap storing sum over attribute values for every class label
        Map<Integer,HashMap<Integer,Integer>> countMap =new HashMap<Integer,HashMap<Integer,Integer>>(); // hashmap storing counts of attribute values for every class label
        
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
            
            //iterate over instances
            for (Instance inst : predictions) {
            	//iterate over attributes
            	for (int attrIndex = predictions.classIndex() * 2; attrIndex < predictions.numAttributes(); attrIndex++) {
            		Attribute att = predictions.attribute(attrIndex);
                    //only numeric attributes involved in average calculation
            		if (att.isNumeric()) {
                    	if(map.get(att.index() - predictions.classIndex() * 2) == null) {
                    		map.put(att.index() - predictions.classIndex() * 2, new HashMap<Integer,Double>());
                    		countMap.put(att.index() - predictions.classIndex() * 2, new HashMap<Integer,Integer>());
                    	}
                    	if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                    	//iterate over class labels
                    	for (int classindex = 0; classindex < predictions.classIndex(); classindex++) {
                    		//check if label confidence is above threshold
                    		if (t[classindex] <= inst.value(classindex)) {
                    			if (map.get(att.index() - predictions.classIndex() * 2).get(classindex) == null) {
                            		map.get(att.index() - predictions.classIndex() * 2).put(classindex,inst.value(att));
                            		countMap.get(att.index() - predictions.classIndex() * 2).put(classindex, 1);
                            	}
                            	else {
                            		map.get(att.index() - predictions.classIndex() * 2).put(classindex, map.get(att.index() - predictions.classIndex() * 2).get(classindex) + inst.value(att));
                            		countMap.get(att.index() - predictions.classIndex() * 2).put(classindex, countMap.get(att.index() - predictions.classIndex() * 2).get(classindex) + 1);
                            	}
                    		}
                    		//ensuring that the hashmaps don't go uninitialized in case no label confidence is above threshold
                    		else if (map.get(att.index() - predictions.classIndex() * 2).get(classindex) == null) {
                    			map.get(att.index() - predictions.classIndex() * 2).put(classindex, 0.0);
                        		countMap.get(att.index() - predictions.classIndex() * 2).put(classindex, 0);
                    		}
                    	}
                    }
            	}
            }
            // FIXME transpose table
            props.setProperty("class_values", StringUtils.join(attrNames, ","));// column titles
            for (int classindex = 0; classindex < predictions.classIndex(); classindex++) {
            	String str = "";
            	for (int i=predictions.classIndex() * 2; i<predictions.numAttributes(); i++,str += ",") {
            		Attribute att = predictions.attribute(i);
            		if(att.isNumeric()) {
            			int count = countMap.get(att.index() - predictions.classIndex() * 2).get(classindex);
            			if (count == 0) {
            				str = str + (new Double(0.0)).toString();
            			}
            			else {
            				str = str + (new Double(map.get(att.index() - predictions.classIndex() * 2).get(classindex)/count)).toString();
            			}
            		}
            	}
            	str = str.substring(0, str.length() - 1);
            	props.setProperty(classValues[classindex],str);
            }           
        }
        // -----SINGLE LABEL-----------
        else {
            classValues = new String[predictions.numClasses()];
            for (int i = 0; i < predictions.numClasses(); i++) {
                classValues[i] = predictions.classAttribute().value(i); // distinct outcome classes
            }
            
            for (int attrIndex = 0; attrIndex < predictions.numAttributes(); attrIndex++) {
        		map.put(attrIndex, new HashMap<Integer,Double>());
        		countMap.put(attrIndex, new HashMap<Integer,Integer>());
        	}
            
            //iterate over instances
            for (Instance inst : predictions) {
            	//iterate over attributes
            	for (int attrIndex = 0; attrIndex < predictions.numAttributes(); attrIndex++) {
            		Attribute att = predictions.attribute(attrIndex);
            		int classification = new Double(inst.value(predictions
                            .attribute(Constants.CLASS_ATTRIBUTE_NAME
                                    + WekaUtils.COMPATIBLE_OUTCOME_CLASS))).intValue();
            		//only numeric attributes involved in average calculation
            		if (att.isNumeric()) {
                    	if (!attrNames.contains(att.name())) {
                            attrNames.add(att.name());
                        }
                    	if (map.get(attrIndex).get(classification) == null) {
                    		map.get(attrIndex).put(classification,inst.value(att));
                    		countMap.get(attrIndex).put(classification, 1);
                    	}
                    	else {
                    		map.get(attrIndex).put(classification, map.get(attrIndex).get(classification) + inst.value(att));
                    		countMap.get(attrIndex).put(classification, countMap.get(attrIndex).get(classification) + 1);
                    	}
                    }
            	}
            }
            // FIXME transpose table
            props.setProperty("class_values", StringUtils.join(attrNames, ","));// column titles
            for (int classindex = 0; classindex < predictions.numClasses(); classindex++) {
            	String str = "";
            	for (int i=0; i<predictions.numAttributes(); i++,str += ",") {
            		if(!map.get(i).isEmpty()) {
            			str = str + (new Double(map.get(i).get(classindex)/countMap.get(i).get(classindex))).toString();
            		}
            	}
            	str = str.substring(0, str.length() - 1);
            	if(!str.isEmpty()) {
            		props.setProperty(classValues[classindex],str);
            	}
            }
        }
        getContext().storeBinary(ID_OUTCOME_KEY, new PropertiesAdapter(props));
    }

}