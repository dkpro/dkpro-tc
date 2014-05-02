package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import mulan.data.InvalidDataFormatException;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import mulan.dimensionalityReduction.BinaryRelevanceAttributeEvaluator;
import mulan.dimensionalityReduction.LabelPowersetAttributeEvaluator;
import mulan.dimensionalityReduction.Ranker;

import org.apache.commons.io.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Result;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MekaClassAttributes;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MekaEvaluationUtils;

/**
 * Utils required by Weka/Meka tasks.
 * 
 */
public class TaskUtils
{

    /**
     * Read instances from uncompressed or compressed arff files. Compression is determined by
     * filename suffix. For bz2 files, it is expected that the first two bytes mark the compression
     * types (BZ) - thus, the first bytes of the stream are skipped. <br>
     * For arff files with single-label outcome, the class attribute is expected at the end of the
     * attribute set. For arff files with multi-label outcome, the class attribute is expected at
     * the beginning of the attribute set; additionally the number of class labels must be specified
     * in the relation tag behind a "-C" argument, e.g. "-C 3".
     * 
     * @param instancesFile
     *            arff File
     * @param multiLabel
     *            whether this arff file contains single- or multi-label outcome
     * @return instances with class attribute set
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Instances getInstances(File instancesFile, boolean multiLabel)
        throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(instancesFile);
        BufferedInputStream bufStr = new BufferedInputStream(fis);

        InputStream underlyingStream = null;
        if (instancesFile.getName().endsWith(".gz")) {
            underlyingStream = new GZIPInputStream(bufStr);
        }
        else if (instancesFile.getName().endsWith(".bz2")) {
            // skip bzip2 prefix that we added manually
            fis.read();
            fis.read();
            underlyingStream = new CBZip2InputStream(bufStr);
        }
        else {
            underlyingStream = bufStr;
        }

        Reader reader = new InputStreamReader(underlyingStream, "UTF-8");
        Instances trainData = new Instances(reader);

        if (multiLabel) {
            String relationTag = trainData.relationName();
            // for multi-label classification, class labels are expected at beginning of attribute
            // set and their number must be specified with the -C parameter in the relation tag
            Matcher m = Pattern.compile("-C\\s\\d+").matcher(relationTag);
            m.find();
            trainData.setClassIndex(Integer.parseInt(m.group().split("-C ")[1]));
        }
        else {
            // for single-label classification, class label expected as last attribute
            trainData.setClassIndex(trainData.numAttributes() - 1);
        }
        reader.close();
        return trainData;
    }

    /**
     * @return The offset of the instanceId attribute within the weka instance
     */
    @SuppressWarnings("unchecked")
    public static int getInstanceIdAttributeOffset(Instances data)
    {
        int attOffset = 1;
        Enumeration<Attribute> enumeration = data.enumerateAttributes();
        while (enumeration.hasMoreElements()) {
            Attribute att = enumeration.nextElement();
            // System.out.println(att.name());
            if (att.name().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
                return attOffset;
            }
            attOffset++;
        }
        return -1;
    }

    /**
     * Returns a list with names of the class attribute values. Only works for single-label outcome.
     * 
     * @param eval
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<String> getClassLabels(Evaluation eval)
    {
        Enumeration<String> classLabels = eval.getHeader().attribute(eval.getHeader().classIndex())
                .enumerateValues();
        List<String> classLabelList = new ArrayList<String>();
        while (classLabels.hasMoreElements()) {
            classLabelList.add(classLabels.nextElement());
        }
        return classLabelList;
    }

    /**
     * Calculates the threshold to turn a ranking of label predictions into a bipartition (one
     * threshold for each label)
     * 
     * @param threshold
     *            PCut1, PCutL, or number between 0 and 1 (see Meka documentation for details on
     *            this)
     * @param r
     *            Results file
     * @param data
     *            training data to use for automatically determining the threshold
     * @return an array with thresholds for each label
     * @throws Exception
     */
    public static double[] getMekaThreshold(String threshold, Result r, Instances data)
        throws Exception
    {
        double[] t = new double[r.L];
        if (threshold.equals("PCut1")) {
            // one threshold for all labels (PCut1 in Meka)
            Arrays.fill(
                    t,
                    MekaEvaluationUtils.calibrateThreshold(r.predictions,
                            Double.valueOf(r.getValue("LCard_train"))));
        }
        else if (threshold.equals("PCutL")) {
            // one threshold for each label (PCutL in Meka)
            t = MekaEvaluationUtils.calibrateThresholds(r.predictions,
                    MLUtils.labelCardinalities(data));
            // FIXME
            throw new Exception("Not yet implemented.");
        }
        else {
            // manual threshold
            Arrays.fill(t, Double.valueOf(threshold));
        }
        return t;
    }

    /**
     * 
     * Feature selection using Weka.
     * 
     * @param trainData
     *            training data
     * @param featureSearcher
     * @param attributeEvaluator
     * @return a feature selector
     * @throws Exception
     */
    public static AttributeSelection singleLabelAttributeSelection(Instances trainData,
            List<String> featureSearcher, List<String> attributeEvaluator)
        throws Exception
    {
        AttributeSelection selector = new AttributeSelection();

        // Get feature searcher
        ASSearch search = ASSearch.forName(featureSearcher.get(0),
                featureSearcher.subList(1, featureSearcher.size()).toArray(new String[0]));
        // Get attribute evaluator
        ASEvaluation evaluation = ASEvaluation.forName(attributeEvaluator.get(0),
                attributeEvaluator.subList(1, attributeEvaluator.size()).toArray(new String[0]));

        selector.setSearch(search);
        selector.setEvaluator(evaluation);
        selector.SelectAttributes(trainData);

        return selector;
    }

    /**
     * Feature selection using Mulan.
     * 
     * @param trainData
     *            training data
     * @param labelTransformationMethod
     *            method to transform multi-label data into single-label data
     * @param attributeEvaluator
     * @param numLabelsToKeep
     *            number of labels that should be kept
     * @param featureSelectionResultsFile
     *            a file to write the evaluated attributes to
     * @return a filter to reduce the attribute dimension
     * @throws TextClassificationException
     */
    public static Remove multiLabelAttributeSelection(Instances trainData,
            String labelTransformationMethod, List<String> attributeEvaluator, int numLabelsToKeep,
            File featureSelectionResultsFile)
        throws TextClassificationException
    {
        Remove filterRemove = new Remove();
        try {
            MultiLabelInstances mulanInstances = convertMekaInstancesToMulanInstances(trainData);

            ASEvaluation eval = ASEvaluation.forName(attributeEvaluator.get(0), attributeEvaluator
                    .subList(1, attributeEvaluator.size()).toArray(new String[0]));

            AttributeEvaluator attributeSelectionFilter;

            // We currently only support the following Mulan Transformation methods (configuration
            // is complicated due to missing commandline support of mulan):
            if (labelTransformationMethod.equals("LabelPowersetAttributeEvaluator")) {
                attributeSelectionFilter = new LabelPowersetAttributeEvaluator(eval, mulanInstances);
            }
            else if (labelTransformationMethod.equals("BinaryRelevanceAttributeEvaluator")) {
                attributeSelectionFilter = new BinaryRelevanceAttributeEvaluator(eval,
                        mulanInstances, "max", "none", "rank");
            }
            else {
                throw new TextClassificationException(
                        "This Label Transformation Method is not supported.");
            }

            Ranker r = new Ranker();
            int[] result = r.search(attributeSelectionFilter, mulanInstances);

            // collect evaluation for *all* attributes and write to file
            StringBuffer evalFile = new StringBuffer();
            for (Attribute att : mulanInstances.getFeatureAttributes()) {
                evalFile.append(att.name()
                        + ": "
                        + attributeSelectionFilter.evaluateAttribute(att.index()
                                - mulanInstances.getNumLabels()) + "\n");
            }
            FileUtils.writeStringToFile(featureSelectionResultsFile, evalFile.toString());

            // create a filter to reduce the dimension of the attributes
            int[] toKeep = new int[numLabelsToKeep + mulanInstances.getNumLabels()];
            System.arraycopy(result, 0, toKeep, 0, numLabelsToKeep);
            int[] labelIndices = mulanInstances.getLabelIndices();
            System.arraycopy(labelIndices, 0, toKeep, numLabelsToKeep,
                    mulanInstances.getNumLabels());

            filterRemove.setAttributeIndicesArray(toKeep);
            filterRemove.setInvertSelection(true);
            filterRemove.setInputFormat(mulanInstances.getDataSet());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // less attributes than we want => no filtering
            return null;
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
        return filterRemove;
    }

    /**
     * Converts the Meka-specific instances format to Mulan-specific instances. Hierarchical
     * relationships among labels cannot be expressed.
     * 
     * @param instances
     * @return
     * @throws InvalidDataFormatException
     */
    public static MultiLabelInstances convertMekaInstancesToMulanInstances(Instances instances)
        throws InvalidDataFormatException
    {
        LabelsMetaDataImpl labelsMetaDataImpl = new LabelsMetaDataImpl();
        for (int i = 0; i < instances.classIndex(); i++) {
            String classAttName = instances.attribute(i).name();
            LabelNodeImpl labelNodeImpl = new LabelNodeImpl(classAttName);
            labelsMetaDataImpl.addRootNode(labelNodeImpl);
        }
        return new MultiLabelInstances(instances, labelsMetaDataImpl);
    }

    /**
     * Applies a filter to reduce the dimension of attributes and reorders them to be used within
     * Meka
     * 
     * @param trainData
     * @param removeFilter
     * @return a dataset to be used with Meka
     * @throws Exception
     */
    public static Instances applyAttributeSelectionFilter(Instances trainData, Remove removeFilter)
        throws Exception
    {
        Instances filtered = Filter.useFilter(trainData, removeFilter);
        filtered.setClassIndex(trainData.classIndex());
        // swap attributes to fit MEKA
        MekaClassAttributes attFilter = new MekaClassAttributes();
        attFilter.setAttributeIndices(filtered.numAttributes() - trainData.classIndex() + 1
                + "-last");
        attFilter.setInputFormat(filtered);
        filtered = Filter.useFilter(filtered, attFilter);
        int newClassindex = filtered.classIndex();
        filtered.setRelationName(filtered.relationName().replaceAll("\\-C\\s[\\d]+",
                "-C " + newClassindex));

        return filtered;
    }
}
