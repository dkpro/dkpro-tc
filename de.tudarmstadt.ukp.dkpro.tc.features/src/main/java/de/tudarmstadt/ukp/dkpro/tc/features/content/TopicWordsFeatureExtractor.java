package de.tudarmstadt.ukp.dkpro.tc.features.content;

import static org.uimafit.util.JCasUtil.toText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;



public class TopicWordsFeatureExtractor
    implements FeatureExtractor, Initializable
{
	//takes as parameter list of names of word-list-files in resources, outputs one attribute per list
    public static final String PARAM_TOPIC_FILE = "TopicFile";
    
    private String topicFilePath;
    private String prefix;

	@Override
	public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
	//TODO: not adapted for focus annotations
			throws TextClassificationException {
                if (topicFilePath==null || topicFilePath.isEmpty()) {
                	System.out.println("Path to word list must be set!");       
                }
        		List<String> topics = null;
        		List<Feature> featList = new ArrayList<Feature>();
         		List<String> tokens = toText(JCasUtil.select(jcas, Token.class));
		        try {
				    topics = FileUtils.readLines(new File(topicFilePath));
				    for (String t : topics) {
				    	featList.addAll(countWordHits(t, tokens)); 
				    }
		        } catch (IOException e) {
					e.printStackTrace();
				}
		   return featList;
	}
                
    private List<Feature> countWordHits(String wordListName, List<String> tokens) {            
           
    	//word lists are stored in resources folder relative to feature extractor
		String wordListPath = TopicWordsFeatureExtractor.class.getClassLoader().getResource("./"+wordListName).getPath();
		List<String> topicwords = null;
 		try {
 			topicwords = FileUtils.readLines(new File(wordListPath), "utf-8");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int wordcount = 0;				
 	    for (String token : tokens) {
 	            if (topicwords.contains(token)) {
 	                wordcount++;
 	            }
 			}	
 		double numTokens = tokens.size();
    	//name the feature same as wordlist
 		return Arrays.asList(new Feature(prefix+wordListName, numTokens>0?(wordcount / numTokens):0));
 	}
 
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {

        initializeParameters(context);
        prefix = "TopicWords_";

    }
    
    private void initializeParameters(UimaContext context) {
        if (context.getConfigParameterValue(PARAM_TOPIC_FILE) != null) {
            this.topicFilePath = (String) context.getConfigParameterValue(PARAM_TOPIC_FILE);
        }
	}
	
}
