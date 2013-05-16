/**
 * Copyright (c) 2012, Regents of the University of Colorado
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For a complete copy of the license please see the file LICENSE distributed
 * with the cleartk-syntax-berkeley project or visit
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.classifier.weka.singlelabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;
import org.cleartk.classifier.weka.WekaFeaturesEncoder;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public abstract class AbstractWekaClassifier<OUTCOME>
    extends Classifier_ImplBase<Iterable<Feature>, OUTCOME, String>
{

    Classifier classifier;

    ArrayList<Attribute> attributes;

    Map<String, Attribute> attributeMap;

    Instances dataset;

    ReplaceMissingValuesWithZeroFilter filter;

    public AbstractWekaClassifier(Classifier wekaClassifier,
            FeaturesEncoder<Iterable<Feature>> featuresEncoder,
            OutcomeEncoder<OUTCOME, String> outcomeEncoder)
    {
        super(featuresEncoder, outcomeEncoder);
        this.classifier = wekaClassifier;

        this.attributes = ((WekaFeaturesEncoder) featuresEncoder).getWekaAttributes();
        this.attributeMap = ((WekaFeaturesEncoder) featuresEncoder).getWekaAttributeMap();

        dataset = new Instances("stub", attributes, 0);
        dataset.setClassIndex(attributes.size() - 1); // see AbstractWekaDataWriter
        filter = new ReplaceMissingValuesWithZeroFilter();
        try {
            filter.setInputFormat(dataset);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OUTCOME classify(List<Feature> features)
        throws CleartkProcessingException
    {
        Instance inst = instanceToWeka(features);

        try {
            return decode(inst.classAttribute().value((int) classifier.classifyInstance(inst)));
        }
        catch (Exception e) {
            throw new CleartkProcessingException(e);
        }
    }

    abstract protected OUTCOME decode(String classifyInstance);

    // TODO no implementation of the score method
    @Override
    public List<ScoredOutcome<OUTCOME>> score(List<Feature> features, int maxResults)
    {
        throw new NotImplementedException();
    }

    private Instance instanceToWeka(List<Feature> features)
    {
        SparseInstance instance = new SparseInstance(attributes.size());
        instance.setDataset(dataset);

        for (Feature feature : features) {
            Attribute attribute = attributeMap.get(feature.getName());
            Object featureValue = feature.getValue();
            if (attribute != null) {// can happen for ngrams
                if (featureValue instanceof Number) {
                    double attributeValue = ((Number) feature.getValue()).doubleValue();
                    instance.setValue(attribute, attributeValue);
                }
                else if (featureValue instanceof Boolean) {
                    double attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                    instance.setValue(attribute, attributeValue);
                }
                else {
                    instance.setValue(attribute, featureValue.toString());
                }
            }
        }

        filter.input(instance);

        return filter.output();

    }

}
