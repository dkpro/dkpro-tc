package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.meta.MetaCollector;

public class ExtractFeaturesTask
    extends UimaTaskBase
{

    public static final String OUTPUT_KEY = "output";
    public static final String INPUT_KEY = "preprocessing_input";

    @Discriminator
    protected String[] featureSet;
    // @Discriminator
    // protected String[] pairFeatureSet;
    @Discriminator
    protected Object[] featureParameters;

    @Discriminator
    protected String featureAnnotation;

    private String dataWriter;
    private boolean isRegressionExperiment = false;
    private boolean addInstanceId = false;
    private List<Class<? extends MetaCollector>> metaCollectorClasses;

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE);

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
        // adding FE parameters, if any
        if (featureParameters != null) {
            parameters.addAll(Arrays.asList(featureParameters));
        }

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                    parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }

        ExternalResourceDescription[] extractorResources = new ExternalResourceDescription[featureSet.length];
        for (int i = 0; i < featureSet.length; i++) {
            // System.out.println(featureSet[i]);
            try {
                extractorResources[i] = ExternalResourceFactory.createExternalResourceDescription(
                        (Class) Class.forName(featureSet[i]), parameters.toArray());
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

        return createAggregateDescription(createPrimitiveDescription(InstanceExtractor.class,
                parameters.toArray()));

    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String path = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY).getPath();
        return createDescription(SerializedCasReader.class, SerializedCasReader.PARAM_PATH, path
                + "/", SerializedCasReader.PARAM_PATTERNS,
                new String[] { SerializedCasReader.INCLUDE_PREFIX + "**/*.ser.gz" });
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

    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        return metaCollectorClasses;
    }

    public void setMetaCollectorClasses(List<Class<? extends MetaCollector>> metaCollectorClasses)
    {
        this.metaCollectorClasses = metaCollectorClasses;
    }

    public boolean getAddInstanceId()
    {
        return addInstanceId;
    }

    public void setAddInstanceId(boolean aAddInstanceId)
    {
        addInstanceId = aAddInstanceId;
    }

}