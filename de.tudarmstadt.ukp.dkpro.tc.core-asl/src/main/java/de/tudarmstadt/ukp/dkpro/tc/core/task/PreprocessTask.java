package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;

public class PreprocessTask
    extends UimaTaskBase
{
    public static String OUTPUT_KEY = "preprocessorOutput";

    private CollectionReaderDescription reader;
    public void setReader(CollectionReaderDescription aReader)
    {
        reader = aReader;
    }
    public CollectionReaderDescription getReader()
    {
        return reader;
    }

    private AnalysisEngineDescription aggregate;
    public AnalysisEngineDescription getAggregate()
    {
        return aggregate;
    }
    public void setAggregate(AnalysisEngineDescription aAggregate)
    {
        aggregate = aAggregate;
    }


    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        return reader;
    }

	@Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        AnalysisEngineDescription xmiWriter = createPrimitiveDescription(
                SerializedCasWriter.class,
                SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
                SerializedCasWriter.PARAM_PATH, aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE).getPath()
        );

        return createAggregateDescription(aggregate, xmiWriter);
    }
}