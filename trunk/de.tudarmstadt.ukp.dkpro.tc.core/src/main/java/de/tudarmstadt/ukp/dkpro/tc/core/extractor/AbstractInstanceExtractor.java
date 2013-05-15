package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.component.initialize.ExternalResourceInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.initializable.Initializable;

/**
 * Abstract InstanceExtractor. Takes care of instantiating the Feature Extractor Analysis Engines
 *
 * @author oferschke
 *
 * @param <OUTCOME_TYPE>
 */
public abstract class AbstractInstanceExtractor<OUTCOME_TYPE>
    extends CleartkAnnotator<OUTCOME_TYPE>
{

    public static final String PARAM_ADD_INSTANCE_ID = "AddInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true)
    protected boolean addInstanceId;

    public static final String PARAM_FEATURE_EXTRACTORS = "Extractors";
    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    private String[] extractorClasses;

    protected List<SimpleFeatureExtractor> featureExtractors;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        if(extractorClasses.length==0){
            context.getLogger().log(Level.SEVERE, "No feature extractor has been defined.");
            throw new ResourceInitializationException();
        }

        //instantiate FEs and add to list. They are then used by the extending (SL/ML)InstanceExtractor
        featureExtractors = new ArrayList<SimpleFeatureExtractor>();
        try{
            for (String name : extractorClasses) {
                featureExtractors.add((SimpleFeatureExtractor) Class.forName(name).newInstance());
            }
        }
        catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Could not load feature extractors. Check the fully qualified feature extractor class names defined in your configuration.");
            throw new ResourceInitializationException(e);
        }

        //UIMA Magic - init FE-AEs
        for (SimpleFeatureExtractor featExt : featureExtractors) {
            ConfigurationParameterInitializer.initialize(featExt, context);
            ExternalResourceInitializer.initialize(context, featExt);

            if (featExt instanceof Initializable) {
                ((Initializable) featExt).initialize(context);
            }
        }
    }

    @Override
    public abstract void process(JCas jcas) throws AnalysisEngineProcessException;
}