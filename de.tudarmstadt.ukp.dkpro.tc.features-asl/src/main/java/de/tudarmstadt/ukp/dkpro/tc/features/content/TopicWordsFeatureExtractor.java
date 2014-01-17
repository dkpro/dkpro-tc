package de.tudarmstadt.ukp.dkpro.tc.features.content;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Given a list of topic terms, extracts the ratio of topic terms to all terms.
 */
public class TopicWordsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    // takes as parameter list of names of word-list-files in resources, outputs one attribute per
    // list
    public static final String PARAM_TOPIC_FILE_PATH = "topicFilePath";
    @ConfigurationParameter(name = PARAM_TOPIC_FILE_PATH, mandatory = true)
    private String topicFilePath;

    private String prefix;

    @Override
    public List<IFeature> extract(JCas jcas)
        // TODO: not adapted for focus annotations
        throws TextClassificationException
    {
        if (topicFilePath == null || topicFilePath.isEmpty()) {
            System.out.println("Path to word list must be set!");
        }
        List<String> topics = null;
        List<IFeature> featList = new ArrayList<IFeature>();
        List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
        try {
            topics = FileUtils.readLines(new File(topicFilePath));
            for (String t : topics) {
                featList.addAll(countWordHits(t, tokens));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return featList;
    }

    private List<IFeature> countWordHits(String wordListName, List<String> tokens)
        throws TextClassificationException
    {

        // word lists are stored in resources folder relative to feature extractor
        String wordListPath = TopicWordsFeatureExtractor.class.getClassLoader()
                .getResource("./" + wordListName).getPath();
        List<String> topicwords = null;
        try {
            topicwords = FileUtils.readLines(new File(wordListPath), "utf-8");
        }
        catch (IOException e) {
            throw new TextClassificationException(e);
        }
        int wordcount = 0;
        for (String token : tokens) {
            if (topicwords.contains(token)) {
                wordcount++;
            }
        }
        double numTokens = tokens.size();
        // name the feature same as wordlist
        return Arrays.<IFeature>asList(new Feature(prefix + wordListName,
                numTokens > 0 ? (wordcount / numTokens) : 0));
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        prefix = "TopicWords_";

        return true;
    }
}
