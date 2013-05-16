package org.cleartk.classifier.weka.singlelabel;

import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;

public class StringWekaClassifierBuilder
    extends AbstractWekaClassifierBuilder<String>
{

    public StringWekaClassifierBuilder()
    {
        super();
        this.setOutcomeEncoder(new StringToStringOutcomeEncoder());
    }

    @Override
    protected StringWekaClassifier newClassifier()
    {
        return new StringWekaClassifier(this.classifier, this.featuresEncoder, this.outcomeEncoder);
    }
}
