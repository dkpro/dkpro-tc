package de.tudarmstadt.ukp.dkpro.tc.demo.regression;

import java.io.IOException;
import java.util.ArrayList;
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

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {
        // DIMENSIONS
        folds = pipelineConfiguration.getInt("folds");

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
                Dimension.create("classificationArguments", classificationArgsArray),
                Dimension.create("featureSet", featureSets.toArray()),
                // this is important
                Dimension.create("isRegressionExperiment", true),
                Dimension.create("folds", folds),
                Dimension.create("multiLabel", false));
        return pSpace;
    }
}