package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.component.initialize.ExternalResourceInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.meta.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class SingleLabelInstanceExtractorPair
    extends AbstractInstanceExtractor<String>
{

    public static final String PARAM_PAIR_FEATURE_EXTRACTORS = "PairExtractors";
    @ConfigurationParameter(name = PARAM_PAIR_FEATURE_EXTRACTORS, mandatory = true)
    private String[] pairExtractorClasses;

    protected List<PairFeatureExtractor> pairFeatureExtractors;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        pairFeatureExtractors = new ArrayList<PairFeatureExtractor>();
        try{
            for (String name : pairExtractorClasses) {
                pairFeatureExtractors.add((PairFeatureExtractor) Class.forName(name).newInstance());
            }
        }
        catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Could not load feature extractors. Check the fully qualified feature extractor class names defined in your configuration.");
            throw new ResourceInitializationException(e);
        }

        for (PairFeatureExtractor featExt : pairFeatureExtractors) {
            ConfigurationParameterInitializer.initialize(featExt, context);
            ExternalResourceInitializer.initialize(context, featExt);

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

        for (FeatureExtractor featExt : featureExtractors) {
            try {
                instance.addFeatures(featExt.extract(jcas, null));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        for (PairFeatureExtractor featExt : pairFeatureExtractors) {
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