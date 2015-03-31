package de.tudarmstadt.ukp.dkpro.tc.ml.modelpersist;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_FEATURE_EXTRACTORS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_META;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_PARAMETERS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;

public class ModelPersistUtil
{
    public static TCMachineLearningAdapter initMachineLearningAdapter(File tcModelLocation) throws Exception
    {
        File modelMeta = new File(tcModelLocation, MODEL_META);
        String fileContent = FileUtils.readFileToString(modelMeta);
        Class<?> classObj = Class.forName(fileContent);
        return (TCMachineLearningAdapter) classObj.newInstance();
    }

    public static List<String> initFeatureExtractors(File tcModelLocation)
        throws Exception
    {
        List<String> featureExtractors = new ArrayList<>();
        File featureExtractorsDescription = new File(tcModelLocation, MODEL_FEATURE_EXTRACTORS);
        List<String> featureConfiguration = FileUtils.readLines(featureExtractorsDescription);
        for (String featureExtractor : featureConfiguration) {
            featureExtractors.add(featureExtractor);
        }
        return featureExtractors;
    }

    public static List<Object> initParameters(File tcModelLocation)
        throws IOException
    {
        List<Object> parameters = new ArrayList<>();
        for (String parameter : FileUtils.readLines(new File(tcModelLocation, MODEL_PARAMETERS))) {
            if (!parameter.startsWith("#")) {
                String[] parts = parameter.split("=");
                parameters.add(parts[0]);

                if (isExistingFilePath(tcModelLocation, parts[1])) {
                    parameters.add(tcModelLocation + "/" + parts[1]);
                }
                else {
                    parameters.add(parts[1]);
                }
            }
        }
        return parameters;
    }

    private static boolean isExistingFilePath(File tcModelLocation, String name)
    {
        
        return new File(tcModelLocation.getAbsolutePath() + "/" + name).exists();
    }

}
