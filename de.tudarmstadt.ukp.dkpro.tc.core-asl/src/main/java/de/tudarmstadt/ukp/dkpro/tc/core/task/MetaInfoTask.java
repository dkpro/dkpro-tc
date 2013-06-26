package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.meta.MetaCollector;

public class MetaInfoTask
    extends UimaTaskBase
{

    public static final String META_KEY = "meta";
    public static final String INPUT_KEY = "preprocessing_input";

    @Discriminator
    boolean lowerCase;

    @Discriminator
    protected Object[] pipelineParameters;
    
    private List<Class<? extends MetaCollector>> metaCollectorClasses;

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

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        
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
        parameters.addAll(Arrays.asList(pipelineParameters));

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE), parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }
        
        AnalysisEngineDescription[] aeds = new AnalysisEngineDescription[metaCollectorClasses.size()];
        int i=0;
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            aeds[i] = createPrimitiveDescription(metaCollectorClass, parameters.toArray());
            i++;
        }
        
        return createAggregateDescription(aeds);
    }
    
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        return metaCollectorClasses;
    }

    public void setMetaCollectorClasses(List<Class<? extends MetaCollector>> metaCollectorClasses)
    {
        this.metaCollectorClasses = metaCollectorClasses;
    }
}