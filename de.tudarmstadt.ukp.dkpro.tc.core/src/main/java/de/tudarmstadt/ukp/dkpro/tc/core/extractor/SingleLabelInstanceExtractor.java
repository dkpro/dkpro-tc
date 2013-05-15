package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.tc.features.meta.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class SingleLabelInstanceExtractor
    extends AbstractInstanceExtractor<String>
{

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        // TODO do we want to account for cases, where one Cas creates more than one Instance?
        Instance<String> instance = new Instance<String>();
        for (SimpleFeatureExtractor featExt : featureExtractors) {
            instance.addAll(featExt.extract(jcas, null));
        }

        if (addInstanceId) {
            instance.addAll(new AddIdFeatureExtractor().extract(jcas, null));
        }

        // training -> write instances to the data write
        if (this.isTraining()) {

            Collection<TextClassificationOutcome> outcome = JCasUtil.select(jcas,
                    TextClassificationOutcome.class);

            // set and write labels for single-label classification
            if (outcome.size() > 1) {
                throw new AnalysisEngineProcessException(
                        new Throwable("Cannot deal with multi-labeled instances. Please use MultiLabelInstanceExtractor instead.")
                );
            }
            instance.setOutcome(outcome.iterator().next().getOutcome());
            this.dataWriter.write(instance);
        }
        // classification
        else {
            throw new AnalysisEngineProcessException(new Throwable("The current version of DKPro TC does not make use of the cleartk test mode."));
        }
    }
}