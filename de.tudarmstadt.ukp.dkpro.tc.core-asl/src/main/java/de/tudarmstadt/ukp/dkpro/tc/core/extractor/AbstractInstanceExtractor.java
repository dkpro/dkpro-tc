package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.component.initialize.ExternalResourceInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.initializable.Initializable;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;

public abstract class AbstractInstanceExtractor<OUTCOME_TYPE>
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_ADD_INSTANCE_ID = "AddInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true)
    protected boolean addInstanceId;

    public static final String PARAM_FEATURE_EXTRACTORS = "Extractors";
    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    private String[] extractorClasses;
    
    public static final String PARAM_DATA_WRITER_CLASS = "DataWriterClass";
    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriterClass;

    protected List<FeatureExtractor> featureExtractors;
    
    protected InstanceList instanceList;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        instanceList = new InstanceList();

        if (extractorClasses.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractor has been defined.");
            throw new ResourceInitializationException();
        }

        // instantiate FEs and add to list. They are then used by the extending
        // (SL/ML)InstanceExtractor
        featureExtractors = new ArrayList<FeatureExtractor>();
        try {
            for (String name : extractorClasses) {
                featureExtractors.add((FeatureExtractor) Class.forName(name).newInstance());
            }
        }
        catch (Exception e) {
            context.getLogger()
                    .log(Level.SEVERE,
                            "Could not load feature extractors. Check the fully qualified feature extractor class names defined in your configuration.");
            throw new ResourceInitializationException(e);
        }

        // UIMA Magic - init FE-AEs
        for (FeatureExtractor featExt : featureExtractors) {
            ConfigurationParameterInitializer.initialize(featExt, context);
            ExternalResourceInitializer.initialize(context, featExt);

            if (featExt instanceof Initializable) {
                ((Initializable) featExt).initialize(context);
            }
        }
    }

    @Override
    public abstract void process(JCas jcas)
        throws AnalysisEngineProcessException;

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
                    
        // addInstanceId requires dense instances, thus reuse boolean
        try {
            DataWriter writer = (DataWriter) Class.forName(dataWriterClass).newInstance();
            writer.write(outputDirectory, instanceList, addInstanceId);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}