package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts information if the classification unit is situated between two tokens
 * (left and right boundary tokens)
 * 
 * @author Andriy Nadolskyy
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class SituatedBetweenTwoTokensUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
	// takes as parameter a composition of space separated boundary tokens: on first place is
	// the left boundary token and on the second the right boundary token
    public static final String PARAM_SPACE_SEPARATED_BOUNDARY_TOKENS = "spaceSeparatedBoundaryTokens";
    @ConfigurationParameter(name = PARAM_SPACE_SEPARATED_BOUNDARY_TOKENS, mandatory = true)
    private String spaceSeparatedBoundaryTokens;
    
    /**
     * Public name of the feature "Between two tokens"
     */
    public static final String BETWEEN_TWO_TOKENS = "BetweenTwoTokens";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
    	int counter = 0;
    	
    	String[] boundaries = spaceSeparatedBoundaryTokens.split(" ");
	   	if (boundaries.length !=2) {
	   		System.out.println("PARAM_SPACE_SEPARATED_BOUNDARY_TOKENS must be correctly set!");
	    }
    	String leftBoundary = boundaries[0];
    	String rightBoundary = boundaries[1];
    	
    	Token tokenAnnotation = JCasUtil.selectCovered(jcas, Token.class, classificationUnit).get(0);
    	int maxTokensCount = JCasUtil.select(jcas, Token.class).size(); 
    	
    	// handle tokens situated before the actual token
    	List<Token> precedingTokens = JCasUtil.selectPreceding(jcas, Token.class, tokenAnnotation, maxTokensCount);
    	for (Token token : precedingTokens) {
			if (token.getCoveredText().equals(leftBoundary)){
				counter += 1;
			}
			else if (token.getCoveredText().equals(rightBoundary)){
				counter -= 1;
			}
		}

        List<Feature> featList = new ArrayList<Feature>();
        boolean bool = false;
        
        // if boundaries are equally it's impossible/not exactly clear how to find out if one 
        // in the middle is the left or right boundary, e.g. --" it " is a " test "-- 
        // - it's not clear if 'a' is inside or outside of " "
        // but it's clearly inside if the entire sum until this token is an odd number
        if (leftBoundary.equals(rightBoundary)){
        	if (counter % 2 == 1){
        		featList.add(new Feature(BETWEEN_TWO_TOKENS, !bool));
        	}
        	else{
        		featList.add(new Feature(BETWEEN_TWO_TOKENS, bool));
        	}
        }
        // different boundaries
        else if (counter > 0){
        	featList.add(new Feature(BETWEEN_TWO_TOKENS, !bool));
        }
        else{
        	featList.add(new Feature(BETWEEN_TWO_TOKENS, bool));
        }
        		
        return featList;
    }
}