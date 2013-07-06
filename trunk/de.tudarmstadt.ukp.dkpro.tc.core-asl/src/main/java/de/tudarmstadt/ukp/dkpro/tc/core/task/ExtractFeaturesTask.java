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
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.AbstractInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.MultiLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractorPair;
import de.tudarmstadt.ukp.dkpro.tc.core.meta.MetaCollector;

public class ExtractFeaturesTask
    extends UimaTaskBase
{

    public static final String OUTPUT_KEY = "output";
    public static final String INPUT_KEY = "preprocessing_input";

    @Discriminator
    protected Boolean lowerCase;
    @Discriminator
    protected Integer topNgramsK;
    @Discriminator
    protected Float ngramFreqThreshold;
    @Discriminator
    protected Float posNgramFreqThreshold;

    @Discriminator
    protected String[] featureSet;

    @Discriminator
    protected String[] pairFeatureSet;

    @Discriminator
    boolean multiLabel;

    @Discriminator
    boolean featuresInMemory = false;

    @Discriminator
    protected Object[] pipelineParameters;

    private String dataWriter;
    
    private boolean isRegressionExperiment = false;

    private List<Class<? extends MetaCollector>> metaCollectorClasses;
    
    private Class<? extends AbstractInstanceExtractor> instanceExtractor;

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE);

        // collect parameter/key pairs that need to be set
        Map<String,String> parameterKeyPairs = new HashMap<String,String>();
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

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE), parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }
        
        ExternalResourceDescription[] extractorResources = new ExternalResourceDescription[featureSet.length];
        for (int i=0; i < featureSet.length; i++) {
            System.out.println(featureSet[i]);
            try {
                extractorResources[i] = ExternalResourceFactory.createExternalResourceDescription(
                        (Class) Class.forName(featureSet[i]),
                        parameters.toArray()
                );
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            } 
        }
        
        parameters.addAll(Arrays.asList(pipelineParameters));

               
// TODO feature parameters are going to be handled via FE-resources
//        parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_USE_TOP_K, topNgramsK));
//
//        // TODO YC NGRAM Freq Threshold
//        if (ngramFreqThreshold != null) {
//            parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_FREQ_THRESHOLD,
//                    ngramFreqThreshold));
//        }
//
//        parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_LOWER_CASE, lowerCase));

        // FIXME do we still need that switch?
        if (multiLabel) {
            parameters.addAll(Arrays.asList(
                    MultiLabelInstanceExtractor.PARAM_OUTPUT_DIRECTORY, outputDir.getAbsolutePath(),
                    MultiLabelInstanceExtractor.PARAM_DATA_WRITER_CLASS, dataWriter,
                    MultiLabelInstanceExtractor.PARAM_IS_REGRESSION, isRegressionExperiment,
                    MultiLabelInstanceExtractor.PARAM_ADD_INSTANCE_ID, addInstanceId,
                    MultiLabelInstanceExtractor.PARAM_FEATURE_EXTRACTORS, extractorResources,
					SingleLabelInstanceExtractorPair.PARAM_PAIR_FEATURE_EXTRACTORS, pairFeatureSet
            ));

            return createAggregateDescription(createPrimitiveDescription(
                    instanceExtractor, parameters.toArray()));
        }
        else {
            parameters
                    .addAll(Arrays.asList(
                            SingleLabelInstanceExtractor.PARAM_OUTPUT_DIRECTORY, outputDir.getAbsolutePath(),
                            SingleLabelInstanceExtractor.PARAM_DATA_WRITER_CLASS, dataWriter,
                            SingleLabelInstanceExtractor.PARAM_IS_REGRESSION, isRegressionExperiment,
                            SingleLabelInstanceExtractor.PARAM_ADD_INSTANCE_ID, addInstanceId,
                            SingleLabelInstanceExtractor.PARAM_FEATURE_EXTRACTORS, extractorResources,
                            SingleLabelInstanceExtractorPair.PARAM_PAIR_FEATURE_EXTRACTORS, pairFeatureSet
			));
            return createAggregateDescription(createPrimitiveDescription(
                    instanceExtractor, parameters.toArray()));
        }

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
    
    public Class<? extends AbstractInstanceExtractor> getInstanceExtractor()
    {
        return instanceExtractor;
    }

    public void setInstanceExtractor(Class<? extends AbstractInstanceExtractor> aInstanceExtractor)
    {
        instanceExtractor = aInstanceExtractor;
    }

    public boolean getAddInstanceId()
    {
        return addInstanceId;
    }

    public void setAddInstanceId(boolean aAddInstanceId)
    {
        addInstanceId = aAddInstanceId;
    }
    
    private boolean addInstanceId=false;

}