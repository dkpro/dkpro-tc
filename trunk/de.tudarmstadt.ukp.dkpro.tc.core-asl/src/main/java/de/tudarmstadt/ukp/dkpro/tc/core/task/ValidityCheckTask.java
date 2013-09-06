package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.ValidityChecker;

/**
 * This should be the first task in the TC pipeline.
 * It checks that everything has been configured properly and throws more meaningful exception otherwise than would have been thrown downstream.
 * 
 * @author zesch
 *
 */
public class ValidityCheckTask
    extends UimaTaskBase
{
    public static final String DUMMY_KEY = "dummy";

    @Discriminator
    protected Class<? extends CollectionReader> readerTrain;

    @Discriminator
    protected List<Object> readerTrainParams;

    @Discriminator
    protected List<Object> pipelineParameters;
    
    @Discriminator
    private boolean isRegressionExperiment = false;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        CollectionReaderDescription readerDesc = createReaderDescription(readerTrain,
                readerTrainParams.toArray());

        return readerDesc;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {    
        
        // make a dummy folder so that the lab can create an import on it
        File file = new File(aContext.getStorageLocation(DUMMY_KEY, AccessMode.READWRITE)
                .getPath());
        file.mkdir();
        
        List<Object> parameters = new ArrayList<Object>();
        if (pipelineParameters != null) {
            parameters.addAll(pipelineParameters);
        }
        parameters.add(ValidityChecker.PARAM_IS_REGRESSION);
        parameters.add(isRegressionExperiment);
        
        return createEngineDescription(ValidityChecker.class, parameters.toArray());
    }
}