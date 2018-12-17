/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.util.AttributeStore;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;
import static java.nio.charset.StandardCharsets.UTF_8;

public class _eka
    implements Constants
{
    private static Log logger = LogFactory.getLog(_eka.class);

    protected Instances toWekaInstances(File data, boolean isMultilabel) throws Exception
    {
        Instances train = getInstances(data, isMultilabel);
        Instances wekaData = removeInstanceId(train, isMultilabel);
        return wekaData;
    }

    /**
     * Removes the instanceId attribute, iff present
     *
     * @param data
     *            data set with or without instanceId attribute
     * @param multilabel
     *            is multi label processing
     * @return the data set without instanceId attribute
     * @throws Exception
     *             an exception
     */
    public static Instances removeInstanceId(Instances data, boolean multilabel) throws Exception
    {

        Instances filteredData;
        int classIndex = data.classIndex();

        if (data.attribute(Constants.ID_FEATURE_NAME) != null) {
            int instanceIdOffset = data.attribute(Constants.ID_FEATURE_NAME).index();

            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(instanceIdOffset + 1));
            remove.setInvertSelection(false);
            remove.setInputFormat(data);
            filteredData = Filter.useFilter(data, remove);
        }
        else {
            filteredData = new Instances(data);
        }
        // make sure the class index gets retained in multi-label
        if (multilabel) {
            filteredData.setClassIndex(classIndex);
        }
        return filteredData;
    }

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
     *             if file is not found
     * @throws IOException
     *             if an exception occurs
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

        try (Reader reader = new InputStreamReader(underlyingStream, UTF_8)) {
            Instances trainData = new Instances(reader);

            if (multiLabel) {
                String relationTag = trainData.relationName();
                // for multi-label classification, class labels are expected at beginning of
                // attribute
                // set and their number must be specified with the -C parameter in the relation tag
                Matcher m = Pattern.compile("-C\\s\\d+").matcher(relationTag);
                m.find();
                trainData.setClassIndex(Integer.parseInt(m.group().split("-C ")[1]));
            }
            else {
                // for single-label classification, class label expected as last attribute
                trainData.setClassIndex(trainData.numAttributes() - 1);
            }
            return trainData;
        }
    }

    public static Instances addInstanceId(Instances newData, Instances oldData,
            boolean isMultilabel)
        throws Exception
    {
        Instances filteredData;

        if (oldData.attribute(Constants.ID_FEATURE_NAME) != null) {
            int instanceIdOffset = oldData.attribute(Constants.ID_FEATURE_NAME).index();

            Add add = new Add();
            add.setAttributeName(Constants.ID_FEATURE_NAME);
            // for multi-label setups, id attribute goes to the end of the header, and vice verse
            // for single-label
            if (isMultilabel) {
                add.setAttributeIndex("last");
            }
            else {
                add.setAttributeIndex("first");
            }
            add.setAttributeType(new SelectedTag(Attribute.STRING, Add.TAGS_TYPE));
            add.setInputFormat(newData);
            filteredData = Filter.useFilter(newData, add);
            int j = isMultilabel ? filteredData.numAttributes() - 1 : 0;
            for (int i = 0; i < filteredData.numInstances(); i++) {
                String outcomeId = oldData.instance(i).stringValue(instanceIdOffset);
                filteredData.instance(i).setValue(j, outcomeId);
            }
        }
        else {
            filteredData = new Instances(newData);
        }
        return filteredData;
    }

    public static List<String> getClassLabels(Instances data, boolean isMultilabel)
    {
        List<String> classLabelList = new ArrayList<String>();
        if (!isMultilabel) {
            Enumeration<Object> classLabels = data.classAttribute().enumerateValues();
            while (classLabels.hasMoreElements()) {
                classLabelList.add((String) classLabels.nextElement());
            }
        }
        else {
            int numLabels = data.classIndex();
            for (int i = 0; i < numLabels; i++) {
                classLabelList.add(data.attribute(i).name());
            }
        }
        return classLabelList;
    }

    public weka.core.Instance tcInstanceToWekaInstance(Instance instance, Instances trainingData,
            List<String> allClasses, boolean isRegressionExperiment)
        throws Exception
    {
        AttributeStore attributeStore = new AttributeStore();

        // outcome attribute is last and will be ignored
        for (int i = 0; i < trainingData.numAttributes() - 1; i++) {
            attributeStore.addAttribute(trainingData.attribute(i).name(),
                    trainingData.attribute(i));
        }

        // add outcome attribute
        Attribute outcomeAttribute = createOutcomeAttribute(allClasses, isRegressionExperiment);
        attributeStore.addAttribute(outcomeAttribute.name(), outcomeAttribute);

        double[] featureValues = getFeatureValues(attributeStore, instance);

        SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
        sparseInstance.setDataset(trainingData);
        return sparseInstance;
    }

    private Attribute createOutcomeAttribute(List<String> outcomeValues, boolean isRegresion)
    {
        if (isRegresion) {
            return new Attribute(CLASS_ATTRIBUTE_NAME);
        }
        else {
            // make the order of the attributes predictable
            Set<String> outcomesUnique = new HashSet<>(outcomeValues);
            outcomeValues = new ArrayList<>(outcomesUnique);
            Collections.sort(outcomeValues);
            return new Attribute(CLASS_ATTRIBUTE_NAME, outcomeValues);
        }
    }

    private double[] getFeatureValues(AttributeStore attributeStore, Instance instance)
    {
        double[] featureValues = new double[attributeStore.getAttributes().size()];

        for (Feature feature : instance.getFeatures()) {

            try {
                Attribute attribute = attributeStore.getAttribute(feature.getName());
                Object featureValue = feature.getValue();

                double attributeValue;
                if (featureValue instanceof Number) {
                    // numeric attribute
                    attributeValue = ((Number) feature.getValue()).doubleValue();
                }
                else if (featureValue instanceof Boolean) {
                    // boolean attribute
                    attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                }
                else if (featureValue == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
                }
                else {
                    // nominal or string
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException(
                                "Attribute neither nominal nor string: " + stringValue);
                    }

                    int valIndex = attribute.indexOfValue(stringValue.toString());
                    if (valIndex == -1) {
                        if (attribute.isNominal()) {
                            throw new IllegalArgumentException(
                                    "Value not defined for given nominal attribute!");
                        }
                        else {
                            attribute.addStringValue(stringValue.toString());
                            valIndex = attribute.indexOfValue(stringValue.toString());
                        }
                    }
                    attributeValue = valIndex;
                }
                int offset = attributeStore.getAttributeOffset(attribute.name());

                if (offset != -1) {
                    featureValues[offset] = attributeValue;
                }
            }
            catch (NullPointerException e) {
                logger.debug("Unseen attribute [" + feature.getName() + "] -- silently ignored");
            }
        }
        return featureValues;
    }
}
