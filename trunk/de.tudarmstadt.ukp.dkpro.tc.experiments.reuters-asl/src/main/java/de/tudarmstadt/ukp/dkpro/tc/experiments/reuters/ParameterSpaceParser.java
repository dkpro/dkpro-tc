package de.tudarmstadt.ukp.dkpro.tc.experiments.reuters;

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

    public static String languageCode;
    public static int folds;
    public static Integer[] topNgramsK;
    public static Boolean[] toLowerCase;

    public static String threshold;

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        // DIMENSIONS

        languageCode = pipelineConfiguration.getString("languageCode");
        folds = pipelineConfiguration.getInt("folds");

        Object[] topNgramsKO = pipelineConfiguration.getJSONArray("topNgramsK").toArray();
        topNgramsK = Arrays.asList(topNgramsKO).toArray(new Integer[topNgramsKO.length]);
        Object[] toLowerCaseO = pipelineConfiguration.getJSONArray("toLowerCase").toArray();
        toLowerCase = Arrays.asList(toLowerCaseO).toArray(new Boolean[toLowerCaseO.length]);

        JSONArray classificationArgsO = pipelineConfiguration.getJSONArray("classification");
        List<Object[]> classificationArgs = new ArrayList<Object[]>();
        for (Object object : classificationArgsO) {

            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("trainingArgs").toArray(new String[] {});
            threshold = jObj.getString("threshold");

            classificationArgs.add(array);
        }
        Object[] classificationArgsArray = classificationArgs.toArray();

        //Load config for feature extractor sets
        JSONArray featureSetConf = pipelineConfiguration.getJSONArray("featureSets");
        List<Object[]> featureSets = new ArrayList<Object[]>();
        for (Object object : featureSetConf) {
            Object[] featureSet = ((JSONObject)object).getJSONArray("featureSet").toArray(new String[] {});
            featureSets.add(featureSet);
        }

        String languageCode = pipelineConfiguration.getString("languageCode");

        Object[] pipelineParameters = new Object[]{
                NGramFeatureExtractor.PARAM_NGRAM_MIN_N, pipelineConfiguration.getInt("nGramMinSize"),
                NGramFeatureExtractor.PARAM_NGRAM_MAX_N, pipelineConfiguration.getInt("nGramMaxSize")};

        ParameterSpace pSpace = new ParameterSpace(
        		Dimension.create("languageCode", languageCode),
                Dimension.create("threshold", threshold),
                Dimension.create("classificationArguments", classificationArgsArray),
                Dimension.create("featureSet", featureSets.toArray()),
                Dimension.create("folds", folds),
                Dimension.create("topNgramsK", topNgramsK),
                Dimension.create("lowerCase", toLowerCase),
                Dimension.create("multiLabel", true),
                Dimension.create("languageCode", languageCode),
                Dimension.create("pipelineParameters", Arrays.asList(pipelineParameters)));
        return pSpace;
    }
}
