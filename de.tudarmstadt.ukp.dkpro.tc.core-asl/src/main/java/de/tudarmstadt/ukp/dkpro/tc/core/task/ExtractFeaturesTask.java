package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

public class ExtractFeaturesTask
    extends UimaTaskBase
{

    public static final String OUTPUT_KEY = "output";
    public static final String INPUT_KEY = "input";

    @Discriminator
    protected List<String> featureSet;
    // @Discriminator
    // protected String[] pairFeatureSet;
    @Discriminator
    protected List<Object> pipelineParameters;

    @Discriminator
    protected String featureAnnotation;

    @Discriminator
    private File filesRoot;

    @Discriminator
    private Collection<String> files_training;

    @Discriminator
    private Collection<String> files_validation;

    @Discriminator
    private boolean isRegressionExperiment = false;

    private String dataWriter;
    private boolean isTesting = false;
    private boolean addInstanceId = false;
    private List<Class<? extends MetaCollector>> metaCollectorClasses;
    private Set<String> requiredTypes;

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE);

        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        try {
            metaCollectorClasses = TaskUtils.getMetaCollectorsFromFeatureExtractors(featureSet);
            requiredTypes = TaskUtils.getRequiredTypesFromFeatureExtractors(featureSet);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        // collect parameter/key pairs that need to be set
        Map<String, String> parameterKeyPairs = new HashMap<String, String>();
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            try {
                parameterKeyPairs.putAll(metaCollectorClass.newInstance().getParameterKeyPairs());
            }
            catch (InstantiationException e) {
                throw new ResourceInitializationException(e);
            }
            catch (IllegalAccessException e) {
                throw new ResourceInitializationException(e);
            }
        }

        List<Object> parameters = new ArrayList<Object>();
        // adding pipeline parameters, if any
        if (pipelineParameters != null) {
            // convert parameters to string as external resources only take string parameters
            for (Object parameter : pipelineParameters) {
                parameters.add(parameter.toString());
            }
        }

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                    parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }

        List<ExternalResourceDescription> extractorResources = new ArrayList<ExternalResourceDescription>();
        for (String featureExtractor : featureSet) {
            // System.out.println(featureSet[i]);
            try {
                extractorResources.add(
                        ExternalResourceFactory.createExternalResourceDescription(
                        (Class) Class.forName(featureExtractor), parameters.toArray())
                );
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            }
        }

        // // TODO YC NGRAM Freq Threshold
        // if (ngramFreqThreshold != null) {
        // parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_FREQ_THRESHOLD,
        // ngramFreqThreshold));
        // }

        parameters.addAll(Arrays.asList(InstanceExtractor.PARAM_OUTPUT_DIRECTORY,
                outputDir.getAbsolutePath(), InstanceExtractor.PARAM_DATA_WRITER_CLASS, dataWriter,
                InstanceExtractor.PARAM_IS_REGRESSION_EXPERIMENT, isRegressionExperiment,
                InstanceExtractor.PARAM_ADD_INSTANCE_ID, addInstanceId,
                InstanceExtractor.PARAM_FEATURE_EXTRACTORS, extractorResources,
                // InstanceExtractor.PARAM_PAIR_FEATURE_EXTRACTORS, pairFeatureSet)
                InstanceExtractor.PARAM_FEATURE_ANNOTATION, featureAnnotation
                ));

        return createEngineDescription(InstanceExtractor.class,
                parameters.toArray());

    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null) {
            File file = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY);
            return createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, file,
                    BinaryCasReader.PARAM_PATTERNS, BinaryCasReader.INCLUDE_PREFIX + "**/*.bin");

        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {
            Collection<String> patterns = new ArrayList<String>();
            Collection<String> files = isTesting ? files_validation : files_training;
            for (String f : files) {
                patterns.add(BinaryCasReader.INCLUDE_PREFIX + "**/*" + f);
            }

            return createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, filesRoot,
                    BinaryCasReader.PARAM_PATTERNS, patterns);
        }
    }

    public String getDataWriter()
    {
        return dataWriter;
    }

    public void setDataWriter(String aDataWriterClassName)
    {
        dataWriter = aDataWriterClassName;
    }

    public boolean isRegressionExperiment()
    {
        return isRegressionExperiment;
    }

    public void setRegressionExperiment(boolean isRegressionExperiment)
    {
        this.isRegressionExperiment = isRegressionExperiment;
    }

    public boolean getAddInstanceId()
    {
        return addInstanceId;
    }

    public void setAddInstanceId(boolean aAddInstanceId)
    {
        addInstanceId = aAddInstanceId;
    }

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }
}