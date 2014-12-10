package de.tudarmstadt.ukp.dkpro.tc.core.util;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class SaveModelUtils
    implements Constants
{
    public static void writeFeatureInformation(File outputFolder, List<String> featureSet)
        throws Exception
    {
        String featureExtractorString = StringUtils.join(featureSet, "\n");
        FileUtils.writeStringToFile(new File(outputFolder, MODEL_FEATURE_EXTRACTORS),
                featureExtractorString);
    }

    public static void writeModelParameters(TaskContext aContext, File aOutputFolder,
            List<String> aFeatureSet, List<Object> aFeatureParameters)
        throws Exception
    {
        // write meta collector data
        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        Set<Class<? extends MetaCollector>> metaCollectorClasses;
        try {
            metaCollectorClasses = TaskUtils.getMetaCollectorsFromFeatureExtractors(aFeatureSet);
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
        Map<String, String> metaParameterKeyPairs = new HashMap<String, String>();
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            try {
                metaParameterKeyPairs.putAll(metaCollectorClass.newInstance()
                        .getParameterKeyPairs());
            }
            catch (InstantiationException e) {
                throw new ResourceInitializationException(e);
            }
            catch (IllegalAccessException e) {
                throw new ResourceInitializationException(e);
            }
        }

        Properties parameterProperties = new Properties();
        for (Entry<String, String> entry : metaParameterKeyPairs.entrySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                    entry.getValue());
            parameterProperties.put(entry.getKey(), file.getAbsolutePath());
        }

        for (int i = 0; i < aFeatureParameters.size(); i = i + 2) {
            parameterProperties.put((String) aFeatureParameters.get(i).toString(),
                    aFeatureParameters.get(i + 1).toString());
        }

        FileWriter writer = new FileWriter(new File(aOutputFolder, MODEL_PARAMETERS));
        parameterProperties.store(writer, "");
        writer.close();
    }

    public static void writeModelAdapterInformation(File aOutputFolder, String aModelMeta)
        throws Exception
    {
        // as a marker for the type, write the name of the ml adapter class
        // write feature extractors
        FileUtils.writeStringToFile(new File(aOutputFolder, MODEL_META), aModelMeta);
    }

}
