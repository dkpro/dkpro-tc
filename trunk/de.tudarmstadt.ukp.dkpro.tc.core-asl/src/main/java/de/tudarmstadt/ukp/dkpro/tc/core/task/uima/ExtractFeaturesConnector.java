package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

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

import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * UIMA analysis engine that is used in the {@link ExtractFeaturesTask} to apply the feature
 * extractors on each CAS.
 * 
 * @author zesch
 * 
 */
public class ExtractFeaturesConnector
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_ADD_INSTANCE_ID = "addInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true)
    protected boolean addInstanceId;

    public static final String PARAM_FEATURE_EXTRACTORS = "featureExtractors";
    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    public static final String PARAM_DATA_WRITER_CLASS = "dataWriterClass";
    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriterClass;

    public static final String PARAM_IS_REGRESSION_EXPERIMENT = "isRegressionExperiment";
    @ConfigurationParameter(name = PARAM_IS_REGRESSION_EXPERIMENT, mandatory = true, defaultValue = "false")
    private boolean isRegressionExperiment;

    // public static final String PARAM_PAIR_FEATURE_EXTRACTORS =
    // "pairFeatureExtractors";
    // @ExternalResource(key = PARAM_PAIR_FEATURE_EXTRACTORS, mandatory = false)
    // protected PairFeatureExtractorResource_ImplBase[] pairFeatureExtractors;

    protected InstanceList instanceList;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        instanceList = new InstanceList();

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }

        // if (pairFeatureExtractors != null) {
        // context.getLogger().log(Level.INFO,
        // "Using pair feature extractors.");
        // }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        Instance instance = new Instance();
        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
            try {
                if (featExt instanceof DocumentFeatureExtractor) {
                    instance.addFeatures(((DocumentFeatureExtractor) featExt).extract(jcas));
                }
                if (featExt instanceof ClassificationUnitFeatureExtractor) {
                    TextClassificationUnit classificationUnit = null;
                    Collection<TextClassificationUnit> classificationUnits = JCasUtil.select(jcas,
                            TextClassificationUnit.class);
                    if (classificationUnits.size() == 1) {
                        classificationUnit = classificationUnits.iterator().next();
                    }
                    else if (classificationUnits.size() > 1) {
                        throw new AnalysisEngineProcessException(
                                "There are more than one TextClassificationUnit anotations in the JCas.",
                                null);
                    }
                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, classificationUnit));
                }
                if (featExt instanceof PairFeatureExtractor) {
                    JCas view1 = jcas.getView(Constants.PART_ONE);
                    JCas view2 = jcas.getView(Constants.PART_TWO);
                    instance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        // if (pairFeatureExtractors != null) {
        // for (PairFeatureExtractor featExt : pairFeatureExtractors) {
        // try {
        // JCas view1 = jcas.getView(Constants.PART_ONE);
        // JCas view2 = jcas.getView(Constants.PART_TWO);
        // instance.addFeatures(featExt.extract(view1, view2));
        // }
        // catch (CASException e) {
        // throw new AnalysisEngineProcessException(e);
        // }
        // catch (TextClassificationException e) {
        // throw new AnalysisEngineProcessException(e);
        // }
        // }
        // }

        if (addInstanceId) {
            try {
                DocumentFeatureExtractor extractor = new AddIdFeatureExtractor();
                instance.addFeatures(extractor.extract(jcas));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        Collection<TextClassificationOutcome> outcome = JCasUtil.select(jcas,
                TextClassificationOutcome.class);

        if (outcome.size() == 0) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "No outcome annotations present in current CAS."));
        }

        String[] stringOutcomes = new String[outcome.size()];
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