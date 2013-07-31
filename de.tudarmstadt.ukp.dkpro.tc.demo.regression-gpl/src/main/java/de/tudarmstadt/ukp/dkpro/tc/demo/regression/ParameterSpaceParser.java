package de.tudarmstadt.ukp.dkpro.tc.demo.regression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;

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
    public static Boolean[] toLowerCase;

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        // DIMENSIONS

        folds = pipelineConfiguration.getInt("folds");
        Object[] toLowerCaseO = pipelineConfiguration.getJSONArray("toLowerCase").toArray();
        toLowerCase = Arrays.asList(toLowerCaseO).toArray(new Boolean[toLowerCaseO.length]);

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

        // JSONArray pairFeatureSetConf = pipelineConfiguration.getJSONArray("pairFeatureSets");
        // List<Object[]> pairFeatureSets = new ArrayList<Object[]>();
        // for (Object object : pairFeatureSetConf) {
        // Object[] pairFeatureSet = ((JSONObject) object).getJSONArray("pairFeatureSet").toArray(
        // new String[] {});
        // pairFeatureSets.add(pairFeatureSet);
        // }

        Object[] pipelineParameters = new Object[] {};
        Object[] featureParameters = new Object[] {};

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.create("classificationArguments", classificationArgsArray),
                Dimension.create("featureSet", featureSets.toArray()),
                // Dimension.create("pairFeatureSet", pairFeatureSets.toArray()),
                Dimension.create("folds", folds),
                Dimension.create("lowerCase", toLowerCase),
                Dimension.create("multiLabel", false),
                Dimension.create("pipelineParameters", Arrays.asList(pipelineParameters)),
                Dimension.create("featureParameters", featureParameters));
        return pSpace;
    }
}