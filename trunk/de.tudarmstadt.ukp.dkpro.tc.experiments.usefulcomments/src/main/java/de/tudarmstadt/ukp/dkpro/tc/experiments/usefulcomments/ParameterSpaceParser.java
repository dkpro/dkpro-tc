package de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments;

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

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        // DIMENSIONS

        Object[] toLowerCaseO = pipelineConfiguration.getJSONArray("toLowerCase").toArray();
        toLowerCase = Arrays.asList(toLowerCaseO).toArray(new Boolean[toLowerCaseO.length]);
        Object[] pipelineParameters = new Object[] {
                NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
                pipelineConfiguration.getInt("nGramMinSize"),
                NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
                pipelineConfiguration.getInt("nGramMaxSize") };

        // Load config for classifier
        JSONArray classificationArgsO = pipelineConfiguration.getJSONArray("classification");
        List<Object[]> classificationArgs = new ArrayList<Object[]>();
        for (Object object : classificationArgsO) {
            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("trainingArgs").toArray(new String[] {});
            classificationArgs.add(array);
        }

        // Load config for feature extractor sets
        JSONArray featureSetConf = pipelineConfiguration.getJSONArray("featureSets");
        List<Object[]> featureSets = new ArrayList<Object[]>();
        for (Object object : featureSetConf) {
            Object[] featureSet = ((JSONObject) object).getJSONArray("featureSet").toArray(
                    new String[] {});
            featureSets.add(featureSet);
        }

        // Load config for feature extractor set parameters
        JSONArray featureParametersConf = pipelineConfiguration.getJSONArray("featureParameters");
        List<Object[]> featureParameters = new ArrayList<Object[]>();
        for (Object object : featureParametersConf) {
            Object[] featureParameter = ((JSONObject) object).getJSONArray("featureParameter")
                    .toArray(new String[] {});
            featureParameters.add(featureParameter);
        }

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.create("multiLabel", false),
                Dimension.create("lowerCase", toLowerCase),
                Dimension.create("pipelineParameters", Arrays.asList(pipelineParameters)),
                Dimension.create("featureSet", featureSets.toArray()),
                Dimension.create("featureParameters", featureParameters.toArray()),
                Dimension.create("classificationArguments", classificationArgs.toArray()));
        return pSpace;
    }
}
