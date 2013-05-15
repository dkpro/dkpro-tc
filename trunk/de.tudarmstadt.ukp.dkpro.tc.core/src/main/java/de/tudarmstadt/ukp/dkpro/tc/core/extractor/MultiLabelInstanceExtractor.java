package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.tc.features.meta.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class MultiLabelInstanceExtractor
    extends AbstractInstanceExtractor<String[]>
{

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        // TODO do we want to account for cases, where one Cas creates more than one Instance?
        Instance<String[]> instance = new Instance<String[]>();
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
            instance.setOutcome(stringOutcomes);
            this.dataWriter.write(instance);
        }
        // classification
        else {
            // // for now we only train
            // String outcome = this.classifier.classify(instance);
        }
    }
}