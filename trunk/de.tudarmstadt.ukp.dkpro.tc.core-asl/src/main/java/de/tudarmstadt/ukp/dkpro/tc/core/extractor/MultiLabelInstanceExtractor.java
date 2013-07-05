package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class MultiLabelInstanceExtractor
    extends AbstractInstanceExtractor<String[]>
{

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        // TODO do we want to account for cases, where one Cas creates more than one Instance?
        Instance instance = new Instance();
        for (FeatureExtractor featExt : extractors) {
            try {
                instance.addFeatures(featExt.extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        if (addInstanceId) {
            try {
                instance.addFeatures(new AddIdFeatureExtractor().extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        Collection<TextClassificationOutcome> outcome = JCasUtil.select(jcas,
                TextClassificationOutcome.class);
        // FSCollectionFactory<FeatureStructure>.createStringList(jcas, aCollection)
        // TODO YC - Outcome Size 0
        /*
         * if (outcome.size() < 1) { throw new AnalysisEngineProcessException(new Throwable(
         * "No outcome for this instance.")); }
         */

        String[] stringOutcomes = new String[outcome.size()];
        Iterator<TextClassificationOutcome> iterator = outcome.iterator();
        for (int i = 0; i < outcome.size(); i++) {
            stringOutcomes[i] = iterator.next().getOutcome();
        }

        // set and write labels for multi-label classification
        instance.setOutcomes(stringOutcomes);
        this.instanceList.addInstance(instance);
    }
}