package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
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
        Instance instance = new Instance();
        for (FeatureExtractor featExt : featureExtractors) {
            try {
                instance.addFeatures(featExt.extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        if (addInstanceId) {
            try {
                FeatureExtractor extractor = new AddIdFeatureExtractor();
                instance.addFeatures(extractor.extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

         Collection<TextClassificationOutcome> outcome = JCasUtil.select(jcas,
                TextClassificationOutcome.class);

        // set and write labels for single-label classification
        if (outcome.size() > 1) {
            throw new AnalysisEngineProcessException(
                    new Throwable("Cannot deal with multi-labeled instances. Please use MultiLabelInstanceExtractor instead.")
            );
        }
        instance.setOutcomes(outcome.iterator().next().getOutcome());
        this.instanceList.addInstance(instance);
    }
}