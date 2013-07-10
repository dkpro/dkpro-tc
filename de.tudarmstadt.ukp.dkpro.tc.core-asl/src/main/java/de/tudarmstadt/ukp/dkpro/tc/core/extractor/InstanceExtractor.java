package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class InstanceExtractor
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "OutputDirectory";
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

    public static final String PARAM_PAIR_FEATURE_EXTRACTORS = "PairExtractors";
    @ExternalResource(key = PARAM_PAIR_FEATURE_EXTRACTORS, mandatory = false)
    protected PairFeatureExtractorResource_ImplBase[] pairExtractors;

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

        if (pairExtractors != null) {
            context.getLogger().log(Level.INFO, "Using pair feature extractors.");
        }
    }

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

        // TODO better way to account for pair feature extractors?
        if (pairExtractors != null) {
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
        String[] stringOutcomes = new String[outcome.size()];

        // TODO
        // we are currently relying on the user to set the right DataWriter for multi- and
        // single-label, i.e. a wrong configuration will cause exceptions later on
        Iterator<TextClassificationOutcome> iterator = outcome.iterator();
        for (int i = 0; i < outcome.size(); i++) {
            stringOutcomes[i] = iterator.next().getOutcome();
        }

        // set and write label(s)
        instance.setOutcomes(stringOutcomes);
        this.instanceList.addInstance(instance);
    }

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