package org.cleartk.classifier.weka.multilabel;

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

public class DefaultMekaClassifier
    extends Classifier_ImplBase<Iterable<Feature>, String[], String[]>
{

    Classifier classifier;

    ArrayList<Attribute> attributes;

    Map<String, Attribute> attributeMap;

    Instances dataset;

    ReplaceMissingValuesWithZeroFilter filter;

    // TODO test, Filter might cause problems
    public DefaultMekaClassifier(Classifier mekaClassifier,
            FeaturesEncoder<Iterable<Feature>> featuresEncoder,
            OutcomeEncoder<String[], String[]> outcomeEncoder)
    {
        super(featuresEncoder, outcomeEncoder);
        this.classifier = mekaClassifier;

        this.attributes = ((WekaFeaturesEncoder) featuresEncoder).getWekaAttributes();
        this.attributeMap = ((WekaFeaturesEncoder) featuresEncoder).getWekaAttributeMap();

        dataset = new Instances("stub", attributes, 0);
        filter = new ReplaceMissingValuesWithZeroFilter();
        try {
            filter.setInputFormat(dataset);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] classify(List<Feature> features)
        throws CleartkProcessingException
    {
        Instance inst = instanceToWeka(features);
        List<String> outcomes = new ArrayList<String>();
        try {
            int attIndex = 0;
            for (double value : classifier.distributionForInstance(inst)) {
                // FIXME not optimal
                if (Math.round(value) == 1) {
                    outcomes.add(inst.attribute(attIndex).name());
                }
            }
            return decode(outcomes);
        }
        catch (Exception e) {
            throw new CleartkProcessingException(e);
        }
    }

    protected String[] decode(List<String> outcome)
    {
        return outcome.toArray(new String[outcome.size()]);
    }

    // TODO no implementation of the score method
    @Override
    public List<ScoredOutcome<String[]>> score(List<Feature> features, int maxResults)
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