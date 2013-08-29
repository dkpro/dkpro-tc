package de.tudarmstadt.ukp.dkpro.tc.demo.regression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.demo.regression.io.STSReader;

/**
 * Creates the parameter spaces for the lab from a json configuration.
 * 
 * @author kutschke
 * @author oferschke
 * 
 */
public class ParameterSpaceParser
{

    public static int folds;

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        String inputFile = pipelineConfiguration.getString("inputFile");
        String goldFile = pipelineConfiguration.getString("goldFile");

        // DIMENSIONS

        // configure training data reader dimension
        Map<String, Object> dimReaderTrain = new HashMap<String, Object>();
        dimReaderTrain.put("readerTrain", STSReader.class);
        dimReaderTrain.put("readerTrainParams", Arrays.asList(
                STSReader.PARAM_INPUT_FILE, inputFile,
                STSReader.PARAM_GOLD_FILE, goldFile
                ));

        JSONArray classificationArgsO = pipelineConfiguration.getJSONArray("classification");
        List<Object[]> classificationArgs = new ArrayList<Object[]>();
        for (Object object : classificationArgsO) {

            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("trainingArgs").toArray(new String[] {});

            classificationArgs.add(array);
        }

        Object[] classificationArgsArray = classificationArgs.toArray();

        JSONArray featureSetConf = pipelineConfiguration.getJSONArray("featureSets");
        List<Object[]> featureSets = new ArrayList<Object[]>();
        for (Object object : featureSetConf) {
            Object[] featureSet = ((JSONObject) object).getJSONArray("featureSet").toArray(
                    new String[] {});
            featureSets.add(featureSet);
        }

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readerTrain", dimReaderTrain),
                Dimension.create("classificationArguments", classificationArgsArray),
                Dimension.create("featureSet", featureSets.toArray()),
                // this is important
                Dimension.create("isRegressionExperiment", true),
                Dimension.create("multiLabel", false));
        return pSpace;
    }
}