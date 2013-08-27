package de.tudarmstadt.ukp.dkpro.tc.demo.reuters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;

/**
 * Creates the parameter spaces for the lab from a json configuration.
 * 
 * @author kutschke
 * @author oferschke
 * 
 */
public class ParameterSpaceParser
{

    public static Boolean[] toLowerCase;
    public static String threshold;

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        // DIMENSIONS
        Object[] specialPipelineParameters = new Object[] {
                NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
                pipelineConfiguration.getInt("nGramMinSize"),
                NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
                pipelineConfiguration.getInt("nGramMaxSize") };
        threshold = pipelineConfiguration.getString("threshold");

        // Load config for classifier
        JSONArray classificationArgsO = pipelineConfiguration.getJSONArray("classification");
        List<Object[]> classificationArgs = new ArrayList<Object[]>();
        for (Object object : classificationArgsO) {
            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("trainingArgs").toArray(new String[] {});
            classificationArgs.add(array);
        }

        // Load config for pipeline parameters
        JSONArray pipelineParamsArg0 = pipelineConfiguration.getJSONArray("pipelineParameters");
        List<List<Object>> pipelineParameters = new ArrayList<List<Object>>();
        for (Object object : pipelineParamsArg0) {
            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("pipelineParameter").toArray(new String[0]);
            List<Object> args = new ArrayList<Object>(Arrays.asList(array));
            for (Object specialArg : specialPipelineParameters) {
                args.add(specialArg);
            }
            pipelineParameters.add(args);
        }

        // Load feature extractor sets
        JSONArray featureSetConf = pipelineConfiguration.getJSONArray("featureSets");
        List<List<String>> featureSets = new ArrayList<List<String>>();
        for (Object object : featureSetConf) {
            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("featureSet").toArray(new String[0]);
            List<String> args = new ArrayList<String>();
            for (Object element : array) {
                args.add((String) element);
            }
            featureSets.add(args);
        }

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.create("threshold", threshold),
                Dimension.create("classificationArguments", classificationArgs.toArray()),
                Dimension.create("featureSet", featureSets.toArray()),
                Dimension.create("multiLabel", true),
                Dimension.create("pipelineParameters", pipelineParameters.toArray()));
        return pSpace;
    }
}
