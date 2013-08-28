package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity;

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
import de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity.io.MovieReviewCorpusReader;
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

    public static ParameterSpace createParamSpaceFromJson(JSONObject pipelineConfiguration)
        throws IOException

    {

        String corpusFilePathTrain = pipelineConfiguration.getString("corpusFilePathTrain");
        String corpusFilePathTest = pipelineConfiguration.getString("corpusFilePathTest");
        String languageCode = pipelineConfiguration.getString("languageCode");

        // configure training data reader dimension
        Map<String, Object> dimReaderTrain = new HashMap<String, Object>();
        dimReaderTrain.put("readerTrain", MovieReviewCorpusReader.class);
        dimReaderTrain.put("readerTrainParams", Arrays.asList(new Object[] {
                MovieReviewCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                MovieReviewCorpusReader.PARAM_LANGUAGE, languageCode,
                MovieReviewCorpusReader.PARAM_PATTERNS,
                MovieReviewCorpusReader.INCLUDE_PREFIX + "*/*.txt" }
                ));

        // configure test data reader dimension (only for train-test setup)
        Map<String, Object> dimReaderTest = new HashMap<String, Object>();
        dimReaderTest.put("readerTest", MovieReviewCorpusReader.class);
        dimReaderTest.put("readerTestParams", Arrays.asList(new Object[] {
                MovieReviewCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                MovieReviewCorpusReader.PARAM_LANGUAGE, languageCode,
                MovieReviewCorpusReader.PARAM_PATTERNS,
                MovieReviewCorpusReader.INCLUDE_PREFIX + "*/*.txt" }
                ));

        // Load classifier
        JSONArray classificationArgsO = pipelineConfiguration.getJSONArray("classification");
        List<Object[]> classificationArgs = new ArrayList<Object[]>();
        for (Object object : classificationArgsO) {
            JSONObject jObj = (JSONObject) object;
            Object[] array = jObj.getJSONArray("trainingArgs").toArray(new String[] {});
            classificationArgs.add(array);
        }

        // Load feature extractors
        JSONArray featureSetConf = pipelineConfiguration.getJSONArray("featureSets");
        List<Object[]> featureSets = new ArrayList<Object[]>();
        for (Object object : featureSetConf) {
            Object[] featureSet = ((JSONObject) object).getJSONArray("featureSet").toArray(
                    new String[] {});
            featureSets.add(featureSet);
        }

        // Load pipeline parameters
        Object[] specialPipelineParameters = new Object[] {
                NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
                pipelineConfiguration.getInt("nGramMinSize"),
                NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
                pipelineConfiguration.getInt("nGramMaxSize") };
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

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readerTrain", dimReaderTrain),
                Dimension.createBundle("readerTest", dimReaderTest),
                Dimension.create("multiLabel", false),
                Dimension.create("folds", pipelineConfiguration.getInt("folds")),
                Dimension.create("pipelineParameters", pipelineParameters.toArray()),
                Dimension.create("featureSet", featureSets.toArray()),
                Dimension.create("classificationArguments", classificationArgs.toArray()));
        return pSpace;
    }
}
