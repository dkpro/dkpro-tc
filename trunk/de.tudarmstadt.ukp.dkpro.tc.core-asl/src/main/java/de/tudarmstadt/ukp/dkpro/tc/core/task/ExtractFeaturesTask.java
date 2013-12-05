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

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

/**
 * Executes all feature extractors and stores the feature representation (usually an Weka ARFF file)
 * on disk.
 * 
 * @author zesch
 * 
 */
public class ExtractFeaturesTask
    extends UimaTaskBase
{

    public static final String OUTPUT_KEY = "output";
    public static final String INPUT_KEY = "input";

    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    private File filesRoot;
    @Discriminator
    private Collection<String> files_training;
    @Discriminator
    private Collection<String> files_validation;
    @Discriminator
    private boolean isRegressionExperiment;
    @Discriminator
    private String dataWriter;

    private boolean isTesting = false;
    private boolean addInstanceId = false;
    private List<Class<? extends MetaCollector>> metaCollectorClasses;
    private Set<String> requiredTypes;
    protected String featureAnnotation;

    public String getFeatureAnnotation()
    {
        return this.featureAnnotation;
    }

    public void setFeatureAnnotation(String featureAnnotation)
    {
        this.featureAnnotation = featureAnnotation;
    }

    public String getDataWriter()
    {
        return dataWriter;
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
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY),
                    parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }

        List<ExternalResourceDescription> extractorResources = new ArrayList<ExternalResourceDescription>();
        for (String featureExtractor : featureSet) {
            try {
                extractorResources.add(ExternalResourceFactory.createExternalResourceDescription(
                        Class.forName(featureExtractor).asSubclass(Resource.class),
                        parameters.toArray()));
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            }
        }

        parameters.addAll(Arrays.asList(ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY,
                outputDir.getAbsolutePath(), ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS,
                dataWriter, ExtractFeaturesConnector.PARAM_IS_REGRESSION_EXPERIMENT,
                isRegressionExperiment, ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID,
                addInstanceId, ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS,
                extractorResources));
        return createEngineDescription(ExtractFeaturesConnector.class, parameters.toArray());

    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null) {
            File root = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {

            Collection<String> files = isTesting ? files_validation : files_training;
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
    }
}