package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.AbstractInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractorPair;
import de.tudarmstadt.ukp.dkpro.tc.core.weka.DKProTCMultiLabelDataWriterFactory;
import de.tudarmstadt.ukp.dkpro.tc.core.weka.DKProTCSingleLabelDataWriterFactory;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.POSNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

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

    @Discriminator
    protected String languageCode;


    private Class<? extends CleartkAnnotator> instanceExtractor;
    public Class<? extends CleartkAnnotator> getInstanceExtractor()
    {
        return instanceExtractor;
    }

    public void setInstanceExtractor(Class<? extends CleartkAnnotator> aInstanceExtractor)
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


    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE);
        File metaDir = aContext.getStorageLocation(META_KEY, AccessMode.READONLY);

        List<Object> parameters = new ArrayList<Object>();
        parameters.addAll(Arrays.asList(pipelineParameters));

        parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_NGRAM_FD_FILE,
                metaDir.getAbsolutePath() + "/" + NGramMetaCollector.NGRAM_FD_KEY));
        parameters.addAll(Arrays.asList(POSNGramFeatureExtractor.PARAM_POS_NGRAM_FD_FILE,
                metaDir.getAbsolutePath() + "/" + POSNGramMetaCollector.POS_NGRAM_FD_KEY));

        parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_USE_TOP_K, topNgramsK));

        // TODO YC NGRAM Freq Threshold
        if (ngramFreqThreshold != null) {
            parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_FREQ_THRESHOLD,
                    ngramFreqThreshold));
        }

        parameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_LOWER_CASE, lowerCase));

        if (multiLabel) {
            parameters.addAll(Arrays.asList(CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
                    DKProTCMultiLabelDataWriterFactory.class.getName(),
                    DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir.getAbsolutePath(),
                    DKProTCMultiLabelDataWriterFactory.PARAM_ADD_INSTANCE_ID, addInstanceId,
                    DKProTCMultiLabelDataWriterFactory.PARAM_FEATURES_IN_MEMORY, featuresInMemory,
                    AbstractInstanceExtractor.PARAM_FEATURE_EXTRACTORS, featureSet,
					SingleLabelInstanceExtractorPair.PARAM_PAIR_FEATURE_EXTRACTORS, pairFeatureSet
            ));
            
            return createAggregateDescription(createPrimitiveDescription(
                    instanceExtractor, parameters.toArray()));
        }
        else {
            parameters
                    .addAll(Arrays.asList(CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
                            DKProTCSingleLabelDataWriterFactory.class.getName(),
                            DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                            outputDir.getAbsolutePath(),
                            DKProTCSingleLabelDataWriterFactory.PARAM_ADD_INSTANCE_ID,
                            addInstanceId,
                            DKProTCSingleLabelDataWriterFactory.PARAM_FEATURES_IN_MEMORY, featuresInMemory,
                            AbstractInstanceExtractor.PARAM_FEATURE_EXTRACTORS, featureSet,
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

}