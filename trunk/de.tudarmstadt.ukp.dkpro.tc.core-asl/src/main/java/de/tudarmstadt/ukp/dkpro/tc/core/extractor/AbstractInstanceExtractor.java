package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;

public abstract class AbstractInstanceExtractor
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_ADD_INSTANCE_ID = "AddInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true)
    protected boolean addInstanceId;

    public static final String PARAM_FEATURE_EXTRACTORS = "Extractors";
    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractor[] extractors;

    public static final String PARAM_DATA_WRITER_CLASS = "DataWriterClass";
    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriterClass;

    public static final String PARAM_IS_REGRESSION = "IsRegressionExperiment";
    @ConfigurationParameter(name = PARAM_IS_REGRESSION, mandatory = true, defaultValue = "false")
    private boolean isRegressionExperiment;

    protected InstanceList instanceList;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        instanceList = new InstanceList();

        if (extractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
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
            writer.write(outputDirectory, instanceList, addInstanceId, isRegressionExperiment);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}