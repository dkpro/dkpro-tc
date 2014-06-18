package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Counts the ratio of tokens containing numbers or 
 * combinations of numbers and letters. Possibly useful to capture
 * teenage slang in online chats. For texts with lot of numbers
 * expected, you may want to modify the regex to capture 
 * letters AND numbers only
 */
public class NumberWordsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FEATURE_NAME = "WordsWithNumbers";

    @Override
    public List<Feature> extract(JCas jcas)
    {

        List<Feature> featList = new ArrayList<Feature>();
        List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
        int nrOfTokens = tokens.size();

        Pattern p = Pattern.compile("^[a-zA-Z0-9]*[0-9]+[a-zA-Z0-9]*$");

        int pmatches = 0;

        for (String t : tokens) {
            Matcher m = p.matcher(t);
            if (m.find()) {
                pmatches++;
                System.out.println(t + " matches Words With Numbers");
            }
        }
        featList.add(new Feature(FEATURE_NAME, (double) pmatches / nrOfTokens));

        return featList;
    }
}