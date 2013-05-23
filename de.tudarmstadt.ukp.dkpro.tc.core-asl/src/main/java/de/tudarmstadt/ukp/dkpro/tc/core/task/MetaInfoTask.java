package de.tudarmstadt.ukp.dkpro.tc.core.task;

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

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.POSNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

public class MetaInfoTask
    extends UimaTaskBase
{
    public static final String META_KEY = "meta";
    public static final String INPUT_KEY = "preprocessing_input";

    @Discriminator
    boolean lowerCase;

    @Discriminator
    protected Object[] pipelineParameters;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String path = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY).getPath();
        return createDescription(
                SerializedCasReader.class,
                SerializedCasReader.PARAM_PATH, path + "/",
                SerializedCasReader.PARAM_PATTERNS, new String[]{ SerializedCasReader.INCLUDE_PREFIX + "**/*.ser.gz" }
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File ngramsFile = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                NGramMetaCollector.NGRAM_FD_KEY);
        File posngramsFile = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                POSNGramMetaCollector.POS_NGRAM_FD_KEY);

        List<Object> ngramParameters = new ArrayList<Object>();
        ngramParameters.addAll(Arrays.asList(pipelineParameters));
        ngramParameters.addAll(Arrays.asList(NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, ngramsFile,
        		NGramFeatureExtractor.PARAM_LOWER_CASE, lowerCase));

        List<Object> posParameters = new ArrayList<Object>();
        posParameters.addAll(Arrays.asList(pipelineParameters));
        posParameters.addAll(Arrays.asList(POSNGramFeatureExtractor.PARAM_POS_NGRAM_FD_FILE, posngramsFile,
                POSNGramFeatureExtractor.PARAM_LOWER_CASE, lowerCase));

        return createAggregateDescription(
                createPrimitiveDescription(NGramMetaCollector.class, ngramParameters.toArray()),
                createPrimitiveDescription(POSNGramMetaCollector.class, posParameters.toArray())
        );
    }
}