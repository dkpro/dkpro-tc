package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class SingleLabelInstanceExtractorPair
    extends AbstractInstanceExtractor<String>
{

    public static final String PARAM_PAIR_FEATURE_EXTRACTORS = "PairExtractors";
    @ExternalResource(key = PARAM_PAIR_FEATURE_EXTRACTORS, mandatory = true)
    private PairFeatureExtractorResource_ImplBase[] pairExtractors;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        for (PairFeatureExtractor featExt : pairExtractors) {
            ConfigurationParameterInitializer.initialize(featExt, context);
            ExternalResourceInitializer.initialize(featExt, context);

            if (featExt instanceof Initializable) {
                ((Initializable) featExt).initialize(context);
            }
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        Instance instance = new Instance();

        for (FeatureExtractor featExt : extractors) {
            try {
                instance.addFeatures(featExt.extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        for (PairFeatureExtractor featExt : pairExtractors) {
            try {
                JCas view1 = jcas.getView(Constants.PART_ONE);
                JCas view2 = jcas.getView(Constants.PART_TWO);
                instance.addFeatures(featExt.extract(view1, view2));
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
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